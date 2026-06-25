# 계약 업체 관리 백오피스 시스템

오픈소스컨설팅에서 전자계약/수기계약을 진행한 업체와 외주 작업자를 통합 관리하는 백오피스 시스템입니다.
계약서를 업로드하면 OCR/LLM으로 업체명·계약기간을 자동 추출해 업체·프로젝트를 생성하고,
관리자는 작업자를 등록·승인하고 프로젝트에 배정할 수 있습니다.

## 1. 기술 스택 선택 근거

| 영역 | 선택 | 비고 |
|---|---|---|
| 언어/프레임워크 | Java 21, Spring Boot 3.x | |
| DB | MySQL 8 | |
| 비동기 처리 | Kafka (KRaft, 단일 브로커) | 계약서 OCR 처리를 비동기로 분리 |
| OCR | Tesseract (Docker, REST API) | API Key 불필요 — 평가 환경에서 별도 키 발급 없이 바로 실행 가능 |
| LLM | Ollama + EXAONE 3.5 (2.4b) | 한국어 특화, 로컬 실행, API Key 불필요 |
| 인증 | JWT (Access/Refresh) | Spring Security + 커스텀 필터 |
| 빌드 | Gradle | |

### 백엔드: Spring Boot + JPA
연관관계 설정과 DDD 구조(애그리거트, 캡슐화된 상태 전이)를 표현하기에 JPA의 엔티티 매핑이 적합하다고 판단했습니다.
또한 Repository Pattern(도메인 인터페이스 + JPA 구현체 다중 상속)으로 도메인 계층이 영속성 기술을 직접 알지 못하게 분리할 수 있고,
Query Method(메서드 이름 기반 쿼리 자동 생성)로 단순 조회 로직의 보일러플레이트를 크게 줄일 수 있다는 점도 선택 이유입니다.

### DB: MySQL (RDB)
계약/업체/프로젝트/작업자 간 관계가 명확하고 트랜잭션 일관성이 중요한 도메인이라, 타입 안정성과 정합성이 보장되는 RDB로 한정했습니다.
인메모리 DB(H2 등)는 영속성이 없어 운영 환경에 부적합하고, NoSQL(MongoDB 등)은 관계형 데이터가 많은 이 도메인에서는 조인/정합성 관리 비용이 더 커진다고 판단해 제외했습니다.
RDB 중에서는 PostgreSQL, MariaDB 등도 고려했으나, 초기 계정/스키마/권한 설정이 간단하고 평가 환경에서 추가 설정 없이 바로 띄울 수 있다는 점에서 MySQL을 선택했습니다.

### 빌드 도구: Gradle
Maven은 의존성/플러그인을 XML로 선언해야 해서 설정이 길어지고 가독성이 떨어집니다. Gradle은 Groovy/Kotlin DSL 기반으로 같은 설정을 더 간결하게 표현할 수 있어 선택했습니다.

### 외부 연동 정책: Docker 설정만으로 실행 가능한 것만 채택
기획 단계에서는 Google Drive 연동, Jira 정식 연동, 상용 OCR/LLM API 등 외부 시스템 연동 아이디어가 많았습니다.
다만 API Key 발급이나 외부 앱 연동(OAuth 등)이 필요한 항목은 평가자가 테스트할 때 추가 설정 부담이 생긴다고 판단해, **`docker compose up`만으로 키 발급 없이 끝까지 동작하는 구성요소만** 선택했습니다 (OCR: Tesseract, LLM: Ollama). 이 결정의 트레이드오프는 8번 항목에 정리했습니다.

### LLM 모델: Qwen2.5-3B → EXAONE-3.5-2.4B로 변경
초기에는 Qwen2.5-3B를 사용했으나, 계약서에서 업체명/날짜를 추출하는 테스트 과정에서 한국어 인식 정확도가 기대에 못 미쳤습니다.
한국어/영어 이중언어로 명시적으로 학습된 LG AI Research의 EXAONE 3.5로 교체한 뒤 추출 정확도가 개선되어 최종적으로 채택했습니다 (모델 크기는 로컬 실행 부담을 고려해 2.4b로 유지).

