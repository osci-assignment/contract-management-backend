package com.osci.contractmanagement.domain.model.contract;

import com.osci.contractmanagement.domain.model.company.Company;
import com.osci.contractmanagement.domain.model.company.OcrStatus;
import com.osci.contractmanagement.domain.model.company.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ocr_status", nullable = false)
    private OcrStatus ocrStatus;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "extracted_company_name")
    private String extractedCompanyName;

    @Column(name = "extracted_start_date")
    private LocalDate extractedStartDate;

    @Column(name = "extracted_end_date")
    private LocalDate extractedEndDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Contract create(String fileUrl, String contentType) {
        return Contract.builder()
                .fileUrl(fileUrl)
                .contentType(contentType)
                .ocrStatus(OcrStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void markProcessing() {
        this.ocrStatus = OcrStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete(Long companyId, Long projectId, String extractedText, String extractedCompanyName,
                         LocalDate startDate, LocalDate endDate) {
        this.companyId = companyId;
        this.projectId = projectId;
        this.ocrStatus = OcrStatus.COMPLETED;
        this.extractedText = extractedText;
        this.extractedCompanyName = extractedCompanyName;
        this.extractedStartDate = startDate;
        this.extractedEndDate = endDate;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed(String failureReason) {
        this.ocrStatus = OcrStatus.FAILED;
        this.failureReason = failureReason;
        this.updatedAt = LocalDateTime.now();
    }

    public void correctManually(Long companyId, Long projectId, String companyName,
                                LocalDate startDate, LocalDate endDate) {
        this.companyId = companyId;
        this.projectId = projectId;
        this.extractedCompanyName = companyName;
        this.extractedStartDate = startDate;
        this.extractedEndDate = endDate;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}