package com.osci.contractmanagement.infrastructure.repository;

import com.osci.contractmanagement.domain.model.worker.WorkerProjectAssignment;
import com.osci.contractmanagement.domain.repository.WorkerProjectAssignmentRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerProjectAssignmentJpaRepository
        extends WorkerProjectAssignmentRepository, JpaRepository<WorkerProjectAssignment, Long> {
}
