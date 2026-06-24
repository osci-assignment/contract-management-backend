package com.osci.contractmanagement.application.dto.response.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ContractUploadResponseDto {
    private Long contractId;
    private String ocrStatus;

    public static ContractUploadResponseDto of(Long contractId) {
        return new ContractUploadResponseDto(contractId, "PENDING");
    }
}