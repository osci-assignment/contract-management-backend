package com.osci.contractmanagement.infrastructure.security;

import com.osci.contractmanagement.application.exceptions.BusinessException;
import com.osci.contractmanagement.application.exceptions.BusinessExceptionType;
import com.osci.contractmanagement.domain.model.User;
import com.osci.contractmanagement.domain.model.UserStatus;
import com.osci.contractmanagement.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedAtIsNullAndStatus(username, UserStatus.APPROVED).orElseThrow(() -> new BusinessException(BusinessExceptionType.USER_NOT_FOUND));
        return CustomUserDetails.from(user);
    }
}
