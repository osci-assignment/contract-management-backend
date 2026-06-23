package com.sparta.contractmanagement.application.service.user;

import com.sparta.contractmanagement.application.dto.request.auth.LoginUserDto;
import com.sparta.contractmanagement.application.dto.request.user.CreateAdminUserRequestDto;
import com.sparta.contractmanagement.application.dto.request.user.CreateUserRequestDto;
import com.sparta.contractmanagement.application.dto.response.auth.TokenResponseDto;
import com.sparta.contractmanagement.application.dto.response.user.UserResponseDto;
import org.springframework.stereotype.Service;

@Service
public class UserServiceFacade implements UserService {
    private final UserCommandService userCommandService;
    private final AuthService authService;

    public UserServiceFacade(UserCommandService userCommandService, AuthService authService) {
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
    public TokenResponseDto login(LoginUserDto request) {
        return authService.login(request);
    }
}
