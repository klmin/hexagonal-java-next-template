# AGENTS.md

AI 코딩 에이전트(Claude Code, Codex 등)와 사람이 함께 지키는 작업 규칙입니다.

## 저장소 구성

| 경로 | 내용 | 상세 |
|---|---|---|
| `backend/` | Spring Boot 4.1 · Java 25 · Gradle (Kotlin DSL) · PostgreSQL · Flyway | [backend/README.md](backend/README.md) |
| `frontend/` | Next.js 16 (App Router, Cache Components) · React 19 · TypeScript · Tailwind 4 | [frontend/README.md](frontend/README.md), [frontend/AGENTS.md](frontend/AGENTS.md) |

## 명령

```bash
# backend (JDK 25 필요)
cd backend && ./gradlew build          # 컴파일 + 테스트 + ArchUnit

# frontend (Node 20.9+)
cd frontend && npm ci
npm run lint && npm run typecheck && npm run build
```

변경을 마치기 전에 영향받는 쪽의 위 명령을 실행해 통과시킵니다.
Docker가 없으면 Testcontainers 테스트는 skip되며 CI에서 실행됩니다.

## 백엔드 아키텍처 규칙 (헥사고날)

패키지 루트: `com.example.template`

```
domain/                  순수 도메인. Spring, JPA, Jakarta Validation 의존 금지
application/port/in/     유스케이스 인터페이스 (+ Command record)
application/port/out/    외부 의존 포트 인터페이스
application/service/     유스케이스 구현. 트랜잭션 경계(@Transactional)는 여기
application/exception/   애플리케이션 예외
adapter/in/web/<기능>/   컨트롤러, Request/Response DTO
adapter/in/web/common/   전역 예외 처리 (Problem Details)
adapter/out/persistence/<기능>/  JPA 엔티티, Spring Data 리포지토리, 포트 구현
config/                  공통 Bean
```

- 의존 방향은 `adapter → application → domain` 한 방향만 허용합니다. `HexagonalArchitectureTest`가 강제합니다.
- `adapter.in`과 `adapter.out`은 서로 참조하지 않습니다.
- JPA 엔티티는 `adapter.out.persistence` 밖으로 노출하지 않고, 어댑터에서 도메인 모델로 변환합니다. 엔티티·리포지토리·어댑터는 package-private으로 둡니다.
- 컨트롤러는 유스케이스 인터페이스(`port.in`)에만 의존합니다. 서비스 클래스를 직접 주입하지 않습니다.
- 도메인 규칙 위반은 `DomainValidationException`(400), 리소스 없음은 애플리케이션 예외(404)로 던지고 `GlobalExceptionHandler`에서 Problem Details로 변환합니다.
- 스키마 변경은 `src/main/resources/db/migration/V{n}__설명.sql` Flyway 스크립트로만 합니다. `ddl-auto`는 `validate`입니다. 이미 머지된 마이그레이션은 수정하지 않습니다.
- 현재 시각은 `Clock` Bean을 주입받아 사용합니다 (`Instant.now()` 직접 호출 금지).

### 테스트

| 대상 | 방식 |
|---|---|
| domain | 순수 단위 테스트 |
| application.service | 출력 포트를 인메모리 구현으로 대체한 단위 테스트 |
| adapter.in.web | `@WebMvcTest` + `@MockitoBean` 유스케이스 |
| adapter.out.persistence | `@DataJpaTest` + Testcontainers PostgreSQL (`@Testcontainers(disabledWithoutDocker = true)`) |

## 프론트엔드 규칙

- Next.js 16은 학습 데이터와 API가 다를 수 있습니다. 코드를 쓰기 전에 `frontend/node_modules/next/dist/docs/`의 관련 문서를 확인합니다.
- 백엔드 호출은 서버에서만 합니다. API 클라이언트는 `src/lib/api/`에 두고 `import "server-only"`를 붙입니다. 백엔드 주소는 `BACKEND_URL`(서버 전용 환경변수)입니다.
- 캐시하지 않는 데이터를 읽는 서버 컴포넌트는 `<Suspense>` 안에서 렌더링합니다. 그렇지 않으면 `next build`가 실패합니다.
- 변경 작업은 Server Action + `useActionState`로 하고, 성공하면 `refresh()`(`next/cache`)로 화면을 갱신합니다. 예상 가능한 오류는 throw하지 말고 상태 값으로 반환합니다.
- `next lint`는 없습니다. `npm run lint`(ESLint CLI)를 씁니다.

## Git / PR

- 브랜치: `feat/…`, `fix/…`, `chore/…`, `refactor/…`, `docs/…`, `test/…`
- 커밋 메시지: Conventional Commits, 설명은 한국어. 범위가 있으면 `feat(backend): …`, `fix(frontend): …`
- 커밋과 PR 본문에 AI 도구 서명(`Co-Authored-By`, "Generated with …")을 넣지 않습니다.
- 하나의 PR은 하나의 목적만 담고, 논리 단위로 커밋을 나눕니다.

## Review guidelines

PR 자동 리뷰(Codex 등)는 아래 항목을 우선 확인합니다.

- 헥사고날 의존 방향 위반 (domain의 프레임워크 의존, 컨트롤러의 서비스·리포지토리 직접 참조, 엔티티 노출)
- 트랜잭션 경계 누락 또는 잘못된 위치
- 기존 Flyway 마이그레이션 수정, 엔티티와 스키마 불일치
- 입력 검증 누락, 오류 응답이 Problem Details 형식이 아닌 경우
- 프론트에서 백엔드를 브라우저로 직접 호출하거나 서버 전용 값을 `NEXT_PUBLIC_`으로 노출하는 경우
- 새 기능에 테스트가 없는 경우
- 비밀값(키, 비밀번호, 토큰) 커밋

스타일·취향 문제는 지적하지 않거나 낮은 우선순위로 표시합니다.
