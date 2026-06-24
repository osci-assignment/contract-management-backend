package com.osci.contractmanagement.domain.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private static final UserStatus DEFAULT_USER_STATUS = UserStatus.PENDING;


    public static User create(String email, String password) {
        return User.builder()
                .email(email)
                .password(password)
                .role(UserRole.WORKER)
                .status(DEFAULT_USER_STATUS)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static User createAdmin(String email, String password) {
        final User user = User.builder()
                .email(email)
                .password(password)
                .role(UserRole.ADMIN)
                .status(DEFAULT_USER_STATUS)
                .createdAt(LocalDateTime.now())
                .build();
        user.approve();
        return user;
    }

    public void approve() {
        this.status = UserStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = UserStatus.REJECTED;
    }

    public boolean isApproved() {
        return this.status == UserStatus.APPROVED;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public void delete() { this.deletedAt = LocalDateTime.now();}
}