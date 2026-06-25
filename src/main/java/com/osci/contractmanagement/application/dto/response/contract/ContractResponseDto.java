package com.osci.contractmanagement.application.dto.response.contract;

import com.osci.contractmanagement.domain.model.company.Company;
import com.osci.contractmanagement.domain.model.company.OcrStatus;
import com.osci.contractmanagement.domain.model.company.Project;
import com.osci.contractmanagement.domain.model.contract.Contract;
import lombok.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class ContractResponseDto {
    Long contractId;
    ContractProjectResponseDto project;
    ContractCompanyResponseDto company;
    String fileName;
    String contentType;
    OcrStatus ocrStatus;
    String failureReason;

    public static ContractResponseDto of(Contract contract, Company company, Project project) {
        return ContractResponseDto.builder()
                .contractId(contract.getId())
                .project(project != null
                        ? new ContractProjectResponseDto(project.getId(), project.getTitle())
                        : null)
                .company(company != null
                        ? new ContractCompanyResponseDto(company.getId(), company.getName())
                        : null)
                .fileName(contract.getOriginalFilename())
                .contentType(contract.getContentType())
                .ocrStatus(contract.getOcrStatus())
                .failureReason(contract.getFailureReason())
                .build();
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class ContractProjectResponseDto {
        Long projectId;
        String title;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class ContractCompanyResponseDto {
        Long companyId;
        String name;
    }
}