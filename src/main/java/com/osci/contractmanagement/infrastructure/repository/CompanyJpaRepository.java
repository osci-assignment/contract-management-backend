package com.osci.contractmanagement.infrastructure.repository;

import com.osci.contractmanagement.domain.model.company.Company;
import com.osci.contractmanagement.domain.repository.CompanyRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyJpaRepository extends CompanyRepository, JpaRepository<Company, Long> {
}
