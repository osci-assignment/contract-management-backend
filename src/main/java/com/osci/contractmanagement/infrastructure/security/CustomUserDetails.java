package com.osci.contractmanagement.infrastructure.security;

import com.osci.contractmanagement.domain.model.User;
import com.osci.contractmanagement.domain.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Builder(access = lombok.AccessLevel.PRIVATE)
@Getter
public class CustomUserDetails implements UserDetails {
    private Long userId;
    private String email;
    private String password;
    private final UserRole role;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public static CustomUserDetails from(User user) {
        return CustomUserDetails.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
    }

    public static CustomUserDetails of(Long userId, String email, UserRole role) {
        return CustomUserDetails.builder()
                .userId(userId)
                .email(email)
                .password(null)
                .role(role)
                .build();
    }
}
