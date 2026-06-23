package com.sparta.contractmanagement.domain.repository;


import com.sparta.contractmanagement.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long userId);
    Optional<User> findByEmail(String email);
}
