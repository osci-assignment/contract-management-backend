package com.osci.contractmanagement.presentation;

import com.osci.contractmanagement.application.dto.request.worker.CreateWorkerRequestDto;
import com.osci.contractmanagement.application.dto.request.worker.UpdateWorkerRequestDto;
import com.osci.contractmanagement.application.dto.response.worker.WorkerProjectResponseDto;
import com.osci.contractmanagement.application.dto.response.worker.WorkerResponseDto;
import com.osci.contractmanagement.application.service.worker.WorkerUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class WorkerController {

    private final WorkerUseCase workerUseCase;

    public WorkerController(WorkerUseCase workerUseCase) {
        this.workerUseCase = workerUseCase;
    }

    // ===== 본인(일반 유저, 승인된 WORKER) =====

    /** 승인된 유저 본인이 작업자 프로필(이름/직책/부서)을 등록 */
    @PostMapping("/workers/me")
    public ResponseEntity<CommonResponse<WorkerResponseDto>> createMyWorkerProfile(
            @AuthenticationPrincipal(expression = "userId") Long loginUserId,
            @RequestBody @Valid CreateWorkerRequestDto request
    ) {
        WorkerResponseDto response = workerUseCase.createMyWorkerProfile(loginUserId, request);
        return CommonResponse.ok(response);
    }

    /** 본인 작업자 프로필 수정 */
    @PutMapping("/workers/me")
    public ResponseEntity<CommonResponse<WorkerResponseDto>> updateMyWorkerProfile(
            @AuthenticationPrincipal(expression = "userId") Long loginUserId,
            @RequestBody @Valid UpdateWorkerRequestDto request
    ) {
        WorkerResponseDto response = workerUseCase.updateMyWorkerProfile(loginUserId, request);
        return CommonResponse.ok(response);
    }

    /** 본인이 배정된 프로젝트 목록 */
    @GetMapping("/workers/me/projects")
    public ResponseEntity<CommonResponse<List<WorkerProjectResponseDto>>> getMyProjects(
            @AuthenticationPrincipal(expression = "userId") Long loginUserId
    ) {
        List<WorkerProjectResponseDto> response = workerUseCase.getMyProjects(loginUserId);
        return CommonResponse.ok(response);
    }

    // ===== 관리자 (조회 및 프로젝트 매칭) =====

    @GetMapping("/workers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Page<WorkerResponseDto>>> getWorkers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<WorkerResponseDto> response = workerUseCase.getWorkers(keyword, pageable);
        return CommonResponse.ok(response);
    }

    @GetMapping("/workers/{workerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<WorkerResponseDto>> getWorker(
            @PathVariable Long workerId
    ) {
        WorkerResponseDto response = workerUseCase.getWorker(workerId);
        return CommonResponse.ok(response);
    }

    /** 관리자가 특정 작업자에게 배정된 프로젝트 목록 조회 */
    @GetMapping("/workers/{workerId}/projects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<List<WorkerProjectResponseDto>>> getWorkerProjects(
            @PathVariable Long workerId
    ) {
        List<WorkerProjectResponseDto> response = workerUseCase.getWorkerProjects(workerId);
        return CommonResponse.ok(response);
    }

    @DeleteMapping("/workers/{workerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Boolean>> deleteWorker(
            @PathVariable Long workerId
    ) {
        workerUseCase.deleteWorker(workerId);
        return CommonResponse.ok(true);
    }

    /** 관리자가 작업자 정보를 직접 수정 */
    @PutMapping("/workers/{workerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<WorkerResponseDto>> updateWorkerByAdmin(
            @PathVariable Long workerId,
            @RequestBody @Valid UpdateWorkerRequestDto request
    ) {
        WorkerResponseDto response = workerUseCase.updateWorkerByAdmin(workerId, request);
        return CommonResponse.ok(response);
    }

    /** 관리자가 작업자를 프로젝트에 매칭(배정) */
    @PostMapping("/projects/{projectId}/workers/{workerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Boolean>> assignProject(
            @PathVariable Long projectId,
            @PathVariable Long workerId
    ) {
        workerUseCase.assignProject(projectId, workerId);
        return CommonResponse.ok(true);
    }

    @DeleteMapping("/projects/{projectId}/workers/{workerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Boolean>> unassignProject(
            @PathVariable Long projectId,
            @PathVariable Long workerId
    ) {
        workerUseCase.unassignProject(projectId, workerId);
        return CommonResponse.ok(true);
    }

    @GetMapping("/projects/{projectId}/workers")
    public ResponseEntity<CommonResponse<List<WorkerResponseDto>>> getProjectWorkers(
            @PathVariable Long projectId
    ) {
        List<WorkerResponseDto> response = workerUseCase.getProjectWorkers(projectId);
        return CommonResponse.ok(response);
    }
}