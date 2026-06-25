package com.osci.contractmanagement.application.service.contract;

import com.osci.contractmanagement.application.dto.response.contract.ContractFileData;
import com.osci.contractmanagement.application.dto.response.contract.ContractResponseDto;
import com.osci.contractmanagement.application.dto.response.contract.ContractUploadResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContractUseCase {

    Long uploadContract(byte[] fileBytes, String originalFilename, String contentType);

    /**
     * 여러 파일을 한 번에 업로드한다. 파일 하나가 실패해도 나머지는 계속 처리하는
     * "부분 성공 허용" 정책을 여기서 결정한다 (Controller는 이 정책을 모름).
     */
    List<ContractUploadResponseDto> uploadContracts(List<MultipartFile> files);

    Page<ContractResponseDto> getContracts(com.osci.contractmanagement.domain.model.company.OcrStatus ocrStatus, Pageable pageable);
    ContractResponseDto getContract(Long contractId);
    ContractFileData getContractFile(Long contractId);

    void processContract(Long contractId);
}