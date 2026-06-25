package com.osci.contractmanagement.domain.repository;

import com.osci.contractmanagement.domain.model.company.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 쓰기(생성/수정)는 Company 애그리거트(registerProject/updateProject)를 통해서만 한다.
 * 이 Repository는 조회 전용 - Project 자체가 실제 @Entity로 매핑되어 있어
 * 효율적인 목록/단건 조회를 위해 별도로 둔다 (쓰기 메서드는 의도적으로 없음).
 */
public interface ProjectRepository {
    Optional<Project> findByIdAndDeletedAtIsNull(Long id);
    Page<Project> findByDeletedAtIsNull(Pageable pageable);
}
