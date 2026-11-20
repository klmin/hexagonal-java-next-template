# backend

Spring Boot 4.1 / Java 25 / Gradle(Kotlin DSL) / PostgreSQL / Flyway

## 패키지 구조

기능(feature)별로 패키지를 나누고, 기능 안에서 헥사고날 구조를 따른다.

```
com.example.template
├── item/                       # 기능 하나
│   ├── domain/                 # 도메인 모델, 규칙 (Spring/JPA 의존 X)
│   ├── application/
│   │   ├── port/in/            # 유스케이스 인터페이스
│   │   ├── port/out/           # 외부 의존 포트 (저장소 등)
│   │   └── service/            # 유스케이스 구현, 트랜잭션
│   └── adapter/
│       ├── in/web/             # 컨트롤러, 요청/응답 DTO
│       └── out/persistence/    # JPA 엔티티, 리포지토리, 포트 구현
└── common/                     # 기능 공통 (예외, 전역 예외 처리, 설정)
```

규칙은 `HexagonalArchitectureTest`(ArchUnit)가 빌드 때 검사한다.

- 기능 안에서는 `adapter -> application -> domain` 방향만 허용
- 컨트롤러는 `port.in`만 사용 (서비스, `port.out` 직접 참조 X)
- 다른 기능은 그 기능의 `application.port.in`과 `domain`으로만 접근
- `common`은 특정 기능을 참조하지 않음

## 실행 / 빌드

```bash
./gradlew bootRun       # compose.yaml의 PostgreSQL 자동 기동 (Docker 필요)
./gradlew bootTestRun   # Testcontainers로 DB 띄워서 실행
./gradlew build         # 테스트 + ArchUnit 포함
```

Docker가 꺼져 있으면 DB가 필요한 테스트는 skip된다. CI에서는 항상 돈다.

## DB 설정

로컬에서는 `application.yml`에 datasource 설정이 없다.
`spring-boot-docker-compose`가 `compose.yaml`의 PostgreSQL을 띄우고 접속 정보를 자동으로 넣어 준다.
이 의존성은 `developmentOnly`라서 빌드된 jar에는 포함되지 않는다.

운영(jar 실행)에서는 환경변수로 넣는다.

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<db>
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<password>
```

스키마는 Flyway(`src/main/resources/db/migration`)로만 바꾼다. `ddl-auto`는 `validate`.

## 샘플 API

- `POST /api/items` - 생성 (201, Location 헤더)
- `GET /api/items` - 목록 (최신순)
- `GET /api/items/{id}` - 조회 (없으면 404)

오류는 Problem Details(`application/problem+json`)로 응답하고, 입력값 검증 실패 시 `errors`에 필드별 메시지가 들어간다.

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "입력값을 확인해 주세요.",
  "errors": [{ "field": "name", "message": "must not be blank" }]
}
```
