package com.osci.contractmanagement.application.service.worker;

import com.osci.contractmanagement.application.dto.request.worker.CreateWorkerRequestDto;
import com.osci.contractmanagement.application.dto.request.worker.UpdateWorkerRequestDto;
import com.osci.contractmanagement.application.dto.response.worker.WorkerProjectResponseDto;
import com.osci.contractmanagement.application.dto.response.worker.WorkerResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WorkerUseCase {
    WorkerResponseDto createMyWorkerProfile(Long loginUserId, CreateWorkerRequestDto request);
    WorkerResponseDto updateMyWorkerProfile(Long loginUserId, UpdateWorkerRequestDto request);
    List<WorkerProjectResponseDto> getMyProjects(Long loginUserId);
    Page<WorkerResponseDto> getWorkers(String keyword, Pageable pageable);
    WorkerResponseDto getWorker(Long workerId);
    void deleteWorker(Long workerId);
    WorkerResponseDto updateWorkerByAdmin(Long workerId, UpdateWorkerRequestDto request);
    void assignProject(Long projectId, Long workerId);
    void unassignProject(Long projectId, Long workerId);
    List<WorkerResponseDto> getProjectWorkers(Long projectId);
    List<WorkerProjectResponseDto> getWorkerProjects(Long workerId);
}
