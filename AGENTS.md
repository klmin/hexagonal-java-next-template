# AGENTS.md

이 저장소의 작업 규칙.

## 구성

- `backend/` - Spring Boot 4.1, Java 25, Gradle(Kotlin DSL), PostgreSQL, Flyway ([README](backend/README.md))
- `infra/observability/` - 로컬 모니터링 스택 ([README](infra/observability/README.md))
- `frontend/` - Next.js 16 (App Router, Cache Components), React 19, TypeScript, Tailwind 4 ([README](frontend/README.md), [AGENTS](frontend/AGENTS.md))

## 명령

```bash
# backend (JDK 25)
cd backend && ./gradlew build

# frontend (Node 20.9+)
cd frontend && npm ci
npm run lint && npm run typecheck && npm run build
```

작업을 끝내기 전에 바뀐 쪽의 명령을 돌려서 통과시킨다.
Docker가 없으면 Testcontainers 테스트는 skip되고 CI에서만 돈다.

## 백엔드 구조 (기능별 패키지 + 헥사고날)

패키지 루트는 `com.example.template`. 기능마다 패키지를 만들고 그 안을 아래처럼 나눈다.

```
<feature>/domain/                    도메인 모델, 규칙. Spring/JPA/Validation 의존 금지
<feature>/application/port/in/       유스케이스 인터페이스 (+ Command/Query record)
<feature>/application/port/out/      외부 의존 포트 인터페이스
<feature>/application/service/       유스케이스 구현. @Transactional은 여기
<feature>/application/               기능 예외 (예: ItemNotFoundException)
<feature>/adapter/in/web/            컨트롤러, Request/Response DTO
<feature>/adapter/out/persistence/   JPA 엔티티/리포지토리, jOOQ 조회, 포트 구현
common/error/                        공통 예외 (DomainValidationException, NotFoundException)
common/web/                          전역 예외 처리 (Problem Details)
common/config/                       공통 Bean
common/jooq/                         jOOQ 생성 코드 (빌드 시 생성, 커밋 X)
```

`HexagonalArchitectureTest`가 검사하는 규칙:

- 기능 안에서는 `adapter -> application -> domain` 방향만
- 컨트롤러(adapter.in)는 `port.in`만 사용. 서비스나 `port.out`을 직접 쓰지 않는다
- adapter.in과 adapter.out은 서로 참조하지 않는다
- 다른 기능은 그 기능의 `application.port.in`과 `domain`으로만 접근. 기능 간 순환 참조 금지
- `common`은 특정 기능을 참조하지 않는다
- jOOQ(`org.jooq`, `common.jooq`)는 adapter.out에서만 사용

그 외:

- JPA 엔티티, 리포지토리, 어댑터는 package-private. 엔티티를 어댑터 밖으로 내보내지 않고 도메인 모델로 변환한다
- 도메인 규칙 위반은 `DomainValidationException`(400), 없는 리소스는 `NotFoundException`을 상속한 기능 예외(404)
- 저장/수정은 JPA, 검색/통계 같은 조회는 jOOQ. jOOQ 코드는 Flyway 마이그레이션으로 빌드 때 생성되니 직접 고치지 않는다
- 스키마 변경은 Flyway `V{n}__설명.sql`로만. 머지된 마이그레이션은 고치지 않는다
- 로그는 SLF4J로 남긴다. traceId는 자동으로 붙으니 직접 넣지 않는다. 개인정보, 비밀값은 로그에 남기지 않는다
- 현재 시각은 `Clock` Bean 주입 (`Instant.now()` 직접 호출 X)

### 테스트

- domain: 순수 단위 테스트
- application.service: `port.out`을 인메모리 구현으로 바꿔서 단위 테스트
- adapter.in.web: `@WebMvcTest` + `@MockitoBean`으로 유스케이스 mock
- adapter.out.persistence: JPA는 `@DataJpaTest`, jOOQ는 `@JooqTest`. 둘 다 Testcontainers PostgreSQL + `@Testcontainers(disabledWithoutDocker = true)`

## 프론트엔드

- Next.js 16은 이전 버전과 API가 다르다. 코드 쓰기 전에 `frontend/node_modules/next/dist/docs/`에서 해당 문서를 확인한다
- 백엔드 호출은 서버에서만. API 클라이언트는 `src/lib/api/`에 두고 `import "server-only"`. 주소는 `BACKEND_URL`
- 캐시하지 않는 데이터를 읽는 서버 컴포넌트는 `<Suspense>` 안에 둔다 (아니면 `next build` 실패)
- 변경은 Server Action + `useActionState`. 성공하면 `refresh()`(`next/cache`). 예상 가능한 오류는 throw 말고 상태로 반환
- `next lint`는 없다. `npm run lint` 사용

## Git / PR

- 브랜치: `feat/`, `fix/`, `refactor/`, `chore/`, `docs/`, `test/`
- 커밋: Conventional Commits, 설명은 한국어. 예) `feat(backend): 주문 취소 API 추가`
- PR 하나에 목적 하나. 커밋은 논리 단위로 나눈다

## Review guidelines

PR 리뷰 시 아래를 우선 본다. 스타일, 취향 문제는 지적하지 않거나 낮은 우선순위로.

- 위 헥사고날 규칙 위반 (domain의 프레임워크 의존, 컨트롤러의 서비스/리포지토리 직접 참조, 엔티티 노출, 기능 간 내부 참조)
- 트랜잭션 경계 누락이나 잘못된 위치
- 머지된 Flyway 마이그레이션 수정, 엔티티와 스키마 불일치
- 입력 검증 누락, Problem Details가 아닌 오류 응답
- 프론트에서 백엔드를 브라우저로 직접 호출하거나 서버 전용 값을 `NEXT_PUBLIC_`으로 노출
- 새 기능에 테스트 없음
- 비밀값(키, 비밀번호, 토큰) 커밋
