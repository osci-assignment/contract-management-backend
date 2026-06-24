package com.osci.contractmanagement.application.service.company;

import com.osci.contractmanagement.application.dto.request.company.UpdateCompanyRequestDto;
import com.osci.contractmanagement.application.dto.response.company.CompanyDetailResponseDto;
import com.osci.contractmanagement.application.dto.response.company.CompanyResponseDto;
import com.osci.contractmanagement.application.exceptions.BusinessException;
import com.osci.contractmanagement.application.exceptions.BusinessExceptionType;
import com.osci.contractmanagement.domain.model.company.Company;
import com.osci.contractmanagement.domain.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService implements CompanyUseCase{
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponseDto> getCompanies(Pageable pageable) {
        Page<Company> companies = companyRepository.findByDeletedAtIsNull(pageable);
        return companies.map(CompanyResponseDto::of);
    }

    @Transactional(readOnly = true)
    public CompanyDetailResponseDto getCompany(Long companyId) {
        Company company = findCompany(companyId);

        return CompanyDetailResponseDto.of(company);
    }

    @Transactional
    public CompanyDetailResponseDto updateCompany(Long companyId, UpdateCompanyRequestDto request) {
        Company company = findCompany(companyId);
        company.update(request.getName(), request.getContractType());

        return getCompany(companyId);
    }

    @Transactional
    public void deleteCompany(Long companyId) {
        Company company = findCompany(companyId);
        company.delete();
    }

    private Company findCompany(Long companyId) {
        return companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(BusinessExceptionType.COMPANY_NOT_FOUND));
    }
}
