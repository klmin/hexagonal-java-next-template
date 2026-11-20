import { expect, test } from "@playwright/test";

test("항목을 추가하면 목록에 보인다", async ({ page }) => {
  const name = `e2e-${Date.now()}`;

  await page.goto("/");
  await page.getByPlaceholder("이름").fill(name);
  await page.getByPlaceholder("설명 (선택)").fill("playwright");
  await page.getByRole("button", { name: "추가" }).click();

  await expect(page.getByText(name)).toBeVisible();

  // 새로고침해도 남아 있으면 백엔드에 저장된 것
  await page.reload();
  await expect(page.getByText(name)).toBeVisible();
});

test("이름이 공백뿐이면 오류를 보여 준다", async ({ page }) => {
  await page.goto("/");
  await page.getByPlaceholder("이름").fill("   ");
  await page.getByRole("button", { name: "추가" }).click();

  // Next.js 라우트 안내 요소도 role=alert 라서 폼 안에서 찾는다
  await expect(page.locator("form").getByRole("alert")).toHaveText("이름을 입력해 주세요.");
});

test("없는 페이지는 404 화면을 보여 준다", async ({ page }) => {
  const response = await page.goto("/no-such-page");

  expect(response?.status()).toBe(404);
  await expect(page.getByText("페이지를 찾을 수 없습니다")).toBeVisible();
});
