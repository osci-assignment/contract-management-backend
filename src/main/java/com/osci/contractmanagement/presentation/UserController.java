package com.osci.contractmanagement.presentation;

import com.osci.contractmanagement.application.dto.request.auth.LoginUserDto;
import com.osci.contractmanagement.application.dto.request.user.CreateAdminUserRequestDto;
import com.osci.contractmanagement.application.dto.request.user.CreateUserRequestDto;
import com.osci.contractmanagement.application.dto.response.auth.TokenResponseDto;
import com.osci.contractmanagement.application.dto.response.user.UserResponseDto;
import com.osci.contractmanagement.application.service.user.UserFacade;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserFacade userFacade;

    public UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }


    @PostMapping
    public ResponseEntity<CommonResponse<UserResponseDto>> createUser(@RequestBody @Valid CreateUserRequestDto request) {
        UserResponseDto response = userFacade.createWorker(request);
        return CommonResponse.ok(response);
    }

    @PostMapping("/admin")
    public ResponseEntity<CommonResponse<UserResponseDto>> createAdminUser(@RequestBody @Valid CreateAdminUserRequestDto request) {
        UserResponseDto response = userFacade.createAdmin(request);
        return CommonResponse.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<TokenResponseDto>> login(@RequestBody @Valid LoginUserDto request) {
        TokenResponseDto response = userFacade.login(request);
        return CommonResponse.ok(response);
    }

    @PostMapping("/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<UserResponseDto>> approve(
            @AuthenticationPrincipal(expression = "userId") Long loginUserId,
            @PathVariable(value = "userId") Long targetId) {
        UserResponseDto response = userFacade.approveUser(loginUserId, targetId);
        return CommonResponse.ok(response);
    }

    @PostMapping("/{userId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<UserResponseDto>> reject(
            @AuthenticationPrincipal(expression = "userId") Long loginUserId,
            @PathVariable(value = "userId") Long targetId) {
        UserResponseDto response = userFacade.rejectUser(loginUserId, targetId);
        return CommonResponse.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<CommonResponse<Boolean>> deleteUser(@AuthenticationPrincipal(expression = "userId") Long userId) {
        userFacade.deleteUser(userId);
        return CommonResponse.ok(Boolean.TRUE);
    }
}
