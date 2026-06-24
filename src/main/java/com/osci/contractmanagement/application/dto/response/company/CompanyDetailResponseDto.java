package com.osci.contractmanagement.application.dto.response.company;

import com.osci.contractmanagement.domain.model.company.Company;
import com.osci.contractmanagement.domain.model.company.ContractType;
import com.osci.contractmanagement.domain.model.company.Project;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class CompanyDetailResponseDto {
    private Long id;
    private String name;
    private ContractType contractType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CompanyProjectResponseDto> projects;

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    @Getter
    public static class CompanyProjectResponseDto {
        private Long projectId;
        private String title;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime createdAt;

        public static CompanyProjectResponseDto of(Project project) {
            return CompanyProjectResponseDto.builder()
                    .projectId(project.getId())
                    .title(project.getTitle())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .createdAt(project.getCreatedAt())
                    .build();
        }
    }

    public static CompanyDetailResponseDto of(Company company) {
        return CompanyDetailResponseDto.builder()
                .id(company.getId())
                .name(company.getName())
                .contractType(company.getContractType())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .projects(company.getProjects().stream()
                        .map(CompanyProjectResponseDto::of).toList())

                .build();
    }
}
