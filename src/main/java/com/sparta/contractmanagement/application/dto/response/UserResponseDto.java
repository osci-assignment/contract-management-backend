package com.sparta.contractmanagement.application.dto.response;

import com.sparta.contractmanagement.domain.model.User;
import com.sparta.contractmanagement.domain.model.UserRole;
import com.sparta.contractmanagement.domain.model.UserStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class UserResponseDto {
    private Long userId;
    private String email;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .approvedAt(user.getApprovedAt())
                .build();
    }
}
