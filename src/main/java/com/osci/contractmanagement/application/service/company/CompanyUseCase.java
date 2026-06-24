package com.osci.contractmanagement.application.service.company;

import com.osci.contractmanagement.application.dto.request.company.UpdateCompanyRequestDto;
import com.osci.contractmanagement.application.dto.response.company.CompanyDetailResponseDto;
import com.osci.contractmanagement.application.dto.response.company.CompanyResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyUseCase {
    Page<CompanyResponseDto> getCompanies(Pageable pageable);
    CompanyDetailResponseDto getCompany(Long companyId);
    CompanyDetailResponseDto updateCompany(Long companyId, UpdateCompanyRequestDto request);
    void deleteCompany(Long companyId);
}
