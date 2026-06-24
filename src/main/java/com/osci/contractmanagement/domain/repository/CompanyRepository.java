package com.osci.contractmanagement.domain.repository;

import com.osci.contractmanagement.domain.model.company.Company;

import java.util.Optional;

public interface CompanyRepository {
    Company save(Company company);
    Optional<Company> findById(Long id);
    Optional<Company> findByName(String name);
    void deleteById(Long id);
}
