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
    public Contract createPending(String fileUrl, String contentType) {
        return contractRepository.save(Contract.create(fileUrl, contentType));
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
                company.getName() + " 계약 프로젝트",
                result.getStartDate(),
                result.getEndDate()
        );
        companyRepository.save(company);

        contract.complete(company.getId(), project.getId(), rawText, result.getCompanyName(),
                result.getStartDate(), result.getEndDate());
        // contract는 이 메서드 안에서 조회된 영속 엔티티이므로 dirty checking으로 자동 UPDATE됨
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

        return companyRepository.findByName(companyName)
                .orElseGet(() -> companyRepository.save(
                        Company.create(companyName, ContractType.ELECTRONIC)));
    }

    private Contract getManagedContract(Long contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("계약서를 찾을 수 없습니다. id=" + contractId));
    }
}