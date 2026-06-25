package com.osci.contractmanagement.application.service.user;

import com.osci.contractmanagement.application.dto.request.auth.LoginUserRequestDto;
import com.osci.contractmanagement.application.dto.request.user.CreateAdminUserRequestDto;
import com.osci.contractmanagement.application.dto.request.user.CreateUserRequestDto;
import com.osci.contractmanagement.application.dto.response.auth.TokenResponseDto;
import com.osci.contractmanagement.application.dto.response.user.UserResponseDto;
import com.osci.contractmanagement.domain.model.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserUseCase {
    Page<UserResponseDto> getUsers(UserStatus status, Pageable pageable);
    UserResponseDto createWorker(CreateUserRequestDto request);
    UserResponseDto createAdmin(CreateAdminUserRequestDto request);
    TokenResponseDto login(LoginUserRequestDto request);
    UserResponseDto approveUser(Long loginUserId, Long targetId);
    UserResponseDto rejectUser(Long loginUserId, Long targetId);
    void deleteUser(Long userId);
}
