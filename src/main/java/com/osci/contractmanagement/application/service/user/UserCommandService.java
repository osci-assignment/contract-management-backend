package com.osci.contractmanagement.application.service.user;

import com.osci.contractmanagement.application.dto.request.user.CreateAdminUserRequestDto;
import com.osci.contractmanagement.application.dto.request.user.CreateUserRequestDto;
import com.osci.contractmanagement.application.dto.response.user.UserResponseDto;
import com.osci.contractmanagement.application.exceptions.BusinessException;
import com.osci.contractmanagement.application.exceptions.BusinessExceptionType;
import com.osci.contractmanagement.domain.model.user.User;
import com.osci.contractmanagement.domain.model.user.UserStatus;
import com.osci.contractmanagement.domain.repository.UserRepository;
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

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<UserResponseDto> getUsers(UserStatus status, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<User> users = status != null
                ? userRepository.findByStatusAndDeletedAtIsNull(status, pageable)
                : userRepository.findByDeletedAtIsNull(pageable);
        return users.map(UserResponseDto::from);
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
        userRepository.findByEmailAndDeletedAtIsNull(email).map(user -> {
            throw new BusinessException(BusinessExceptionType.DUPLICATE_EMAIL);
        });
    }

    @Transactional
    public UserResponseDto approveUser(Long loginUserId, Long targetId) {
        final User user = getUserFromId(loginUserId);
        validateAdminUser(user);

        final User target = getUserFromId(targetId);
        if(!target.isApproved()) {
            target.approve();
            userRepository.save(target);
        }

        return UserResponseDto.from(target);
    }

    @Transactional
    public UserResponseDto rejectUser(Long loginUserId, Long targetId) {
        final User user = getUserFromId(loginUserId);
        validateAdminUser(user);

        final User target = getUserFromId(targetId);
        target.reject();
        userRepository.save(target);

        return UserResponseDto.from(target);
    }

    public void validateAdminUser(User user) {
        if (!user.isAdmin()) {
            throw new BusinessException(BusinessExceptionType.FORBIDDEN);
        }
    }

    @Transactional(readOnly = true)
    public User getActiveUserFromEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNullAndStatus(email, UserStatus.APPROVED).orElseThrow(() -> new BusinessException(BusinessExceptionType.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public User getUserFromId(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new BusinessException(BusinessExceptionType.USER_NOT_FOUND));
    }

    @Transactional
    public void deleteUser(Long id) {
        final User user = getUserFromId(id);
        user.delete();
        userRepository.save(user);
    }
}