### 프론트엔드: React + MUI + Zustand
백오피스 특성상 데이터 그리드/폼/모달이 많아, 컴포넌트가 풍부한 MUI를 사용했습니다. 상태 관리는 Redux 대비 보일러플레이트가 적은 Zustand로 선택해, 인증 정보처럼 전역으로 필요한 상태만 가볍게 관리하도록 구성했습니다.

## 2. 실행 방법

```bash
docker compose up -d   # MySQL, Kafka, Ollama(LLM), Tesseract(OCR) 일괄 기동
./gradlew bootRun       # 애플리케이션 실행 (IDE에서 직접 실행해도 무방)
```

- 최초 기동 시 Ollama가 LLM 모델(약 1.5GB)을 자동으로 내려받습니다. 몇 분 정도 걸릴 수 있습니다.
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Kafka UI(토픽/메시지 확인용, 선택): `http://localhost:8089`

### 초기 데이터 (자동 시드)
앱을 처음 기동하면 `DataInitializer`가 테스트용 작업자 3명을 자동으로 만들어둡니다 (재기동 시 이미 있으면 건너뜀, 멱등 처리).

| 이메일 | 비밀번호 | 이름 | 직책 | 부서 |
|---|---|---|---|---|
| worker1@osci.com | password1234 | 김민준 | 대리 | 개발팀 |
| worker2@osci.com | password1234 | 이서연 | 과장 | 영업팀 |
| worker3@osci.com | password1234 | 박지훈 | 사원 | 기획팀 |

관리자 계정은 `POST /api/v1/users/admin`으로 직접 가입해야 합니다 (자동 시드되지 않음).

### 기본 계정 생성 흐름
1. `POST /api/v1/users/admin` — 관리자 계정 생성 (가입과 동시에 자동 승인)
2. `POST /api/v1/users` — 일반 유저 가입 신청 (승인 대기 상태)
3. 관리자 로그인 후 `GET /api/v1/users?status=PENDING` → `POST /api/v1/users/{id}/approve`로 승인
4. 승인된 유저는 `POST /api/v1/workers/me`로 본인 작업자 프로필(이름/직책/부서) 등록
5. 관리자가 `POST /api/v1/projects/{projectId}/workers/{workerId}`로 프로젝트에 배정

### 테스트용 샘플 계약서
계약서 업로드 기능을 테스트할 때 직접 파일을 준비하지 않아도 되도록, 샘플 계약서를 저장소에 포함해뒀습니다.

| 파일 | 형식 | 내용 |
|---|---|---|
| [contract_1.pdf](./sample-contracts/contract_1.pdf) | PDF (텍스트 레이어 포함) | 갑: 오픈소스컨설팅 / 을: 주식회사 그린테크 — 용역 계약서 |
| [contract_2.pdf](./sample-contracts/contract_2.pdf) | PDF (텍스트 레이어 포함) | 갑: 주식회사 가람물산 / 을: 오픈소스컨설팅 — 소프트웨어 공급 계약서 |
| [contract_3_handwritten.png](./sample-contracts/contract_3_handwritten.png) | 이미지 (OCR 테스트용) | 갑: 오픈소스컨설팅 / 을: 미래테크 주식회사 — 외주 개발 계약서 |

모두 가상의 업체명/사업자등록번호로 작성된 테스트 데이터입니다. 실제 거래 업체와는 무관합니다.

## 3. 시스템 구성도

```
[React 프론트엔드]
       │  REST API (JWT 인증)
       ▼
[Spring Boot API] ──────── JPA ────────▶ [MySQL]
       │
       │ contractId만 발행 (파일 바이트는 싣지 않음)
       ▼
   [Kafka: ocr-process 토픽]
       │
       ▼
[OCR 처리 Consumer]
   ├─▶ [Tesseract OCR 서버]  (이미지 텍스트 추출)
   ├─▶ [Ollama LLM]          (업체명/계약기간 추출)
   └─▶ [MySQL]                (Company/Project/Contract 결과 반영)
```

