# backend

Spring Boot 4.1 · Java 25 · Gradle (Kotlin DSL) · PostgreSQL · Flyway

## 패키지 구조 (헥사고날)

```
com.example.template
├─ domain/                  # 순수 도메인 모델과 규칙 (Spring·JPA 의존 금지)
├─ application/
│  ├─ port/in/              # 유스케이스 인터페이스 + Command
│  ├─ port/out/             # 영속성 등 외부 의존 포트
│  ├─ service/              # 유스케이스 구현 (트랜잭션 경계)
│  └─ exception/            # 애플리케이션 예외
├─ adapter/
│  ├─ in/web/               # REST 컨트롤러, 요청/응답 DTO, 예외 처리
│  └─ out/persistence/      # JPA 엔티티, 리포지토리, 포트 구현
└─ config/                  # 공통 Bean 설정
```

의존 방향은 `adapter → application → domain` 한 방향이며,
[`HexagonalArchitectureTest`](src/test/java/com/example/template/architecture/HexagonalArchitectureTest.java)
(ArchUnit)가 빌드 때마다 검사합니다.

## 실행

```bash
./gradlew bootRun        # compose.yaml의 PostgreSQL을 자동으로 띄움 (Docker 필요)
./gradlew bootTestRun    # Testcontainers로 DB를 띄워 실행
./gradlew build          # 컴파일 + 테스트 + ArchUnit
```

Docker가 없으면 Testcontainers 테스트(`BackendApplicationTests`, `ItemPersistenceAdapterTest`)는 건너뜁니다.
CI에서는 항상 실행됩니다.

## 샘플 API

| Method | Path | 설명 |
|---|---|---|
| `POST` | `/api/items` | 생성 (201 + `Location`) |
| `GET` | `/api/items` | 목록 (최신순) |
| `GET` | `/api/items/{id}` | 단건 조회 (없으면 404) |

오류 응답은 RFC 9457 Problem Details(`application/problem+json`) 형식입니다.

## 새 기능 추가 순서

1. `domain/<기능>/` 에 도메인 모델
2. `application/port/in` 에 유스케이스 인터페이스, `port/out` 에 필요한 포트
3. `application/service` 에 유스케이스 구현
4. `adapter/out/persistence/<기능>/` 에 엔티티·리포지토리·포트 구현, `db/migration` 에 Flyway 스크립트
5. `adapter/in/web/<기능>/` 에 컨트롤러와 DTO
6. 계층별 테스트 작성 후 `./gradlew build`
