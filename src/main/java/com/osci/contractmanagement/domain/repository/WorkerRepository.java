package com.osci.contractmanagement.domain.repository;

import com.osci.contractmanagement.domain.model.worker.Worker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
public interface WorkerRepository {
    Worker save(Worker worker);
    Optional<Worker> findByIdAndDeletedAtIsNull(Long workerId);
    Optional<Worker> findByUserIdAndDeletedAtIsNull(Long userId);
    boolean existsByUserIdAndDeletedAtIsNull(Long userId);
    Page<Worker> findByDeletedAtIsNull(Pageable pageable);
    Page<Worker> findByNameContainingAndDeletedAtIsNull(String keyword, Pageable pageable);
}
