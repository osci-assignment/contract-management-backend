package com.osci.contractmanagement.application.service.contract;

import com.osci.contractmanagement.application.dto.response.contract.ContractResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContractUseCase {

    Long uploadContract(byte[] fileBytes, String originalFilename, String contentType);
    Page<ContractResponseDto> getContracts(Pageable pageable);

    void processContract(Long contractId);
}
