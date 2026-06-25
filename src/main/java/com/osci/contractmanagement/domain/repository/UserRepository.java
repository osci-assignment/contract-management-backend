package com.osci.contractmanagement.domain.repository;


import com.osci.contractmanagement.domain.model.user.User;
import com.osci.contractmanagement.domain.model.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByIdAndDeletedAtIsNull(Long userId);
    Optional<User> findByEmailAndDeletedAtIsNullAndStatus(String email, UserStatus status);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    Page<User> findByStatusAndDeletedAtIsNull(UserStatus status, Pageable pageable);
    Page<User> findByDeletedAtIsNull(Pageable pageable);
}
