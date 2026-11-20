---
name: add-frontend-feature
description: 백엔드 API를 사용하는 Next.js 화면(조회 목록, 생성·수정 폼 등)을 추가한다. 새 페이지나 기존 페이지에 데이터 기능을 붙일 때 사용.
---

# 프론트 기능 추가

참고 구현: `frontend/src/lib/api/items.ts`, `frontend/src/app/items/`, `frontend/src/app/page.tsx`

## 0. 먼저 문서 확인
Next.js 16은 이전 버전과 API가 다르다. 쓰려는 기능의 문서를 `frontend/node_modules/next/dist/docs/01-app/`에서 확인한다.
- 데이터 조회·캐시: `01-getting-started/08-caching.md`
- 변경(Server Action): `01-getting-started/07-mutating-data.md`
- 오류 처리: `01-getting-started/10-error-handling.md`

## 1. API 클라이언트
- `src/lib/api/<기능>.ts`에 타입과 함수를 추가한다. 첫 줄은 `import "server-only";`.
- 백엔드 주소는 `backendUrl()` 패턴(`await connection()` 후 `process.env.BACKEND_URL`)을 따른다.
- `!res.ok`이면 Problem Details의 `detail`로 `ApiError`를 만든다. 공통 부분이 늘어나면 `src/lib/api/client.ts`로 분리한다.

## 2. 조회 (서버 컴포넌트)
- `src/app/<경로>/` 아래에 async 서버 컴포넌트로 만든다.
- 캐시하지 않는 데이터는 페이지에서 `<Suspense fallback={…}>`로 감싼다. 감싸지 않으면 `next build`가 실패한다.
- 백엔드 장애 시 페이지 전체가 깨지지 않도록 컴포넌트 안에서 오류 메시지를 보여 주거나 `error.tsx`를 둔다.
- 동적 경로의 params는 `PageProps<'/경로/[id]'>`로 받고 `await props.params`로 읽는다.

## 3. 변경 (Server Action)
- `actions.ts` (`"use server"`)에 `(prevState, formData) => Promise<State>` 형태로 작성한다.
- 예상 가능한 오류(검증, 4xx)는 throw하지 않고 `{ error }`로 반환한다.
- 성공하면 `refresh()`(`next/cache`)를 호출한다. 다른 페이지로 이동해야 하면 `redirect()`를 쓴다.
- 폼은 클라이언트 컴포넌트에서 `useActionState`(`react`)로 연결하고 `pending`으로 버튼을 비활성화한다.

## 4. 규칙
- 브라우저에서 백엔드를 직접 호출하지 않는다. 서버 전용 값을 `NEXT_PUBLIC_`으로 만들지 않는다.
- 스타일은 Tailwind 유틸리티 클래스, 다크 모드는 `dark:` 변형을 같이 쓴다.

## 5. 검증
```bash
cd frontend && npm run lint && npm run typecheck && npm run build
```
가능하면 `npm run dev`로 띄워 실제 백엔드와 함께 생성·조회·오류 표시를 확인한다.
