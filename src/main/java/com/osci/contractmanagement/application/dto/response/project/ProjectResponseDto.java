package com.osci.contractmanagement.application.dto.response.project;

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
public class ProjectResponseDto {
    private Long projectId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long companyId;
    private String companyName;
    private LocalDateTime createdAt;

    public static ProjectResponseDto of(Project project) {
        return ProjectResponseDto.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .companyId(project.getCompany().getId())
                .companyName(project.getCompany().getName())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
