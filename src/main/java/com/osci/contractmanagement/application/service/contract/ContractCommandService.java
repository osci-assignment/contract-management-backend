package com.osci.contractmanagement.application.service.contract;


import com.osci.contractmanagement.application.provider.ContractInfoExtractor.ContractExtractionResult;
import com.osci.contractmanagement.domain.model.company.Company;
import com.osci.contractmanagement.domain.model.company.ContractType;
import com.osci.contractmanagement.domain.model.contract.Contract;
import com.osci.contractmanagement.domain.model.company.Project;
import com.osci.contractmanagement.domain.repository.CompanyRepository;
import com.osci.contractmanagement.domain.repository.ContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;

/**
 * Contract 처리 과정에서 발생하는 모든 쓰기(영속) 작업을 전담한다.
 * Facade는 흐름만 조율하고, 실제 저장/상태 변경 로직은 여기에 모은다.
 */
@Service
public class ContractCommandService {

    private static final int MAX_RETRY_COUNT = 3;

    private final ContractRepository contractRepository;
    private final CompanyRepository companyRepository;

    public ContractCommandService(ContractRepository contractRepository, CompanyRepository companyRepository) {
        this.contractRepository = contractRepository;
        this.companyRepository = companyRepository;
    }


    @Transactional
    public Contract createPending(String fileUrl, String originalFilename, String contentType) {
        return contractRepository.save(Contract.create(fileUrl, originalFilename, contentType));
    }

    @Transactional
    public Contract markProcessing(Long contractId) {
        Contract contract = getManagedContract(contractId);
        contract.markProcessing();
        return contract;
    }

    @Transactional
    public void completeWithExtractionResult(Long contractId, String rawText, ContractExtractionResult result) {
        Contract contract = getManagedContract(contractId);

        Company company = resolveCompany(result.getCompanyName());

        Project project = company.registerProject(
                MessageFormat.format("{0} 프로젝트", company.getName()),
                result.getStartDate(),
                result.getEndDate()
        );

        /*
           Note: Project 가 비영속 상태이고 동일 Transactional 범위 내에 있기에 project.getId() 가 동작하지 않음
           Transactional 범위를 분리하여 진행할 수 있지만 JPA 특성상 객체를 영속 상태로 만들기 위해서는 Repository에 의한 조회가 한번 더 일어나야 함(N+1)
           따라서 우선적으로 성능을 올리기 위해 강제 Flush로 처리
        */
        companyRepository.flush();

        contract.complete(company.getId(), project.getId(), rawText, result.getCompanyName(),
                result.getStartDate(), result.getEndDate());
    }

    @Transactional
    public void markFailed(Long contractId, Exception cause) {
        Contract contract = getManagedContract(contractId);
        contract.markFailed(cause.getMessage());
    }

    private Company resolveCompany(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalStateException("업체명을 추출하지 못했습니다. 수동 입력이 필요합니다.");
        }

        return companyRepository.findByNameAndDeletedAtIsNull(companyName)
                .orElseGet(() -> companyRepository.save(
                        Company.create(companyName, ContractType.ELECTRONIC)));
    }

    private Contract getManagedContract(Long contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("계약서를 찾을 수 없습니다. id=" + contractId));
    }
}