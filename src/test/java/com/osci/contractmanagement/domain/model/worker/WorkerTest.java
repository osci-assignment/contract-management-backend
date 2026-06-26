package com.osci.contractmanagement.domain.model.worker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkerTest {

    @Test
    @DisplayName("create로 생성하면 입력한 값으로 채워지고 삭제되지 않은 상태다")
    void create_success() {
        Worker worker = Worker.create(1L, "김민준", "대리", "개발팀");

        assertThat(worker.getUserId()).isEqualTo(1L);
        assertThat(worker.getName()).isEqualTo("김민준");
        assertThat(worker.getPosition()).isEqualTo("대리");
        assertThat(worker.getDepartment()).isEqualTo("개발팀");
        assertThat(worker.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("update 호출 시 이름/직책/부서가 변경된다")
    void update_changesFields() {
        Worker worker = Worker.create(1L, "김민준", "대리", "개발팀");

        worker.update("김민준", "과장", "기획팀");

        assertThat(worker.getPosition()).isEqualTo("과장");
        assertThat(worker.getDepartment()).isEqualTo("기획팀");
        assertThat(worker.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("delete 호출 시 isDeleted가 true가 된다")
    void delete_marksDeleted() {
        Worker worker = Worker.create(1L, "김민준", "대리", "개발팀");

        worker.delete();

        assertThat(worker.isDeleted()).isTrue();
    }
}
