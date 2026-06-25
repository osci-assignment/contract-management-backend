package com.osci.contractmanagement.infrastructure.initializer;

import com.osci.contractmanagement.domain.model.user.User;
import com.osci.contractmanagement.domain.model.worker.Worker;
import com.osci.contractmanagement.domain.repository.UserRepository;
import com.osci.contractmanagement.domain.repository.WorkerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 테스트/시연 편의를 위해 앱 최초 기동 시 작업자 계정 3명을 미리 만들어둔다.
 * 이미 데이터가 있으면(재기동 시) 아무것도 하지 않는다 (멱등성 보장).
 */
@Component
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "password1234";

    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, WorkerRepository workerRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.workerRepository = workerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmailAndDeletedAtIsNull("worker1@osci.com").isPresent()) {
            log.info("초기 작업자 데이터가 이미 존재합니다. 시드 작업을 건너뜁니다.");
            return;
        }

        seedWorker("worker1@osci.com", "김민준", "대리", "개발팀");
        seedWorker("worker2@osci.com", "이서연", "과장", "영업팀");
        seedWorker("worker3@osci.com", "박지훈", "사원", "기획팀");

        log.info("초기 작업자 3명 등록 완료 (worker1~3@osci.com / 비밀번호: {})", DEFAULT_PASSWORD);
    }

    private void seedWorker(String email, String name, String position, String department) {
        User user = User.create(email, passwordEncoder.encode(DEFAULT_PASSWORD));
        user.approve(); // 테스트 편의를 위해 가입과 동시에 승인 처리
        userRepository.save(user);

        Worker worker = Worker.create(user.getId(), name, position, department);
        workerRepository.save(worker);
    }
}