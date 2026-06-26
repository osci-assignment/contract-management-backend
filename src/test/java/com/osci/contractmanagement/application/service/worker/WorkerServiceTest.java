package com.osci.contractmanagement.application.service.worker;

import com.osci.contractmanagement.application.dto.request.worker.CreateWorkerRequestDto;
import com.osci.contractmanagement.application.dto.response.worker.WorkerResponseDto;
import com.osci.contractmanagement.application.exceptions.BusinessException;
import com.osci.contractmanagement.application.exceptions.BusinessExceptionType;
import com.osci.contractmanagement.domain.model.company.Company;
import com.osci.contractmanagement.domain.model.company.ContractType;
import com.osci.contractmanagement.domain.model.user.User;
import com.osci.contractmanagement.domain.model.worker.Worker;
import com.osci.contractmanagement.domain.repository.CompanyRepository;
import com.osci.contractmanagement.domain.repository.UserRepository;
import com.osci.contractmanagement.domain.repository.WorkerProjectAssignmentRepository;
import com.osci.contractmanagement.domain.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerServiceTest {

    @Mock
    private WorkerRepository workerRepository;
    @Mock
    private WorkerProjectAssignmentRepository assignmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyRepository companyRepository;

    private WorkerService workerService;

    @BeforeEach
    void setUp() {
        workerService = new WorkerService(workerRepository, assignmentRepository, userRepository, companyRepository);
    }

    private CreateWorkerRequestDto createWorkerRequest(String name, String position, String department) {
        try {
            var constructor = CreateWorkerRequestDto.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            CreateWorkerRequestDto dto = constructor.newInstance();
            ReflectionTestUtils.setField(dto, "name", name);
            ReflectionTestUtils.setField(dto, "position", position);
            ReflectionTestUtils.setField(dto, "department", department);
            return dto;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("createMyWorkerProfile")
    class CreateMyWorkerProfile {

        @Test
        @DisplayName("승인된 유저이고 아직 작업자가 아니면 작업자 프로필이 생성된다")
        void success() {
            User user = User.create("worker@osci.com", "encodedPw");
            user.approve();
            ReflectionTestUtils.setField(user, "id", 1L);

            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
            when(workerRepository.existsByUserIdAndDeletedAtIsNull(1L)).thenReturn(false);

            WorkerResponseDto response = workerService.createMyWorkerProfile(
                    1L, createWorkerRequest("김민준", "대리", "개발팀"));

            assertThat(response.getName()).isEqualTo("김민준");
            verify(workerRepository).save(any(Worker.class));
        }

        @Test
        @DisplayName("승인되지 않은 유저면 USER_NOT_APPROVED 예외를 던진다")
        void userNotApproved_throws() {
            User user = User.create("worker@osci.com", "encodedPw"); // approve() 호출 안 함 -> PENDING
            ReflectionTestUtils.setField(user, "id", 1L);

            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));

            assertThatThrownBy(() ->
                    workerService.createMyWorkerProfile(1L, createWorkerRequest("김민준", "대리", "개발팀")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getType())
                    .isEqualTo(BusinessExceptionType.USER_NOT_APPROVED);

            verify(workerRepository, never()).save(any());
        }

        @Test
        @DisplayName("이미 작업자로 등록된 유저면 WORKER_ALREADY_EXISTS 예외를 던진다")
        void alreadyExists_throws() {
            User user = User.create("worker@osci.com", "encodedPw");
            user.approve();
            ReflectionTestUtils.setField(user, "id", 1L);

            when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
            when(workerRepository.existsByUserIdAndDeletedAtIsNull(1L)).thenReturn(true);

            assertThatThrownBy(() ->
                    workerService.createMyWorkerProfile(1L, createWorkerRequest("김민준", "대리", "개발팀")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getType())
                    .isEqualTo(BusinessExceptionType.WORKER_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("assignProject")
    class AssignProject {

        @Test
        @DisplayName("이미 같은 프로젝트에 배정된 작업자면 ASSIGNMENT_ALREADY_EXISTS 예외를 던진다")
        void alreadyAssigned_throws() {
            Worker worker = Worker.create(1L, "김민준", "대리", "개발팀");
            ReflectionTestUtils.setField(worker, "id", 10L);

            Company company = Company.create("그린테크", ContractType.ELECTRONIC);
            company.registerProject("프로젝트", LocalDate.now(), LocalDate.now().plusDays(1));
            ReflectionTestUtils.setField(company.getProjects().get(0), "id", 100L);

            when(workerRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(worker));
            when(companyRepository.findByProjects_IdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(company));
            when(assignmentRepository.existsByWorkerIdAndProjectIdAndDeletedAtIsNull(10L, 100L)).thenReturn(true);

            assertThatThrownBy(() -> workerService.assignProject(100L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getType())
                    .isEqualTo(BusinessExceptionType.ASSIGNMENT_ALREADY_EXISTS);

            verify(assignmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 작업자면 WORKER_NOT_FOUND 예외를 던진다")
        void workerNotFound_throws() {
            when(workerRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> workerService.assignProject(100L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getType())
                    .isEqualTo(BusinessExceptionType.WORKER_NOT_FOUND);
        }
    }
}