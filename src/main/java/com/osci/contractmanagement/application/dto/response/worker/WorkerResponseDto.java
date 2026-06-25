package com.osci.contractmanagement.application.dto.response.worker;

import com.osci.contractmanagement.domain.model.worker.Worker;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class WorkerResponseDto {
    private Long workerId;
    private Long userId;
    private String name;
    private String position;
    private String department;
    private LocalDateTime createdAt;

    public static WorkerResponseDto of(Worker worker) {
        return WorkerResponseDto.builder()
                .workerId(worker.getId())
                .userId(worker.getUserId())
                .name(worker.getName())
                .position(worker.getPosition())
                .department(worker.getDepartment())
                .createdAt(worker.getCreatedAt())
                .build();
    }
}
