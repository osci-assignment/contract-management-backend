package com.osci.contractmanagement.domain.repository;

import com.osci.contractmanagement.domain.model.company.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CompanyRepository {
    Company save(Company company);
    void flush();
    Optional<Company> findByIdAndDeletedAtIsNull(Long id);
    Optional<Company> findByNameAndDeletedAtIsNull(String name);
    Page<Company> findByDeletedAtIsNull(Pageable pageable);
    void deleteById(Long id);

    /**
     * Project는 Company 애그리거트 내부 컬렉션이라 자체 Repository가 없다.
     * projectId만으로 어느 Company에 속하는지 찾기 위한 조회 메서드.
     * (Worker를 Project에 배정할 때 해당 Project가 실제 존재하는지 검증하는 용도)
     */
    Optional<Company> findByProjects_IdAndDeletedAtIsNull(Long projectId);
}
