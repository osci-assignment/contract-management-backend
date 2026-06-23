package com.sparta.contractmanagement.infrastructure.repository;

import com.sparta.contractmanagement.domain.model.User;
import com.sparta.contractmanagement.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long>, UserRepository {
}
