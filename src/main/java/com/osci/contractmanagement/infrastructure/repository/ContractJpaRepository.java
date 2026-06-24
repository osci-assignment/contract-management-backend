package com.osci.contractmanagement.infrastructure.repository;

import com.osci.contractmanagement.domain.model.contract.Contract;
import com.osci.contractmanagement.domain.repository.ContractRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractJpaRepository extends ContractRepository, JpaRepository<Contract, Long> {
    @EntityGraph(attributePaths = {"company", "project"})
    Page<Contract> findAllByDeletedAtIsNull(Pageable pageable);
}
