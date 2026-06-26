package com.osci.contractmanagement.domain.repository;

import com.osci.contractmanagement.domain.model.company.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProjectRepository {
    Optional<Project> findByIdAndDeletedAtIsNull(Long id);
    Page<Project> findByDeletedAtIsNull(Pageable pageable);
}