### 계약서 처리 요청 흐름
1. `POST /api/v1/contracts` — 파일을 로컬 디스크에 저장, `Contract`를 `PENDING`으로 DB 저장 (트랜잭션 커밋)
2. 커밋 완료 후 Kafka에 `contractId`만 발행 (파일 바이트는 메시지에 싣지 않음 — 처리 지연/메시지 크기 문제 방지)
3. Consumer가 `contractId`로 `Contract`를 조회 → 디스크에서 파일 로드 → PDF는 텍스트 직접 추출, 이미지는 OCR
4. 추출된 텍스트를 LLM에 전달해 업체명/계약기간 JSON으로 추출
5. 업체명으로 `Company`를 찾거나 새로 생성 → 그 안에 `Project` 생성 → `Contract`에 연결
6. 실패 시 최대 2회 재시도(`@Retryable`), 모두 실패하면 `FAILED` 상태로 확정

## 4. DB 설계 (애그리거트 구조)

**관계 요약**: `User` 1 : 0~1 `Worker` ・ `Company` 1 : N `Project` ・ `Worker` 1 : N `WorkerProjectAssignment` N : 1 `Project`

| 테이블 | 주요 컬럼 | 설명 |
|---|---|---|
| `user` | id, email, password, role(ADMIN/WORKER), status(PENDING/APPROVED/REJECTED) | 로그인 계정 |
| `company` | id, name, contract_type | 계약 업체 |
| `project` | id, **company_id**(FK), title, start_date, end_date | `company` 애그리거트 내부 자식 |
| `contract` | id, **company_id**(ID 참조, FK 아님), **project_id**(ID 참조, FK 아님), file_url, ocr_status, extracted_text | 업로드 시점엔 업체/프로젝트 미정이라 ID만 보유 |
| `worker` | id, **user_id**(FK), name, position, department | 작업자 프로필 |
| `worker_project_assignment` | id, **worker_id**(FK), **project_id**(FK), assigned_at | 작업자-프로젝트 배정 조인 테이블 |

**설계상 핵심 결정**
- **`Company` ↔ `Project`**: 같은 애그리거트 (`Company`가 루트). `Project`는 `Company`를 통해서만 생성/수정 가능 (package-private 메서드).
- **`Contract`는 독립 애그리거트**: 계약서 업로드 시점엔 어느 업체/프로젝트인지 알 수 없어서(OCR 처리 후에야 확정), `Company`/`Project`를 객체 참조가 아닌 **ID로만** 들고 있습니다 (`company_id`, `project_id`에 외래키 제약을 걸지 않음). 이렇게 해야 서로 다른 애그리거트 간 Hibernate flush 순서에 의존하지 않아 `TransientObjectException` 같은 문제가 구조적으로 발생하지 않습니다.
- **`Worker`도 독립 애그리거트**: `User`를 ID로 참조. `Worker`-`Project` 배정은 `WorkerProjectAssignment` 조인 엔티티로 별도 관리합니다.
- **인덱스**: `Contract.companyId`/`Contract.projectId`는 업체·프로젝트별 계약서 조회 시 자주 필터링되는 컬럼이라 인덱스 권장 (현재 `ddl-auto: update`로만 운영 중이라 운영 환경 전환 시 명시적 인덱스 마이그레이션 필요).

## 5. 계층 구조 (DDD 스타일)

```
domain/        - 엔티티, Repository 인터페이스 (순수 도메인, 프레임워크 의존 최소화)
application/   - UseCase 인터페이스, Facade(오케스트레이션), CommandService(쓰기), QueryService(읽기), DTO
infrastructure/ - JPA Repository 구현체, Kafka, 외부 연동(OCR/LLM/파일저장), Security, 예외 처리
presentation/  - Controller (HTTP ↔ UseCase 변환만 담당)
```

- Repository는 `domain.repository`(순수 인터페이스)와 `infrastructure.repository`(`JpaRepository`와 다중 상속)로 분리
- Controller는 인프라(파일 저장, Kafka 발행 등)를 직접 호출하지 않고 항상 `UseCase`만 의존
- 다중 파일 업로드처럼 "파일 하나 실패해도 나머지는 계속 처리한다" 같은 정책 결정은 Controller가 아닌 Facade에 위치

