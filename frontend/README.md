# frontend

Next.js 16 (App Router, Cache Components) / React 19 / TypeScript / Tailwind 4

## 실행

```bash
cp .env.example .env.local   # BACKEND_URL
npm install
npm run dev
```

백엔드가 `BACKEND_URL`(기본 http://localhost:8080)에서 떠 있어야 목록이 나온다.

## 스크립트

- `npm run dev` - 개발 서버
- `npm run build` - 프로덕션 빌드
- `npm run lint` - ESLint (Next 16부터 `next lint` 없음)
- `npm run typecheck` - `next typegen && tsc --noEmit`
- `npm run api:types` - `../openapi/openapi.json`에서 `src/lib/api/schema.d.ts` 생성

## API 타입

백엔드 API 타입은 손으로 쓰지 않고 OpenAPI 스펙에서 생성한다.
백엔드에서 `./gradlew updateOpenApi`로 스펙이 바뀌면 여기서 `npm run api:types`를 돌리고 같이 커밋한다.
CI가 생성 결과와 커밋된 파일이 다르면 실패시킨다.

```ts
import type { components } from "./schema";
type Item = components["schemas"]["ItemResponse"];
```

## Docker

```bash
docker build -t frontend .
docker run -p 3000:3000 -e BACKEND_URL=http://<backend>:8080 frontend
```

## 구조

```
src/
├── app/
│   ├── page.tsx                 # 목록 + 생성 폼
│   └── items/
│       ├── actions.ts           # Server Action (생성 후 refresh)
│       ├── create-item-form.tsx # 폼 (useActionState)
│       └── item-list.tsx        # 서버 컴포넌트, 요청마다 조회
└── lib/api/items.ts             # 백엔드 API 클라이언트 (server-only)
```

- 백엔드 호출은 서버에서만 한다. 브라우저는 백엔드 주소를 모르니 CORS 설정이 필요 없다.
- `cacheComponents`가 켜져 있어서 캐시하지 않는 데이터는 `<Suspense>` 안에서 읽어야 빌드가 된다.
