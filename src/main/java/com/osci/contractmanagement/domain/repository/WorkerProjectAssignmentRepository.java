package com.osci.contractmanagement.domain.repository;

import com.osci.contractmanagement.domain.model.worker.WorkerProjectAssignment;

import java.util.List;
import java.util.Optional;

public interface WorkerProjectAssignmentRepository {
    WorkerProjectAssignment save(WorkerProjectAssignment assignment);
    Optional<WorkerProjectAssignment> findByWorkerIdAndProjectIdAndDeletedAtIsNull(Long workerId, Long projectId);
    boolean existsByWorkerIdAndProjectIdAndDeletedAtIsNull(Long workerId, Long projectId);
    List<WorkerProjectAssignment> findByWorkerIdAndDeletedAtIsNull(Long workerId);
    List<WorkerProjectAssignment> findByProjectIdAndDeletedAtIsNull(Long projectId);
}
