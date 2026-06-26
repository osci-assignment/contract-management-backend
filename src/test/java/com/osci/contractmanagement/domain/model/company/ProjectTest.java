package com.osci.contractmanagement.domain.model.company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectTest {

    private final Company company = Company.create("그린테크", ContractType.ELECTRONIC);

    @Test
    @DisplayName("시작일이 종료일보다 이전이면 정상 생성된다")
    void create_validPeriod_success() {
        Project project = company.registerProject(
                "프로젝트A", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(project.getTitle()).isEqualTo("프로젝트A");
        assertThat(project.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 생성 시 예외가 발생한다")
    void create_invalidPeriod_throws() {
        assertThatThrownBy(() -> company.registerProject(
                "프로젝트A", LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("시작일");
    }

    @Test
    @DisplayName("update도 동일하게 기간 검증을 수행한다")
    void update_invalidPeriod_throws() {
        Project project = company.registerProject(
                "프로젝트A", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThatThrownBy(() -> company.updateProject(
                getId(project), "프로젝트A", LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("delete 호출 시 isDeleted가 true가 된다")
    void delete_marksDeleted() {
        Project project = company.registerProject(
                "프로젝트A", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        company.delete();

        assertThat(project.isDeleted()).isTrue();
    }

    private Long getId(Project project) {
        try {
            var field = Project.class.getDeclaredField("id");
            field.setAccessible(true);
            if (field.get(project) == null) {
                field.set(project, 1L);
            }
            return (Long) field.get(project);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
