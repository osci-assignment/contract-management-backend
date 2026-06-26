package com.osci.contractmanagement.presentation;


import com.osci.contractmanagement.application.dto.response.contract.ContractFileData;
import com.osci.contractmanagement.application.dto.response.contract.ContractResponseDto;
import com.osci.contractmanagement.application.dto.response.contract.ContractUploadResponseDto;
import com.osci.contractmanagement.application.service.contract.ContractUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Contract", description = "계약서 업로드 및 OCR/LLM 처리 결과 조회 API")
@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private final ContractUseCase contractUseCase;

    public ContractController(ContractUseCase contractUseCase) {
        this.contractUseCase = contractUseCase;
    }

    /**
     * 여러 파일을 한 번에 업로드한다. 파일 단위 성공/실패 처리 정책은
     * ContractUseCase(Facade)가 결정하며, Controller는 호출과 응답 변환만 한다.
     */
    @Operation(
            summary = "계약서 다중 업로드",
            description = "PDF 또는 이미지(수기 계약서) 파일을 여러 개 한 번에 업로드한다. " +
                    "각 파일은 PENDING 상태로 저장된 후 Kafka를 통해 비동기로 OCR/LLM 처리되어 " +
                    "업체/프로젝트가 자동 생성된다. 파일 하나가 실패해도 나머지 파일은 계속 처리된다(부분 성공 허용). " +
                    "관리자 전용."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 접수 성공 (파일별 결과는 응답 리스트의 ocrStatus로 구분: PENDING/UPLOAD_FAILED)"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "413", description = "OSCI9002: 업로드 가능한 파일 크기를 초과함",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @RequestBody(content = @Content(mediaType = "multipart/form-data"))
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<List<ContractUploadResponseDto>>> upload(
            @Parameter(description = "업로드할 계약서 파일 목록 (PDF 또는 이미지)")
            @RequestParam("files") List<MultipartFile> files
    ) {
        List<ContractUploadResponseDto> response = contractUseCase.uploadContracts(files);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "계약서 목록 조회",
            description = "OCR 처리 상태(PENDING/PROCESSING/COMPLETED/FAILED)별로 필터링해 계약서 목록을 조회한다. " +
                    "기본적으로 등록일 최신순으로 정렬된다. 관리자 전용."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Page<ContractResponseDto>>> getContracts(
            @Parameter(description = "조회할 OCR 처리 상태 (생략 시 전체 조회)")
            @RequestParam(required = false) com.osci.contractmanagement.domain.model.company.OcrStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ContractResponseDto> response = contractUseCase.getContracts(status, pageable);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "계약서 상세 조회",
            description = "계약서 1건의 상세 정보(업체, 프로젝트, OCR 상태, 실패 사유 등)를 조회한다. 관리자 전용."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI2003: 계약서를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/{contractId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<ContractResponseDto>> getContract(
            @Parameter(description = "계약서 ID") @PathVariable Long contractId) {
        ContractResponseDto response = contractUseCase.getContract(contractId);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "계약서 원본 파일 다운로드",
            description = "업로드 당시의 원본 파일을 그대로 다운로드한다. 응답 Content-Disposition 헤더에 원본 파일명이 담긴다. 관리자 전용.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "다운로드 성공",
                            content = @Content(mediaType = "application/octet-stream",
                                    schema = @Schema(type = "string", format = "binary"))
                    ),
                    @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "404", description = "OSCI2003: 계약서 또는 원본 파일을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    @GetMapping("/{contractId}/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadContract(
            @Parameter(description = "계약서 ID") @PathVariable Long contractId) {
        ContractFileData fileData = contractUseCase.getContractFile(contractId);

        String encodedFilename = java.net.URLEncoder.encode(fileData.getOriginalFilename(), java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(fileData.getContentType()))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .body(fileData.getFileBytes());
    }
}