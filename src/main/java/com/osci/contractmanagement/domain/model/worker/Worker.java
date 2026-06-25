package com.osci.contractmanagement.domain.model.worker;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Worker는 관리자가 승인된 User(WORKER 역할)에게 부여하는 작업자 프로필이다.
 * User는 ID로만 참조한다 (애그리거트 간 객체 참조를 피해 TransientObjectException 등의
 * 트랜잭션 타이밍 문제를 구조적으로 차단함 - Contract/Company와 동일한 패턴).
 */
@Entity
@Table(name = "worker")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String department;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Worker create(Long userId, String name, String position, String department) {
        return Worker.builder()
                .userId(userId)
                .name(name)
                .position(position)
                .department(department)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void update(String name, String position, String department) {
        this.name = name;
        this.position = position;
        this.department = department;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
