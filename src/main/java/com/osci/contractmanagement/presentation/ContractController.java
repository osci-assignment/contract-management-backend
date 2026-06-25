package com.osci.contractmanagement.presentation;


import com.osci.contractmanagement.application.dto.response.contract.ContractFileData;
import com.osci.contractmanagement.application.dto.response.contract.ContractResponseDto;
import com.osci.contractmanagement.application.dto.response.contract.ContractUploadResponseDto;
import com.osci.contractmanagement.application.service.contract.ContractUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<List<ContractUploadResponseDto>>> upload(
            @RequestParam("files") List<MultipartFile> files
    ) {
        List<ContractUploadResponseDto> response = contractUseCase.uploadContracts(files);
        return CommonResponse.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Page<ContractResponseDto>>> getContracts(
            @RequestParam(required = false) com.osci.contractmanagement.domain.model.company.OcrStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ContractResponseDto> response = contractUseCase.getContracts(status, pageable);
        return CommonResponse.ok(response);
    }

    @GetMapping("/{contractId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<ContractResponseDto>> getContract(@PathVariable Long contractId) {
        ContractResponseDto response = contractUseCase.getContract(contractId);
        return CommonResponse.ok(response);
    }

    @GetMapping("/{contractId}/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadContract(@PathVariable Long contractId) {
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