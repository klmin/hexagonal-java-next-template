# frontend

Next.js 16 (App Router, Cache Components) · React 19 · TypeScript · Tailwind CSS 4

## 실행

```bash
cp .env.example .env.local   # BACKEND_URL 설정
npm install
npm run dev                  # http://localhost:3000
```

백엔드(`../backend`)가 `BACKEND_URL`(기본 `http://localhost:8080`)에서 실행 중이어야 목록이 보입니다.

## 스크립트

| 명령 | 설명 |
|---|---|
| `npm run dev` | 개발 서버 |
| `npm run build` | 프로덕션 빌드 |
| `npm run lint` | ESLint (Next 16부터 `next lint` 없음) |
| `npm run typecheck` | `next typegen && tsc --noEmit` |

## 구조

```
src/
├─ app/
│  ├─ page.tsx                 # 목록(Suspense로 스트리밍) + 생성 폼
│  └─ items/
│     ├─ actions.ts            # Server Action: 생성 후 refresh()
│     ├─ create-item-form.tsx  # 클라이언트 폼 (useActionState)
│     └─ item-list.tsx         # 서버 컴포넌트: 요청마다 백엔드 조회
└─ lib/api/items.ts            # 백엔드 API 클라이언트 (server-only)
```

- 백엔드 호출은 서버에서만 합니다 (`server-only`). 브라우저는 백엔드 주소를 모르므로 CORS 설정이 필요 없습니다.
- `cacheComponents`가 켜져 있어, 캐시하지 않는 데이터는 반드시 `<Suspense>` 안에서 읽어야 빌드가 통과합니다.
- 백엔드 오류(Problem Details)의 `detail`을 그대로 사용자에게 보여 줍니다.
