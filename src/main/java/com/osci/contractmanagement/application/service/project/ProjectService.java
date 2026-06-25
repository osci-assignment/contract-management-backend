package com.osci.contractmanagement.application.service.project;

import com.osci.contractmanagement.application.dto.request.project.UpdateProjectRequestDto;
import com.osci.contractmanagement.application.dto.response.project.ProjectResponseDto;
import com.osci.contractmanagement.application.exceptions.BusinessException;
import com.osci.contractmanagement.application.exceptions.BusinessExceptionType;
import com.osci.contractmanagement.domain.model.company.Company;
import com.osci.contractmanagement.domain.model.company.Project;
import com.osci.contractmanagement.domain.repository.CompanyRepository;
import com.osci.contractmanagement.domain.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService implements ProjectUseCase {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;

    public ProjectService(ProjectRepository projectRepository, CompanyRepository companyRepository) {
        this.projectRepository = projectRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDto> getProjects(Pageable pageable) {
        return projectRepository.findByDeletedAtIsNull(pageable)
                .map(ProjectResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDto getProject(Long projectId) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new BusinessException(BusinessExceptionType.PROJECT_NOT_FOUND));
        return ProjectResponseDto.of(project);
    }

    @Override
    @Transactional
    public ProjectResponseDto updateProject(Long projectId, UpdateProjectRequestDto request) {
        Company company = companyRepository.findByProjects_IdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new BusinessException(BusinessExceptionType.PROJECT_NOT_FOUND));

        company.updateProject(projectId, request.getTitle(), request.getStartDate(), request.getEndDate());

        return getProject(projectId);
    }
}
