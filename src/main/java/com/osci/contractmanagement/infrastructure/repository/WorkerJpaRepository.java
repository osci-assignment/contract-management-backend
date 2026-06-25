package com.osci.contractmanagement.infrastructure.repository;

import com.osci.contractmanagement.domain.model.worker.Worker;
import com.osci.contractmanagement.domain.repository.WorkerRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerJpaRepository extends WorkerRepository, JpaRepository<Worker, Long> {
}
