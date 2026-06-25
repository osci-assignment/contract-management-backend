package com.osci.contractmanagement.application.service.worker;

import com.osci.contractmanagement.application.dto.request.worker.CreateWorkerRequestDto;
import com.osci.contractmanagement.application.dto.request.worker.UpdateWorkerRequestDto;
import com.osci.contractmanagement.application.dto.response.worker.WorkerProjectResponseDto;
import com.osci.contractmanagement.application.dto.response.worker.WorkerResponseDto;
import com.osci.contractmanagement.application.exceptions.BusinessException;
import com.osci.contractmanagement.application.exceptions.BusinessExceptionType;
import com.osci.contractmanagement.domain.model.company.Company;
import com.osci.contractmanagement.domain.model.company.Project;
import com.osci.contractmanagement.domain.model.user.User;
import com.osci.contractmanagement.domain.model.worker.Worker;
import com.osci.contractmanagement.domain.model.worker.WorkerProjectAssignment;
import com.osci.contractmanagement.domain.repository.CompanyRepository;
import com.osci.contractmanagement.domain.repository.UserRepository;
import com.osci.contractmanagement.domain.repository.WorkerProjectAssignmentRepository;
import com.osci.contractmanagement.domain.repository.WorkerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkerService implements WorkerUseCase {

    private final WorkerRepository workerRepository;
    private final WorkerProjectAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public WorkerService(WorkerRepository workerRepository,
                         WorkerProjectAssignmentRepository assignmentRepository,
                         UserRepository userRepository,
                         CompanyRepository companyRepository) {
        this.workerRepository = workerRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public WorkerResponseDto createMyWorkerProfile(Long loginUserId, CreateWorkerRequestDto request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(loginUserId)
                .orElseThrow(() -> new BusinessException(BusinessExceptionType.USER_NOT_FOUND));

        if (!user.isApproved()) {
            throw new BusinessException(BusinessExceptionType.USER_NOT_APPROVED);
        }

        if (workerRepository.existsByUserIdAndDeletedAtIsNull(user.getId())) {
            throw new BusinessException(BusinessExceptionType.WORKER_ALREADY_EXISTS);
        }

        Worker worker = Worker.create(user.getId(), request.getName(), request.getPosition(), request.getDepartment());
        workerRepository.save(worker);

        return WorkerResponseDto.of(worker);
    }

    @Override
    @Transactional
    public WorkerResponseDto updateMyWorkerProfile(Long loginUserId, UpdateWorkerRequestDto request) {
        Worker worker = findWorkerByUserId(loginUserId);
        worker.update(request.getName(), request.getPosition(), request.getDepartment());
        return WorkerResponseDto.of(worker);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkerProjectResponseDto> getMyProjects(Long loginUserId) {
        Worker worker = findWorkerByUserId(loginUserId);

        List<WorkerProjectAssignment> assignments = assignmentRepository.findByWorkerIdAndDeletedAtIsNull(worker.getId());

        return assignments.stream()
                .map(assignment -> {
                    Company company = companyRepository.findByProjects_IdAndDeletedAtIsNull(assignment.getProjectId())
                            .orElseThrow(() -> new BusinessException(BusinessExceptionType.PROJECT_NOT_FOUND));
                    Project project = company.findProjectById(assignment.getProjectId())
                            .orElseThrow(() -> new BusinessException(BusinessExceptionType.PROJECT_NOT_FOUND));
                    return WorkerProjectResponseDto.of(project, company, assignment.getAssignedAt());
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkerResponseDto> getWorkers(String keyword, Pageable pageable) {
        Page<Worker> workers = (keyword == null || keyword.isBlank())
                ? workerRepository.findByDeletedAtIsNull(pageable)
                : workerRepository.findByNameContainingAndDeletedAtIsNull(keyword, pageable);
        return workers.map(WorkerResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkerResponseDto getWorker(Long workerId) {
        return WorkerResponseDto.of(findWorker(workerId));
    }

    @Override
    @Transactional
    public WorkerResponseDto updateWorkerByAdmin(Long workerId, UpdateWorkerRequestDto request) {
        Worker worker = findWorker(workerId);
        worker.update(request.getName(), request.getPosition(), request.getDepartment());
        return WorkerResponseDto.of(worker);
    }

    @Override
    @Transactional
    public void deleteWorker(Long workerId) {
        Worker worker = findWorker(workerId);
        worker.delete();
    }

    @Override
    @Transactional
    public void assignProject(Long projectId, Long workerId) {
        Worker worker = findWorker(workerId);
        // Project가 실제 존재하는지 검증 (Company 애그리거트를 거쳐서 확인)
        findProject(projectId);

        if (assignmentRepository.existsByWorkerIdAndProjectIdAndDeletedAtIsNull(worker.getId(), projectId)) {
            throw new BusinessException(BusinessExceptionType.ASSIGNMENT_ALREADY_EXISTS);
        }

        assignmentRepository.save(WorkerProjectAssignment.create(worker.getId(), projectId));
    }

    @Override
    @Transactional
    public void unassignProject(Long projectId, Long workerId) {
        Worker worker = findWorker(workerId);

        WorkerProjectAssignment assignment = assignmentRepository
                .findByWorkerIdAndProjectIdAndDeletedAtIsNull(worker.getId(), projectId)
                .orElseThrow(() -> new BusinessException(BusinessExceptionType.ASSIGNMENT_NOT_FOUND));

        assignment.delete();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkerResponseDto> getProjectWorkers(Long projectId) {
        findProject(projectId);

        return assignmentRepository.findByProjectIdAndDeletedAtIsNull(projectId).stream()
                .map(assignment -> findWorker(assignment.getWorkerId()))
                .map(WorkerResponseDto::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkerProjectResponseDto> getWorkerProjects(Long workerId) {
        Worker worker = findWorker(workerId);

        List<WorkerProjectAssignment> assignments = assignmentRepository.findByWorkerIdAndDeletedAtIsNull(worker.getId());

        return assignments.stream()
                .map(assignment -> {
                    Company company = companyRepository.findByProjects_IdAndDeletedAtIsNull(assignment.getProjectId())
                            .orElseThrow(() -> new BusinessException(BusinessExceptionType.PROJECT_NOT_FOUND));
                    Project project = company.findProjectById(assignment.getProjectId())
                            .orElseThrow(() -> new BusinessException(BusinessExceptionType.PROJECT_NOT_FOUND));
                    return WorkerProjectResponseDto.of(project, company, assignment.getAssignedAt());
                })
                .toList();
    }

    private Worker findWorker(Long workerId) {
        return workerRepository.findByIdAndDeletedAtIsNull(workerId)
                .orElseThrow(() -> new BusinessException(BusinessExceptionType.WORKER_NOT_FOUND));
    }

    private Worker findWorkerByUserId(Long userId) {
        return workerRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(BusinessExceptionType.WORKER_NOT_FOUND));
    }

    private Project findProject(Long projectId) {
        Company company = companyRepository.findByProjects_IdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new BusinessException(BusinessExceptionType.PROJECT_NOT_FOUND));
        return company.findProjectById(projectId)
                .orElseThrow(() -> new BusinessException(BusinessExceptionType.PROJECT_NOT_FOUND));
    }
}
