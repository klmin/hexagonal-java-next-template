import { afterEach, describe, expect, it, vi } from "vitest";

import { ApiError, request } from "./client";

vi.mock("next/server", () => ({ connection: async () => {} }));

function mockFetch(status: number, body: unknown) {
  const fetchMock = vi.fn().mockResolvedValue(
    new Response(JSON.stringify(body), {
      status,
      headers: { "Content-Type": "application/json" },
    }),
  );
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("request", () => {
  it("BACKEND_URL 기준으로 호출하고 JSON을 돌려준다", async () => {
    const fetchMock = mockFetch(200, [{ id: 1 }]);

    await expect(request("/api/items")).resolves.toEqual([{ id: 1 }]);
    expect(String(fetchMock.mock.calls[0][0])).toBe("http://localhost:8080/api/items");
  });

  it("Problem Details를 ApiError로 바꾸고 필드 오류를 메시지에 붙인다", async () => {
    mockFetch(400, {
      status: 400,
      detail: "입력값을 확인해 주세요.",
      errors: [{ field: "name", message: "must not be blank" }],
    });

    const error = await request("/api/items").catch((e: unknown) => e);

    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(400);
    expect((error as ApiError).message).toBe("입력값을 확인해 주세요. (name: must not be blank)");
  });
});
