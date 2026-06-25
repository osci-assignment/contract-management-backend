package com.osci.contractmanagement.infrastructure.repository;

import com.osci.contractmanagement.domain.model.company.Project;
import com.osci.contractmanagement.domain.repository.ProjectRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectJpaRepository extends ProjectRepository, JpaRepository<Project, Long> {
}
