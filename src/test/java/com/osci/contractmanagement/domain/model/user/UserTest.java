package com.osci.contractmanagement.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("create로 만든 유저는 WORKER 역할, PENDING 상태로 시작한다")
    void create_defaultsToPendingWorker() {
        User user = User.create("worker@osci.com", "encodedPw");

        assertThat(user.getRole()).isEqualTo(UserRole.WORKER);
        assertThat(user.isApproved()).isFalse();
        assertThat(user.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("createAdmin으로 만든 유저는 ADMIN 역할이며 즉시 승인된다")
    void createAdmin_autoApproved() {
        User admin = User.createAdmin("admin@osci.com", "encodedPw");

        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(admin.isAdmin()).isTrue();
        assertThat(admin.isApproved()).isTrue();
        assertThat(admin.getApprovedAt()).isNotNull();
    }

    @Test
    @DisplayName("approve 호출 시 상태가 APPROVED로 바뀌고 approvedAt이 기록된다")
    void approve_changesStatus() {
        User user = User.create("worker@osci.com", "encodedPw");

        user.approve();

        assertThat(user.isApproved()).isTrue();
        assertThat(user.getApprovedAt()).isNotNull();
    }

    @Test
    @DisplayName("reject 호출 시 상태가 REJECTED로 바뀌고 isApproved는 false다")
    void reject_changesStatus() {
        User user = User.create("worker@osci.com", "encodedPw");

        user.reject();

        assertThat(user.getStatus()).isEqualTo(UserStatus.REJECTED);
        assertThat(user.isApproved()).isFalse();
    }

    @Test
    @DisplayName("delete 호출 시 deletedAt이 기록된다")
    void delete_marksDeletedAt() {
        User user = User.create("worker@osci.com", "encodedPw");

        user.delete();

        assertThat(user.getDeletedAt()).isNotNull();
    }
}
