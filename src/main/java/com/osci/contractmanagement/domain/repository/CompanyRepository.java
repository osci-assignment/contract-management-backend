package com.osci.contractmanagement.domain.repository;

import com.osci.contractmanagement.domain.model.company.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CompanyRepository {
    Company save(Company company);
    Optional<Company> findByIdAndDeletedAtIsNull(Long id);
    Optional<Company> findByNameAndDeletedAtIsNull(String name);
    Page<Company> findByDeletedAtIsNull(Pageable pageable);
    void deleteById(Long id);
}
