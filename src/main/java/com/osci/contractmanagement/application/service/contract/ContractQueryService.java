package com.osci.contractmanagement.application.service.contract;


import com.osci.contractmanagement.application.dto.response.contract.ContractFileData;
import com.osci.contractmanagement.application.dto.response.contract.ContractResponseDto;
import com.osci.contractmanagement.domain.model.company.Company;
import com.osci.contractmanagement.domain.model.company.Project;
import com.osci.contractmanagement.domain.model.contract.Contract;
import com.osci.contractmanagement.domain.repository.CompanyRepository;
import com.osci.contractmanagement.domain.repository.ContractRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Contract 처리 과정에서 발생하는 모든 쓰기(영속) 작업을 전담한다.
 * Facade는 흐름만 조율하고, 실제 저장/상태 변경 로직은 여기에 모은다.
 */
@Service
public class ContractQueryService {
    private final ContractRepository contractRepository;
    private final CompanyRepository companyRepository;
    private final com.osci.contractmanagement.application.provider.FileStorageProvider fileStorageProvider;

    public ContractQueryService(ContractRepository contractRepository, CompanyRepository companyRepository,
                                com.osci.contractmanagement.application.provider.FileStorageProvider fileStorageProvider) {
        this.contractRepository = contractRepository;
        this.companyRepository = companyRepository;
        this.fileStorageProvider = fileStorageProvider;
    }



    @Transactional(readOnly = true)
    public ContractFileData getContractFile(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new com.osci.contractmanagement.application.exceptions.BusinessException(
                        com.osci.contractmanagement.application.exceptions.BusinessExceptionType.CONTRACT_NOT_FOUND));

        byte[] fileBytes = fileStorageProvider.load(contract.getFileUrl());
        return new ContractFileData(fileBytes, contract.getOriginalFilename(), contract.getContentType());
    }

    @Transactional(readOnly = true)
    public ContractResponseDto getContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new com.osci.contractmanagement.application.exceptions.BusinessException(
                        com.osci.contractmanagement.application.exceptions.BusinessExceptionType.CONTRACT_NOT_FOUND));

        Company company = contract.getCompanyId() != null
                ? companyRepository.findByIdAndDeletedAtIsNull(contract.getCompanyId()).orElse(null)
                : null;
        Project project = (company != null && contract.getProjectId() != null)
                ? company.findProjectById(contract.getProjectId()).orElse(null)
                : null;

        return ContractResponseDto.of(contract, company, project);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponseDto> getContracts(com.osci.contractmanagement.domain.model.company.OcrStatus ocrStatus, Pageable pageable) {
        Page<Contract> contracts = ocrStatus != null
                ? contractRepository.findAllByOcrStatusAndDeletedAtIsNull(ocrStatus, pageable)
                : contractRepository.findAllByDeletedAtIsNull(pageable);

        // companyId별로 한 번씩만 조회 - 같은 페이지 안에 같은 업체의 계약서가 여러 건일 수 있음
        Map<Long, Company> companiesById = findDistinctCompanies(contracts.getContent());

        return contracts.map(contract -> toResponseDto(contract, companiesById));
    }

    private Map<Long, Company> findDistinctCompanies(List<Contract> contracts) {
        List<Long> companyIds = contracts.stream()
                .map(Contract::getCompanyId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        return companyIds.stream()
                .map(companyRepository::findByIdAndDeletedAtIsNull)
                .flatMap(java.util.Optional::stream)
                .collect(Collectors.toMap(Company::getId, Function.identity()));
    }

    private ContractResponseDto toResponseDto(Contract contract, Map<Long, Company> companiesById) {
        Company company = contract.getCompanyId() != null
                ? companiesById.get(contract.getCompanyId())
                : null;

        Project project = (company != null && contract.getProjectId() != null)
                ? company.findProjectById(contract.getProjectId()).orElse(null)
                : null;

        return ContractResponseDto.of(contract, company, project);
    }
}