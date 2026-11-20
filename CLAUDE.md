@AGENTS.md

## Claude Code

- 반복 작업은 `.claude/skills/`의 스킬을 사용합니다.
  - `add-backend-usecase`: 백엔드 유스케이스를 헥사고날 구조로 추가
  - `add-frontend-feature`: 백엔드 API를 쓰는 프론트 화면 추가
  - `verify`: 백엔드·프론트 전체 검증
- 프론트 `.ts`/`.tsx` 파일을 수정하면 hook이 해당 파일에 `eslint --fix`를 실행합니다.
