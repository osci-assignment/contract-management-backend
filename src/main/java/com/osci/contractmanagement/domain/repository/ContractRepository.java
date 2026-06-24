package com.osci.contractmanagement.domain.repository;

import com.osci.contractmanagement.domain.model.contract.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ContractRepository {

    Contract save(Contract contract);

    Optional<Contract> findById(Long id);

    Page<Contract> findAllByDeletedAtIsNull(Pageable pageable);
}
