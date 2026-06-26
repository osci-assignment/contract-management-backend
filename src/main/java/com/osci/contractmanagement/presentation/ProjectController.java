package com.osci.contractmanagement.presentation;

import com.osci.contractmanagement.application.dto.request.project.UpdateProjectRequestDto;
import com.osci.contractmanagement.application.dto.response.project.ProjectResponseDto;
import com.osci.contractmanagement.application.service.project.ProjectUseCase;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "Project", description = "프로젝트(업체별 외주 프로젝트) 관리 API")
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectUseCase projectUseCase;

    public ProjectController(ProjectUseCase projectUseCase) {
        this.projectUseCase = projectUseCase;
    }

    @Operation(
            summary = "프로젝트 목록 조회",
            description = "전체 프로젝트 목록을 페이징하여 조회한다. 기본적으로 등록일 최신순으로 정렬된다. 관리자 전용."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Page<ProjectResponseDto>>> getProjects(
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProjectResponseDto> response = projectUseCase.getProjects(pageable);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "프로젝트 상세 조회",
            description = "프로젝트 1건의 상세 정보(소속 업체 포함)를 조회한다. 로그인한 사용자라면 누구나 조회 가능하다 " +
                    "(배정된 작업자가 본인의 프로젝트를 확인할 수 있도록 함)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "OSCI2002: 프로젝트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/{projectId}")
    public ResponseEntity<CommonResponse<ProjectResponseDto>> getProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId
    ) {
        ProjectResponseDto response = projectUseCase.getProject(projectId);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "프로젝트 정보 수정",
            description = "프로젝트명, 시작일, 종료일을 수정한다. 시작일이 종료일보다 늦으면 거부된다. 관리자 전용."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "OSCI9001: 입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI2002: 프로젝트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "시작일이 종료일보다 늦은 경우 등 도메인 검증 실패 " +
                    "(현재 IllegalArgumentException으로 처리되어 OSCI9999/500으로 내려감 - 개선 필요)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<ProjectResponseDto>> updateProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @RequestBody @Valid UpdateProjectRequestDto request
    ) {
        ProjectResponseDto response = projectUseCase.updateProject(projectId, request);
        return CommonResponse.ok(response);
    }
}