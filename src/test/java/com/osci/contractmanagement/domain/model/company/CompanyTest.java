package com.osci.contractmanagement.domain.model.company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompanyTest {

    @Nested
    @DisplayName("업체 생성")
    class Create {

        @Test
        @DisplayName("name, contractType으로 생성하면 기본 상태(미삭제)로 만들어진다")
        void create_success() {
            Company company = Company.create("오픈소스컨설팅", ContractType.ELECTRONIC);

            assertThat(company.getName()).isEqualTo("오픈소스컨설팅");
            assertThat(company.getContractType()).isEqualTo(ContractType.ELECTRONIC);
            assertThat(company.isDeleted()).isFalse();
            assertThat(company.getProjects()).isEmpty();
        }
    }

    @Nested
    @DisplayName("프로젝트 등록/수정")
    class ProjectManagement {

        @Test
        @DisplayName("registerProject 호출 시 Company의 projects 컬렉션에 추가된다")
        void registerProject_addsToCollection() {
            Company company = Company.create("그린테크", ContractType.ELECTRONIC);

            Project project = company.registerProject(
                    "백오피스 개발 프로젝트",
                    LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 8, 31)
            );

            assertThat(company.getProjects()).hasSize(1).contains(project);
            assertThat(project.getCompany()).isEqualTo(company);
        }

        @Test
        @DisplayName("findProjectById로 등록된 프로젝트를 찾을 수 있다")
        void findProjectById_found() {
            Company company = Company.create("그린테크", ContractType.ELECTRONIC);
            Project project = company.registerProject("프로젝트", LocalDate.now(), LocalDate.now().plusDays(1));
            setId(project, 1L);

            assertThat(company.findProjectById(1L)).contains(project);
        }

        @Test
        @DisplayName("updateProject는 존재하지 않는 projectId면 예외를 던진다")
        void updateProject_notFound_throws() {
            Company company = Company.create("그린테크", ContractType.ELECTRONIC);

            assertThatThrownBy(() ->
                    company.updateProject(999L, "변경", LocalDate.now(), LocalDate.now().plusDays(1)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        // Project.id는 @GeneratedValue라 리플렉션으로 직접 세팅해 테스트한다 (영속화 없이 단위 테스트하기 위함)
        private void setId(Project project, Long id) {
            try {
                var field = Project.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(project, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    @DisplayName("삭제")
    class Delete {

        @Test
        @DisplayName("delete 호출 시 소속된 모든 Project도 같이 삭제 처리된다")
        void delete_cascadesToProjects() {
            Company company = Company.create("그린테크", ContractType.ELECTRONIC);
            Project project = company.registerProject("프로젝트", LocalDate.now(), LocalDate.now().plusDays(1));

            company.delete();

            assertThat(company.isDeleted()).isTrue();
            assertThat(project.isDeleted()).isTrue();
        }
    }
}
