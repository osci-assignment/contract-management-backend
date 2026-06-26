package com.osci.contractmanagement.application.service.contract;

import com.osci.contractmanagement.application.dto.response.contract.ContractFileData;
import com.osci.contractmanagement.application.dto.response.contract.ContractResponseDto;
import com.osci.contractmanagement.application.dto.response.contract.ContractUploadResponseDto;
import com.osci.contractmanagement.application.provider.ContractInfoExtractor;
import com.osci.contractmanagement.application.provider.FileStorageProvider;
import com.osci.contractmanagement.application.provider.TextExtractor;
import com.osci.contractmanagement.application.service.contract.event.ContractEventProducer;
import com.osci.contractmanagement.domain.model.contract.Contract;
import com.osci.contractmanagement.infrastructure.contract.event.ContractOcrConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ContractFacade implements ContractUseCase {

    private final List<TextExtractor> textExtractors;
    private final ContractInfoExtractor contractInfoExtractor;
    private final FileStorageProvider fileStorageProvider;
    private final ContractEventProducer contractEventProducer;
    private final ContractCommandService contractCommandService;
    private final ContractQueryService contractQueryService;

    public ContractFacade(List<TextExtractor> textExtractors, ContractInfoExtractor contractInfoExtractor, FileStorageProvider fileStorageProvider, ContractEventProducer contractEventProducer, ContractCommandService contractCommandService, ContractQueryService contractQueryService) {
        this.textExtractors = textExtractors;
        this.contractInfoExtractor = contractInfoExtractor;
        this.fileStorageProvider = fileStorageProvider;
        this.contractEventProducer = contractEventProducer;
        this.contractCommandService = contractCommandService;
        this.contractQueryService = contractQueryService;
    }

    @Override
    public Long uploadContract(byte[] fileBytes, String originalFilename, String contentType) {
        String fileUrl = fileStorageProvider.store(fileBytes, originalFilename);

        Contract contract;
        try {
            // createPending()은 별도 트랜잭션으로 즉시 커밋된다. 여기서 실패하면
            // Contract row 자체가 없는 상태이므로, 방금 저장한 파일은 아무도 참조하지
            // 않는 고스트 파일이 된다. 즉시 보상 삭제한다.
            contract = contractCommandService.createPending(fileUrl, originalFilename, contentType);
        } catch (Exception e) {
            log.warn("계약서 등록(DB 저장) 실패로 저장된 파일을 삭제합니다 - fileUrl: {}", fileUrl, e);
            fileStorageProvider.delete(fileUrl);
            throw e;
        }

        try {
            contractEventProducer.publishOcrRequested(contract.getId());
        } catch (Exception e) {
            // 여기서는 Contract row가 이미 커밋되어 있으므로 파일을 지우면 안 된다
            // (지우면 살아있는 Contract가 없는 파일을 가리키게 됨). 대신 이 Contract는
            // 처리 트리거를 못 받아 PENDING 상태로 멈춘다 - 별도 복구(재발행) 수단이 필요하다.
            // (현재 미구현, README "미완성/개선점" 참고)
            log.error("Kafka 발행 실패 - contractId: {}가 PENDING 상태로 멈췄습니다. 별도 복구 처리가 필요합니다.",
                    contract.getId(), e);
            throw e;
        }

        return contract.getId();
    }

    @Override
    public List<ContractUploadResponseDto> uploadContracts(List<MultipartFile> files) {
        return files.stream()
                .map(this::uploadSingleFile)
                .toList();
    }

    private ContractUploadResponseDto uploadSingleFile(MultipartFile file) {
        try {
            Long contractId = uploadContract(file.getBytes(), file.getOriginalFilename(), file.getContentType());
            return ContractUploadResponseDto.of(contractId, file.getOriginalFilename());
        } catch (IOException | RuntimeException e) {
            log.warn("계약서 업로드 실패 - filename: {}", file.getOriginalFilename(), e);
            return ContractUploadResponseDto.failed(file.getOriginalFilename());
        }
    }

    @Override
    public Page<ContractResponseDto> getContracts(com.osci.contractmanagement.domain.model.company.OcrStatus ocrStatus, Pageable pageable) {
        return contractQueryService.getContracts(ocrStatus, pageable);
    }

    @Override
    public ContractResponseDto getContract(Long contractId) {
        return contractQueryService.getContract(contractId);
    }

    @Override
    public ContractFileData getContractFile(Long contractId) {
        return contractQueryService.getContractFile(contractId);
    }

    @Override
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 10
    )
    public void processContract(Long contractId) {

        log.info("계약서 처리 시도 - contractId: {}, attempt: {}", contractId, getCurrentAttempt());
        Contract contract = contractCommandService.markProcessing(contractId);

        byte[] fileBytes = fileStorageProvider.load(contract.getFileUrl());
        String text = extractText(contract.getContentType(), fileBytes);
        ContractInfoExtractor.ContractExtractionResult result = contractInfoExtractor.extract(text);
        // contract 객체 준영속으로 인한 id 전달
        contractCommandService.completeWithExtractionResult(contractId, text, result);
    }

    private int getCurrentAttempt() {
        RetryContext context = RetrySynchronizationManager.getContext();
        return context != null ? context.getRetryCount() + 1 : 1;
    }

    @Recover
    public void recoverProcessContract(Exception e, Long contractId) {
        log.warn("계약서 처리 최종 실패 - contractId: {}, reason: {}", contractId, e.getMessage());
        contractCommandService.markFailed(contractId, e);
    }

    private String extractText(String contentType, byte[] fileBytes) {
        return textExtractors.stream()
                .filter(extractor -> extractor.supports(contentType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + contentType))
                .extractText(fileBytes);
    }
}