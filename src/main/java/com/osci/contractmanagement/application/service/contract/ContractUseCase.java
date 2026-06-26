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

    List<ContractUploadResponseDto> uploadContracts(List<MultipartFile> files);

    Page<ContractResponseDto> getContracts(com.osci.contractmanagement.domain.model.company.OcrStatus ocrStatus, Pageable pageable);
    ContractResponseDto getContract(Long contractId);
    ContractFileData getContractFile(Long contractId);

    void processContract(Long contractId);
}