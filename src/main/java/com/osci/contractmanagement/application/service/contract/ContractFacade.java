package com.osci.contractmanagement.application.service.contract;

import com.osci.contractmanagement.application.dto.response.contract.ContractResponseDto;
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

        Contract contract = contractCommandService.createPending(fileUrl, contentType);

        contractEventProducer.publishOcrRequested(contract.getId());

        return contract.getId();
    }

    @Override
    public Page<ContractResponseDto> getContracts(Pageable pageable) {
        return contractQueryService.getContracts(pageable);
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