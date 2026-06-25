package com.osci.contractmanagement.application.service.project;

import com.osci.contractmanagement.application.dto.request.project.UpdateProjectRequestDto;
import com.osci.contractmanagement.application.dto.response.project.ProjectResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectUseCase {
    Page<ProjectResponseDto> getProjects(Pageable pageable);
    ProjectResponseDto getProject(Long projectId);
    ProjectResponseDto updateProject(Long projectId, UpdateProjectRequestDto request);
}
