package com.osci.contractmanagement.presentation;

import com.osci.contractmanagement.application.dto.request.worker.CreateWorkerRequestDto;
import com.osci.contractmanagement.application.dto.request.worker.UpdateWorkerRequestDto;
import com.osci.contractmanagement.application.dto.response.worker.WorkerProjectResponseDto;
import com.osci.contractmanagement.application.dto.response.worker.WorkerResponseDto;
import com.osci.contractmanagement.application.service.worker.WorkerUseCase;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Worker", description = "작업자(외주 인력) 관리 및 프로젝트 배정 API")
@RestController
@RequestMapping("/api/v1")
public class WorkerController {

    private final WorkerUseCase workerUseCase;

    public WorkerController(WorkerUseCase workerUseCase) {
        this.workerUseCase = workerUseCase;
    }

    // ===== 본인(일반 유저, 승인된 WORKER) =====

    @Operation(
            summary = "본인 작업자 프로필 등록",
            description = "승인(APPROVED)된 유저 본인이 이름/직책/부서를 입력해 작업자 프로필을 등록한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "OSCI3003: 승인되지 않은 유저",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI1001: 유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "409", description = "OSCI3002: 이미 작업자로 등록된 유저",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PostMapping("/workers/me")
    public ResponseEntity<CommonResponse<WorkerResponseDto>> createMyWorkerProfile(
            @AuthenticationPrincipal(expression = "userId") Long loginUserId,
            @RequestBody @Valid CreateWorkerRequestDto request
    ) {
        WorkerResponseDto response = workerUseCase.createMyWorkerProfile(loginUserId, request);
        return CommonResponse.ok(response);
    }

    @Operation(summary = "본인 작업자 프로필 수정", description = "로그인한 본인의 이름/직책/부서를 수정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "OSCI3001: 작업자 프로필이 없음 (아직 등록 안 한 경우)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PutMapping("/workers/me")
    public ResponseEntity<CommonResponse<WorkerResponseDto>> updateMyWorkerProfile(
            @AuthenticationPrincipal(expression = "userId") Long loginUserId,
            @RequestBody @Valid UpdateWorkerRequestDto request
    ) {
        WorkerResponseDto response = workerUseCase.updateMyWorkerProfile(loginUserId, request);
        return CommonResponse.ok(response);
    }

    @Operation(summary = "본인에게 배정된 프로젝트 목록", description = "로그인한 작업자 본인에게 배정된 프로젝트 목록을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "OSCI3001: 작업자 프로필이 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/workers/me/projects")
    public ResponseEntity<CommonResponse<List<WorkerProjectResponseDto>>> getMyProjects(
            @AuthenticationPrincipal(expression = "userId") Long loginUserId
    ) {
        List<WorkerProjectResponseDto> response = workerUseCase.getMyProjects(loginUserId);
        return CommonResponse.ok(response);
    }

    // ===== 관리자 (조회 및 프로젝트 매칭) =====

    @Operation(
            summary = "작업자 목록 조회",
            description = "이름으로 부분 검색이 가능하다(keyword). 비워두면 전체 목록을 조회한다. " +
                    "기본적으로 등록일 최신순으로 정렬된다. 관리자 전용."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/workers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Page<WorkerResponseDto>>> getWorkers(
            @Parameter(description = "작업자 이름 부분 검색 키워드 (생략 시 전체 조회)")
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<WorkerResponseDto> response = workerUseCase.getWorkers(keyword, pageable);
        return CommonResponse.ok(response);
    }

    @Operation(summary = "작업자 상세 조회", description = "작업자 1건의 상세 정보를 조회한다. 관리자 전용.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI3001: 작업자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/workers/{workerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<WorkerResponseDto>> getWorker(
            @Parameter(description = "작업자 ID") @PathVariable Long workerId
    ) {
        WorkerResponseDto response = workerUseCase.getWorker(workerId);
        return CommonResponse.ok(response);
    }

    @Operation(summary = "특정 작업자의 배정 프로젝트 목록", description = "관리자가 특정 작업자에게 배정된 프로젝트 목록을 조회한다. 관리자 전용.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI3001: 작업자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/workers/{workerId}/projects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<List<WorkerProjectResponseDto>>> getWorkerProjects(
            @Parameter(description = "작업자 ID") @PathVariable Long workerId
    ) {
        List<WorkerProjectResponseDto> response = workerUseCase.getWorkerProjects(workerId);
        return CommonResponse.ok(response);
    }

    @Operation(summary = "작업자 삭제", description = "작업자를 soft delete 처리한다. 관리자 전용.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI3001: 작업자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @DeleteMapping("/workers/{workerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Boolean>> deleteWorker(
            @Parameter(description = "작업자 ID") @PathVariable Long workerId
    ) {
        workerUseCase.deleteWorker(workerId);
        return CommonResponse.ok(true);
    }

    @Operation(summary = "작업자 정보 관리자 수정", description = "관리자가 작업자의 이름/직책/부서를 직접 수정한다. 관리자 전용.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI3001: 작업자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PutMapping("/workers/{workerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<WorkerResponseDto>> updateWorkerByAdmin(
            @Parameter(description = "작업자 ID") @PathVariable Long workerId,
            @RequestBody @Valid UpdateWorkerRequestDto request
    ) {
        WorkerResponseDto response = workerUseCase.updateWorkerByAdmin(workerId, request);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "작업자를 프로젝트에 배정",
            description = "관리자가 특정 작업자를 특정 프로젝트에 매칭(배정)한다. 관리자 전용."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "배정 성공"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI3001: 작업자를 찾을 수 없음 또는 OSCI2002: 프로젝트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "409", description = "OSCI3004: 이미 해당 프로젝트에 배정된 작업자",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PostMapping("/projects/{projectId}/workers/{workerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Boolean>> assignProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @Parameter(description = "작업자 ID") @PathVariable Long workerId
    ) {
        workerUseCase.assignProject(projectId, workerId);
        return CommonResponse.ok(true);
    }

    @Operation(summary = "작업자 배정 해제", description = "관리자가 특정 프로젝트에서 작업자 배정을 해제한다. 관리자 전용.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "해제 성공"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI3001: 작업자를 찾을 수 없음 또는 OSCI3005: 배정 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @DeleteMapping("/projects/{projectId}/workers/{workerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Boolean>> unassignProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @Parameter(description = "작업자 ID") @PathVariable Long workerId
    ) {
        workerUseCase.unassignProject(projectId, workerId);
        return CommonResponse.ok(true);
    }

    @Operation(summary = "프로젝트별 배정 작업자 목록", description = "특정 프로젝트에 배정된 작업자 목록을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "OSCI2002: 프로젝트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/projects/{projectId}/workers")
    public ResponseEntity<CommonResponse<List<WorkerResponseDto>>> getProjectWorkers(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId
    ) {
        List<WorkerResponseDto> response = workerUseCase.getProjectWorkers(projectId);
        return CommonResponse.ok(response);
    }
}