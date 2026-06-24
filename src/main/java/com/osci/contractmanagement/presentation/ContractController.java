package com.osci.contractmanagement.presentation;


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

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private final ContractUseCase contractUseCase;

    public ContractController(ContractUseCase contractUseCase) {
        this.contractUseCase = contractUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<ContractUploadResponseDto>> upload(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Long contractId = contractUseCase.uploadContract(
                file.getBytes(),
                file.getOriginalFilename(),
                file.getContentType()
        );

        return CommonResponse.ok(ContractUploadResponseDto.of(contractId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Page<ContractResponseDto>>> getContracts(@PageableDefault(size = 10) Pageable pageable) {
        Page<ContractResponseDto> response = contractUseCase.getContracts(pageable);
        return CommonResponse.ok(response);
    }
}
