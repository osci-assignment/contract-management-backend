package com.osci.contractmanagement.presentation;

import com.osci.contractmanagement.application.dto.request.company.UpdateCompanyRequestDto;
import com.osci.contractmanagement.application.dto.response.company.CompanyDetailResponseDto;
import com.osci.contractmanagement.application.dto.response.company.CompanyResponseDto;
import com.osci.contractmanagement.application.service.company.CompanyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Company", description = "업체(계약 상대방) 관리 API")
@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {
    private final CompanyUseCase companyUseCase;

    public CompanyController(CompanyUseCase companyUseCase) {
        this.companyUseCase = companyUseCase;
    }

    @Operation(
            summary = "업체 목록 조회",
            description = "등록된 업체 목록을 페이징하여 조회한다. 기본적으로 등록일 최신순으로 정렬된다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<CommonResponse<Page<CompanyResponseDto>>> getCompanies(@PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<CompanyResponseDto> response = companyUseCase.getCompanies(pageable);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "업체 상세 조회",
            description = "업체 1건의 상세 정보를 조회한다. 해당 업체에 등록된 프로젝트 목록도 함께 포함된다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "OSCI2001: 업체를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("{companyId}")
    public ResponseEntity<CommonResponse<CompanyDetailResponseDto>> getCompany(
            @Parameter(description = "업체 ID") @PathVariable Long companyId
    ) {
        CompanyDetailResponseDto response = companyUseCase.getCompany(companyId);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "업체 정보 수정",
            description = "업체명, 계약 형태(전자계약/수기계약)를 수정한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "OSCI9001: 입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI2001: 업체를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PutMapping("{companyId}")
    public ResponseEntity<CommonResponse<CompanyDetailResponseDto>> updateCompany(
            @Parameter(description = "업체 ID") @PathVariable Long companyId,
            @RequestBody @Valid UpdateCompanyRequestDto request
    ) {
        CompanyDetailResponseDto response = companyUseCase.updateCompany(companyId, request);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "업체 삭제",
            description = "업체를 soft delete 처리한다. 소속된 프로젝트도 함께 삭제 처리된다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "OSCI2001: 업체를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @DeleteMapping("{companyId}")
    public ResponseEntity<CommonResponse<Boolean>> deleteCompany(
            @Parameter(description = "업체 ID") @PathVariable Long companyId
    ) {
        companyUseCase.deleteCompany(companyId);
        return CommonResponse.ok(true);
    }
}