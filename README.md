# hexagonal-spring-next-template

Spring Boot(헥사고날) 백엔드 + Next.js 프론트엔드 모노레포 템플릿.
샘플 기능(Item 생성/조회)이 DB부터 화면까지 연결돼 있으니 같은 방식으로 기능을 추가하면 된다.

- `backend/` - Spring Boot 4.1, Java 25, Gradle(Kotlin DSL), PostgreSQL, Flyway
- `frontend/` - Next.js 16, React 19, TypeScript, Tailwind 4
- `compose.yaml` - 전체 스택을 이미지로 실행 (E2E, 로컬 확인)
- `openapi/` - API 스펙 (백엔드에서 생성, 프론트 타입 생성에 사용)
- `infra/observability/` - 모니터링 스택 (Prometheus, Loki, Tempo, Grafana, Alloy)
- `.github/` - CI, CodeQL, Dependabot, PR/이슈 템플릿

## 필요한 것

- JDK 25
- Node.js 22.12+
- Docker

## 실행

```bash
# 백엔드 (compose.yaml의 PostgreSQL이 같이 뜸)
cd backend
./gradlew bootRun

# 프론트엔드
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

http://localhost:3000

전체를 이미지로 띄울 때:

```bash
(cd backend && ./gradlew bootJar)
docker compose up -d --build --wait
```

모니터링은 [infra/observability](infra/observability/README.md) 참고.

## 새 프로젝트로 쓸 때

1. GitHub에서 **Use this template**으로 저장소 생성
2. 패키지명 `com.example.template`, `backend/build.gradle.kts`의 `group` 변경
3. `.github/CODEOWNERS`, `LICENSE` 소유자 변경
4. 샘플 `item` 기능은 참고 후 삭제

작업 규칙은 [AGENTS.md](AGENTS.md) 참고.

## License

[MIT](LICENSE)
