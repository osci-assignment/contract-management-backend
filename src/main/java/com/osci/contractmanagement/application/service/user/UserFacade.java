package com.osci.contractmanagement.application.service.user;

import com.osci.contractmanagement.application.dto.request.auth.LoginUserRequestDto;
import com.osci.contractmanagement.application.dto.request.user.CreateAdminUserRequestDto;
import com.osci.contractmanagement.application.dto.request.user.CreateUserRequestDto;
import com.osci.contractmanagement.application.dto.response.auth.TokenResponseDto;
import com.osci.contractmanagement.application.dto.response.user.UserResponseDto;
import org.springframework.stereotype.Service;

@Service
public class UserFacade implements UserUseCase {
    private final UserCommandService userCommandService;
    private final AuthService authService;

    public UserFacade(UserCommandService userCommandService, AuthService authService) {
        this.userCommandService = userCommandService;
        this.authService = authService;
    }

    @Override
    public UserResponseDto createWorker(CreateUserRequestDto request) {
        return userCommandService.createWorker(request);
    }

    @Override
    public UserResponseDto createAdmin(CreateAdminUserRequestDto request) {
        return userCommandService.createAdmin(request);
    }

    @Override
    public TokenResponseDto login(LoginUserRequestDto request) {
        return authService.login(request);
    }

    @Override
    public UserResponseDto approveUser(Long loginUserId, Long targetId) {
        return userCommandService.approveUser(loginUserId, targetId);
    }

    @Override
    public UserResponseDto rejectUser(Long loginUserId, Long targetId) {
        return userCommandService.rejectUser(loginUserId, targetId);
    }

    @Override
    public void deleteUser(Long userId) {
        userCommandService.deleteUser(userId);
    }
}
