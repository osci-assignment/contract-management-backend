package com.osci.contractmanagement.presentation;

import com.osci.contractmanagement.application.dto.request.project.UpdateProjectRequestDto;
import com.osci.contractmanagement.application.dto.response.project.ProjectResponseDto;
import com.osci.contractmanagement.application.service.project.ProjectUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectUseCase projectUseCase;

    public ProjectController(ProjectUseCase projectUseCase) {
        this.projectUseCase = projectUseCase;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Page<ProjectResponseDto>>> getProjects(
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProjectResponseDto> response = projectUseCase.getProjects(pageable);
        return CommonResponse.ok(response);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<CommonResponse<ProjectResponseDto>> getProject(
            @PathVariable Long projectId
    ) {
        ProjectResponseDto response = projectUseCase.getProject(projectId);
        return CommonResponse.ok(response);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<ProjectResponseDto>> updateProject(
            @PathVariable Long projectId,
            @RequestBody @Valid UpdateProjectRequestDto request
    ) {
        ProjectResponseDto response = projectUseCase.updateProject(projectId, request);
        return CommonResponse.ok(response);
    }
}