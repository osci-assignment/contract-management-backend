package com.sparta.contractmanagement.application.service.user;

import com.sparta.contractmanagement.application.dto.request.user.CreateAdminUserRequestDto;
import com.sparta.contractmanagement.application.dto.request.user.CreateUserRequestDto;
import com.sparta.contractmanagement.application.dto.response.user.UserResponseDto;
import com.sparta.contractmanagement.application.exceptions.BusinessException;
import com.sparta.contractmanagement.application.exceptions.BusinessExceptionType;
import com.sparta.contractmanagement.domain.model.User;
import com.sparta.contractmanagement.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserCommandService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserCommandService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDto createWorker(CreateUserRequestDto request) {
        validateDuplicateEmail(request.getEmail());

        final String encodedPassword = passwordEncoder.encode(request.getPassword());
        final User user = User.create(request.getEmail(), encodedPassword);
        userRepository.save(user);

        return UserResponseDto.from(user);
    }

    @Transactional
    public UserResponseDto createAdmin(CreateAdminUserRequestDto request) {
        validateDuplicateEmail(request.getEmail());

        final String encodedPassword = passwordEncoder.encode(request.getPassword());
        final User user = User.createAdmin(request.getEmail(), encodedPassword);
        userRepository.save(user);

        return UserResponseDto.from(user);
    }

    private void validateDuplicateEmail(String email) {
        userRepository.findByEmail(email).map(user -> {
            throw new BusinessException(BusinessExceptionType.DUPLICATE_EMAIL);
        });
    }

    public User getUserFromEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new BusinessException(BusinessExceptionType.USER_NOT_FOUND));
    }
}
