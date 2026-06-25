package com.osci.contractmanagement.application.dto.response.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ContractUploadResponseDto {
    private Long contractId;
    private String fileName;
    private String ocrStatus;

    public static ContractUploadResponseDto of(Long contractId, String fileName) {
        return new ContractUploadResponseDto(contractId, fileName, "PENDING");
    }

    public static ContractUploadResponseDto failed(String fileName) {
        return new ContractUploadResponseDto(null, fileName, "UPLOAD_FAILED");
    }
}