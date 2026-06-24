package com.osci.contractmanagement.presentation;

import com.osci.contractmanagement.application.dto.request.auth.LoginUserRequestDto;
import com.osci.contractmanagement.application.dto.request.user.CreateAdminUserRequestDto;
import com.osci.contractmanagement.application.dto.request.user.CreateUserRequestDto;
import com.osci.contractmanagement.application.dto.response.auth.TokenResponseDto;
import com.osci.contractmanagement.application.dto.response.user.UserResponseDto;
import com.osci.contractmanagement.application.service.user.UserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping
    public ResponseEntity<CommonResponse<UserResponseDto>> createUser(@RequestBody @Valid CreateUserRequestDto request) {
        UserResponseDto response = userUseCase.createWorker(request);
        return CommonResponse.ok(response);
    }

    @PostMapping("/admin")
    public ResponseEntity<CommonResponse<UserResponseDto>> createAdminUser(@RequestBody @Valid CreateAdminUserRequestDto request) {
        UserResponseDto response = userUseCase.createAdmin(request);
        return CommonResponse.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<TokenResponseDto>> login(@RequestBody @Valid LoginUserRequestDto request) {
        TokenResponseDto response = userUseCase.login(request);
        return CommonResponse.ok(response);
    }

    @PostMapping("/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<UserResponseDto>> approve(
            @AuthenticationPrincipal(expression = "userId") Long loginUserId,
            @PathVariable(value = "userId") Long targetId) {
        UserResponseDto response = userUseCase.approveUser(loginUserId, targetId);
        return CommonResponse.ok(response);
    }

    @PostMapping("/{userId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<UserResponseDto>> reject(
            @AuthenticationPrincipal(expression = "userId") Long loginUserId,
            @PathVariable(value = "userId") Long targetId) {
        UserResponseDto response = userUseCase.rejectUser(loginUserId, targetId);
        return CommonResponse.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<CommonResponse<Boolean>> deleteUser(@AuthenticationPrincipal(expression = "userId") Long userId) {
        userUseCase.deleteUser(userId);
        return CommonResponse.ok(Boolean.TRUE);
    }
}
