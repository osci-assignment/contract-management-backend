package com.osci.contractmanagement.application.dto.response.worker;

import com.osci.contractmanagement.domain.model.company.Company;
import com.osci.contractmanagement.domain.model.company.Project;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class WorkerProjectResponseDto {
    private Long projectId;
    private String projectTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long companyId;
    private String companyName;
    private LocalDateTime assignedAt;

    public static WorkerProjectResponseDto of(Project project, Company company, LocalDateTime assignedAt) {
        return WorkerProjectResponseDto.builder()
                .projectId(project.getId())
                .projectTitle(project.getTitle())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .companyId(company.getId())
                .companyName(company.getName())
                .assignedAt(assignedAt)
                .build();
    }
}
