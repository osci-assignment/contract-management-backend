package com.osci.contractmanagement.domain.repository;


import com.osci.contractmanagement.domain.model.User;
import com.osci.contractmanagement.domain.model.UserStatus;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByIdAndDeletedAtIsNull(Long userId);
    Optional<User> findByEmailAndDeletedAtIsNullAndStatus(String email, UserStatus status);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
}
