package com.sparta.contractmanagement.application.service.user;

import com.sparta.contractmanagement.application.dto.request.auth.LoginUserDto;
import com.sparta.contractmanagement.application.dto.request.user.CreateAdminUserRequestDto;
import com.sparta.contractmanagement.application.dto.request.user.CreateUserRequestDto;
import com.sparta.contractmanagement.application.dto.response.auth.TokenResponseDto;
import com.sparta.contractmanagement.application.dto.response.user.UserResponseDto;

public interface UserService {
    UserResponseDto createWorker(CreateUserRequestDto request);
    UserResponseDto createAdmin(CreateAdminUserRequestDto request);
    TokenResponseDto login(LoginUserDto request);
}
