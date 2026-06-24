package com.osci.contractmanagement.application.dto.response.company;

import com.osci.contractmanagement.domain.model.company.Company;
import com.osci.contractmanagement.domain.model.company.ContractType;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class CompanyResponseDto {
    private Long id;
    private String name;
    private ContractType contractType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CompanyResponseDto of(Company company) {
        return CompanyResponseDto.builder()
                .id(company.getId())
                .name(company.getName())
                .contractType(company.getContractType())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
