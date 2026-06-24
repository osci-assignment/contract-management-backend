package com.osci.contractmanagement.infrastructure.repository;

import com.osci.contractmanagement.domain.model.User;
import com.osci.contractmanagement.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long>, UserRepository {
}
