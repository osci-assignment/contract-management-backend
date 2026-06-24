package com.osci.contractmanagement.presentation;

import com.osci.contractmanagement.application.dto.request.company.UpdateCompanyRequestDto;
import com.osci.contractmanagement.application.dto.response.company.CompanyDetailResponseDto;
import com.osci.contractmanagement.application.dto.response.company.CompanyResponseDto;
import com.osci.contractmanagement.application.service.company.CompanyUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {
    private final CompanyUseCase companyUseCase;

    public CompanyController(CompanyUseCase companyUseCase) {
        this.companyUseCase = companyUseCase;
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<CompanyResponseDto>>> getCompanies(@PageableDefault(size = 10) Pageable pageable) {
        Page<CompanyResponseDto> response = companyUseCase.getCompanies(pageable);
        return CommonResponse.ok(response);
    }

    @GetMapping("{companyId}")
    public ResponseEntity<CommonResponse<CompanyDetailResponseDto>> getCompany(
            @PathVariable Long companyId
    ) {
        CompanyDetailResponseDto response = companyUseCase.getCompany(companyId);
        return CommonResponse.ok(response);
    }

    @PutMapping("{companyId}")
    public ResponseEntity<CommonResponse<CompanyDetailResponseDto>> updateCompany(
            @PathVariable Long companyId,
            @RequestBody @Valid UpdateCompanyRequestDto request
            ) {
        CompanyDetailResponseDto response = companyUseCase.updateCompany(companyId, request);
        return CommonResponse.ok(response);
    }

    @DeleteMapping("{companyId}")
    public ResponseEntity<CommonResponse<Boolean>> deleteCompany(
            @PathVariable Long companyId
    ) {
        companyUseCase.deleteCompany(companyId);
        return CommonResponse.ok(true);
    }
}
