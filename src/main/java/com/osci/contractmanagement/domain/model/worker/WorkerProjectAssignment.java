package com.osci.contractmanagement.domain.model.worker;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Worker - Project의 배정 관계를 표현하는 독립 엔티티.
 * Project는 Company 애그리거트에 속해 있어 여기서도 ID로만 참조한다.
 */
@Entity
@Table(name = "worker_project_assignment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"worker_id", "project_id"}))
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
public class WorkerProjectAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "worker_id", nullable = false)
    private Long workerId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static WorkerProjectAssignment create(Long workerId, Long projectId) {
        return WorkerProjectAssignment.builder()
                .workerId(workerId)
                .projectId(projectId)
                .assignedAt(LocalDateTime.now())
                .build();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
