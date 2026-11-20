# hexagonal-spring-next-template

헥사고날 아키텍처 기반 Spring Boot 백엔드와 Next.js 프론트엔드로 구성된 모노레포 템플릿입니다.
샘플 기능(`Item` 생성·조회)이 백엔드부터 화면까지 연결되어 있어, 같은 패턴으로 기능을 추가하면 됩니다.

## 구성

| 경로 | 내용 |
|---|---|
| [`backend/`](backend/README.md) | Spring Boot 4.1 · Java 25 · Gradle (Kotlin DSL) · PostgreSQL · Flyway · ArchUnit |
| [`frontend/`](frontend/README.md) | Next.js 16 (App Router, Cache Components) · React 19 · TypeScript · Tailwind 4 |
| `.github/` | CI(backend, frontend), CodeQL, Dependabot, PR·이슈 템플릿 |
| `AGENTS.md`, `CLAUDE.md`, `.claude/` | AI 에이전트 작업 규칙, 스킬, hook |

## 요구 사항

- JDK 25
- Node.js 20.9 이상 (CI는 24)
- Docker (로컬 DB, Testcontainers 테스트)

## 빠른 시작

```bash
# 1) 백엔드: compose.yaml의 PostgreSQL이 자동으로 뜹니다
cd backend && ./gradlew bootRun

# 2) 프론트엔드
cd frontend && cp .env.example .env.local && npm install && npm run dev
```

http://localhost:3000 에서 항목을 추가하고 목록을 확인할 수 있습니다.

## 템플릿으로 새 프로젝트 시작하기

1. GitHub에서 **Use this template**으로 저장소를 만듭니다.
2. 패키지명 `com.example.template`과 `group = "com.example"`(`backend/build.gradle.kts`)을 바꿉니다.
3. `.github/CODEOWNERS`, `LICENSE`의 소유자를 바꿉니다.
4. 샘플 `Item` 기능은 참고 후 삭제하거나 실제 도메인으로 바꿉니다.

## 작업 규칙

아키텍처 규칙, 테스트 방식, 커밋·PR 규칙은 [AGENTS.md](AGENTS.md)에 있습니다.
PR을 열면 CI, CodeQL, AI 코드 리뷰가 자동으로 실행됩니다.

## 라이선스

[MIT](LICENSE)
