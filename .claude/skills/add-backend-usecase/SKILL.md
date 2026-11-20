---
name: add-backend-usecase
description: 백엔드에 새 기능(유스케이스)을 헥사고날 구조로 추가한다. 새 API, 새 도메인, 기존 도메인에 새 동작을 추가할 때 사용.
---

# 백엔드 유스케이스 추가

`Item` 샘플(`backend/src/main/java/com/example/template/`)을 참고 구현으로 삼아, 아래 순서대로 진행한다.
규칙의 근거는 루트 `AGENTS.md`의 "백엔드 아키텍처 규칙"이다.

## 1. 요구사항 정리
- 입력, 출력, 도메인 규칙, 오류 상황(400/404/409 등)을 먼저 정리한다.
- 기존 도메인에 붙일지, 새 도메인 패키지(`domain/<기능>/`)를 만들지 결정한다.

## 2. domain
- `domain/<기능>/`에 모델을 만든다. 생성은 정적 팩토리(`create`, `restore`)로 하고 불변식은 생성 시점에 검사한다.
- 규칙 위반은 `DomainValidationException`을 던진다.
- Spring, JPA, Jakarta Validation import 금지.
- 테스트: `src/test/.../domain/<기능>/<모델>Test.java` (순수 단위 테스트)

## 3. application
- `port/in/<동사><대상>UseCase.java` 인터페이스, 입력이 둘 이상이면 `<동사><대상>Command` record.
- `port/out/`에 필요한 포트 인터페이스 (`Load…Port`, `Save…Port` 등 동작 단위로 작게).
- `service/<대상>Service.java`에서 구현. 쓰기 메서드에 `@Transactional`, 클래스 기본은 `@Transactional(readOnly = true)`.
- 리소스 없음 등은 `application/exception/`에 예외를 만들고 `GlobalExceptionHandler`에 매핑을 추가한다.
- 현재 시각은 `Clock` 주입.
- 테스트: 출력 포트를 인메모리 구현으로 대체한 단위 테스트 (`ItemServiceTest` 참고).

## 4. adapter.out.persistence
- `adapter/out/persistence/<기능>/`에 `…JpaEntity`, `…JpaRepository`, `…PersistenceAdapter`(포트 구현). 모두 package-private.
- 엔티티 ↔ 도메인 변환은 어댑터 안에서만 한다.
- `src/main/resources/db/migration/V{다음번호}__설명.sql` 추가. 기존 마이그레이션은 수정하지 않는다.
- 테스트: `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + `@Import({TestcontainersConfiguration.class, 어댑터.class})` + `@Testcontainers(disabledWithoutDocker = true)`.

## 5. adapter.in.web
- `adapter/in/web/<기능>/`에 컨트롤러, `…Request`(Bean Validation), `…Response`(record, `from(도메인)`).
- 컨트롤러는 `port.in` 인터페이스에만 의존한다.
- 생성은 `201 Created` + `Location`.
- 테스트: `@WebMvcTest(컨트롤러.class)` + `@MockitoBean` 유스케이스. 성공, 검증 실패(400, `application/problem+json`), 없음(404)을 확인.

## 6. 검증
```bash
cd backend && ./gradlew build
```
`HexagonalArchitectureTest`가 실패하면 의존 방향을 고친다. 규칙을 우회하도록 테스트를 수정하지 않는다.

## 7. 마무리
- `backend/README.md`의 API 표를 갱신한다.
- 프론트 화면이 필요하면 `add-frontend-feature` 스킬로 이어서 진행한다.
