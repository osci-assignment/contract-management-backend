package com.osci.contractmanagement.application.service.worker;

import com.osci.contractmanagement.application.dto.request.worker.CreateWorkerRequestDto;
import com.osci.contractmanagement.application.dto.request.worker.UpdateWorkerRequestDto;
import com.osci.contractmanagement.application.dto.response.worker.WorkerProjectResponseDto;
import com.osci.contractmanagement.application.dto.response.worker.WorkerResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WorkerUseCase {

    /** 로그인한 유저 본인이 작업자 프로필(이름/직책/부서)을 등록한다. 승인된 유저만 가능. */
    WorkerResponseDto createMyWorkerProfile(Long loginUserId, CreateWorkerRequestDto request);

    /** 로그인한 유저 본인이 작업자 프로필을 수정한다. */
    WorkerResponseDto updateMyWorkerProfile(Long loginUserId, UpdateWorkerRequestDto request);

    /** 로그인한 유저(작업자) 본인이 배정된 프로젝트 목록 조회 */
    List<WorkerProjectResponseDto> getMyProjects(Long loginUserId);

    /** 관리자가 작업자 목록을 조회 (매칭 대상 선택용) */
    Page<WorkerResponseDto> getWorkers(String keyword, Pageable pageable);

    /** 관리자가 작업자 상세 조회 */
    WorkerResponseDto getWorker(Long workerId);

    /** 관리자가 작업자를 삭제(soft) - 더 이상 작업자가 아니게 처리 */
    void deleteWorker(Long workerId);

    /** 관리자가 작업자 정보(이름/직책/부서)를 직접 수정 */
    WorkerResponseDto updateWorkerByAdmin(Long workerId, UpdateWorkerRequestDto request);

    /** 관리자가 작업자를 프로젝트에 배정(매칭) */
    void assignProject(Long projectId, Long workerId);

    /** 관리자가 배정을 해제 */
    void unassignProject(Long projectId, Long workerId);

    /** 관리자가 특정 프로젝트에 배정된 작업자 목록 조회 */
    List<WorkerResponseDto> getProjectWorkers(Long projectId);

    /** 관리자가 특정 작업자에게 배정된 프로젝트 목록을 조회 */
    List<WorkerProjectResponseDto> getWorkerProjects(Long workerId);
}
