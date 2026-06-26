package com.osci.contractmanagement.presentation;

import com.osci.contractmanagement.application.dto.request.auth.LoginUserRequestDto;
import com.osci.contractmanagement.application.dto.request.auth.RefreshTokenRequestDto;
import com.osci.contractmanagement.application.dto.request.user.CreateAdminUserRequestDto;
import com.osci.contractmanagement.application.dto.request.user.CreateUserRequestDto;
import com.osci.contractmanagement.application.dto.response.auth.TokenResponseDto;
import com.osci.contractmanagement.application.dto.response.user.UserResponseDto;
import com.osci.contractmanagement.application.service.user.UserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "회원 가입 / 로그인 / 승인 관리 API")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @Operation(
            summary = "유저 목록 조회",
            description = "가입 상태(PENDING/APPROVED/REJECTED)별로 유저 목록을 조회한다. status를 비워두면 전체를 조회한다. 관리자 전용."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<org.springframework.data.domain.Page<UserResponseDto>>> getUsers(
            @Parameter(description = "조회할 가입 상태 (생략 시 전체 조회)")
            @org.springframework.web.bind.annotation.RequestParam(required = false)
            com.osci.contractmanagement.domain.model.user.UserStatus status,
            @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable
    ) {
        org.springframework.data.domain.Page<UserResponseDto> response = userUseCase.getUsers(status, pageable);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "일반 유저 가입",
            description = "WORKER 역할로 가입 신청한다. 가입 즉시는 PENDING 상태이며, 관리자 승인 후 로그인할 수 있다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가입 성공"),
            @ApiResponse(responseCode = "400", description = "OSCI9001: 입력값 검증 실패 (이메일 형식, 비밀번호 길이 등)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "409", description = "OSCI1002: 이미 등록된 이메일",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CommonResponse<UserResponseDto>> createUser(@RequestBody @Valid CreateUserRequestDto request) {
        UserResponseDto response = userUseCase.createWorker(request);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "관리자 가입",
            description = "ADMIN 역할로 가입한다. 가입과 동시에 자동으로 승인 처리되어 즉시 로그인할 수 있다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가입 성공"),
            @ApiResponse(responseCode = "400", description = "OSCI9001: 입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "409", description = "OSCI1002: 이미 등록된 이메일",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PostMapping("/admin")
    public ResponseEntity<CommonResponse<UserResponseDto>> createAdminUser(@RequestBody @Valid CreateAdminUserRequestDto request) {
        UserResponseDto response = userUseCase.createAdmin(request);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "로그인",
            description = "이메일/비밀번호로 로그인한다. 승인(APPROVED) 상태인 유저만 로그인할 수 있으며, accessToken과 refreshToken을 발급한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "OSCI1003: 비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI1001: 승인된 유저를 찾을 수 없음 (가입 안 했거나, 아직 미승인/거절 상태)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<TokenResponseDto>> login(@RequestBody @Valid LoginUserRequestDto request) {
        TokenResponseDto response = userUseCase.login(request);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "토큰 재발급",
            description = "refreshToken으로 accessToken/refreshToken을 재발급한다(rotation). " +
                    "accessToken을 잘못 넣으면 토큰의 type 클레임 검증에 의해 거부된다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "OSCI1005: 토큰이 유효하지 않음 (서명/만료 실패, 또는 accessToken을 refreshToken 자리에 넣은 경우)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI1001: 토큰의 유저가 더 이상 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<TokenResponseDto>> refresh(@RequestBody @Valid RefreshTokenRequestDto request) {
        TokenResponseDto response = userUseCase.refresh(request);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "회원 가입 승인",
            description = "PENDING 상태인 유저를 APPROVED로 전환한다. 관리자 전용."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI1001: 대상 유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PostMapping("/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<UserResponseDto>> approve(
            @AuthenticationPrincipal(expression = "userId") Long loginUserId,
            @Parameter(description = "승인할 대상 유저 ID") @PathVariable(value = "userId") Long targetId) {
        UserResponseDto response = userUseCase.approveUser(loginUserId, targetId);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "회원 가입 거절",
            description = "PENDING 상태인 유저를 REJECTED로 전환한다. 관리자 전용."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거절 처리 성공"),
            @ApiResponse(responseCode = "403", description = "OSCI1004: 관리자 권한이 아님",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "404", description = "OSCI1001: 대상 유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @PostMapping("/{userId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<UserResponseDto>> reject(
            @AuthenticationPrincipal(expression = "userId") Long loginUserId,
            @Parameter(description = "거절할 대상 유저 ID") @PathVariable(value = "userId") Long targetId) {
        UserResponseDto response = userUseCase.rejectUser(loginUserId, targetId);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "본인 계정 탈퇴",
            description = "로그인한 본인 계정을 soft delete 처리한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 토큰이 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @DeleteMapping
    public ResponseEntity<CommonResponse<Boolean>> deleteUser(@AuthenticationPrincipal(expression = "userId") Long userId) {
        userUseCase.deleteUser(userId);
        return CommonResponse.ok(Boolean.TRUE);
    }
}