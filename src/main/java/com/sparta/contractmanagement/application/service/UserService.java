package com.sparta.contractmanagement.application.service;

import com.sparta.contractmanagement.application.dto.request.CreateUserRequestDto;
import com.sparta.contractmanagement.application.dto.response.UserResponseDto;
import com.sparta.contractmanagement.application.exceptions.BusinessException;
import com.sparta.contractmanagement.application.exceptions.BusinessExceptionType;
import com.sparta.contractmanagement.domain.model.User;
import com.sparta.contractmanagement.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponseDto createWorker(CreateUserRequestDto request) {
        validateDuplicateEmail(request.getEmail());

        final User user = User.create(request.getEmail(), request.getPassword());
        userRepository.save(user);

        return UserResponseDto.from(user);
    }

    private void validateDuplicateEmail(String email) {
        userRepository.findByEmail(email).map(user -> {
            throw new BusinessException(BusinessExceptionType.DUPLICATE_EMAIL);
        });
    }
}