## 6. 주요 API

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| POST | `/api/v1/users`, `/api/v1/users/admin` | 공개 | 일반/관리자 가입 |
| POST | `/api/v1/users/login` | 공개 | 로그인 |
| GET | `/api/v1/users?status=PENDING` | ADMIN | 가입 승인 대기 목록 |
| POST | `/api/v1/users/{id}/approve` `/reject` | ADMIN | 가입 승인/거절 |
| POST | `/api/v1/contracts` | ADMIN | 계약서 다중 업로드 (OCR/LLM 비동기 처리) |
| GET | `/api/v1/contracts`, `/{id}` | ADMIN | 계약서 목록/상세 |
| GET | `/api/v1/companies`, `/{id}` | ADMIN | 업체 목록/상세 (상세는 소속 프로젝트 포함) |
| PUT/DELETE | `/api/v1/companies/{id}` | ADMIN | 업체 수정/삭제 |
| GET/PUT | `/api/v1/projects`, `/{id}` | 로그인 사용자 | 프로젝트 목록(ADMIN)/상세(전체)/수정(ADMIN) |
| POST | `/api/v1/workers/me` | 승인된 유저 | 본인 작업자 프로필 등록 |
| GET | `/api/v1/workers/me/projects` | 로그인 사용자 | 본인 배정 프로젝트 목록 |
| GET | `/api/v1/workers?keyword=` | ADMIN | 작업자 목록/이름 검색 |
| POST/DELETE | `/api/v1/projects/{id}/workers/{workerId}` | ADMIN | 작업자 배정/해제 |

전체 API는 Swagger UI에서 확인 가능합니다.

## 7. AI 도구 활용 내역

- **Claude (Anthropic)**: 설계 단계부터 전체 백엔드/프론트엔드 코드 작성, 트러블슈팅(Kafka 컨슈머 그룹 코디네이터 오류, Hibernate `TransientObjectException`, Lombok `@Builder` + 컬렉션 필드 초기화 함정 등)에 활용
- AI가 생성한 코드 중 **직접 수정/검토한 부분**:
  - 애그리거트 경계 설계(어떤 엔티티를 ID로만 참조할지)는 실제 발생한 런타임 오류(`TransientObjectException`)를 직접 분석해 구조를 재조정
  - `CommonResponse`의 HTTP 상태코드 처리, `BusinessExceptionType` 코드 중복 등 최종 코드 리뷰를 통해 발견하고 수정
  - 프론트엔드 권한별 화면 노출 범위, Kafka 메시지 설계(파일 바이트 대신 ID만 전달) 등은 직접 요구사항을 정의하고 반복 보완

## 8. 미완성 또는 개선하고 싶은 점

- **OCR/LLM 정확도**: 로컬 오픈소스(Tesseract + EXAONE 3.5 2.4b)는 상용 API(Google Cloud Vision, GPT 계열)보다 인식률이 낮습니다. 실 서비스 전환 시 `TextExtractor`/`ContractInfoExtractor` 인터페이스의 새 구현체만 추가하면 교체 가능하도록 추상화해뒀습니다.
- **Google Drive 연동**: 기존에 보유 중인 계약서들을 Google Drive에서 한 번에 가져와 시스템에 일괄 등록하는 절차를 검토했으나, 5일 일정상 범위에서 제외했습니다.
- **Jira 정식 연동**: 작업자에게 업무를 배정할 때 Jira에도 동일한 이슈가 생성되어 두 플랫폼이 서로 연동되는 것을 의도했으나, OAuth 연동 및 API 키 설정이 필요해 평가 환경 실행 편의성을 위해 범위에서 제외했습니다. 현재는 플랫폼 내에서만 배정을 관리합니다.
- **Project 접근 제어**: 현재 `GET /api/v1/projects/{id}`는 로그인한 사용자라면 누구나 조회 가능합니다(작업자가 본인 미배정 프로젝트의 ID를 알면 조회는 가능). 본인 배정 여부까지 엄격히 검증하는 로직은 범위에서 제외했습니다.
- **OCR 처리 결과 실시간 알림**: 현재는 프론트엔드에서 5초 폴링 방식으로 상태를 갱신합니다. WebSocket/SSE로 전환하면 더 즉각적인 피드백이 가능합니다.
- **Kafka 단일 파티션/단일 컨슈머 구조**: 현재 `ocr-process` 토픽이 단일 파티션, 컨슈머도 1개라 계약서 업로드가 대량으로 몰리면 순차 처리로 인한 병목이 발생할 수 있습니다. 파티션을 늘리고 컨슈머 그룹 내 인스턴스를 여러 개 운영하는 방식(파티셔닝)을 고려할 수 있습니다.