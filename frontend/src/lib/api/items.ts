import "server-only";

import { connection } from "next/server";

import type { components } from "./schema";

// 백엔드 openapi/openapi.json 에서 생성한 타입 (npm run api:types)
export type Item = components["schemas"]["ItemResponse"];
export type CreateItemInput = components["schemas"]["CreateItemRequest"];

type ProblemDetail = {
  title?: string;
  status?: number;
  detail?: string;
};

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function backendUrl(path: string): Promise<string> {
  // 빌드 시점이 아니라 요청 시점의 BACKEND_URL을 읽도록
  await connection();
  const base = process.env.BACKEND_URL ?? "http://localhost:8080";
  return new URL(path, base).toString();
}

async function toApiError(res: Response): Promise<ApiError> {
  let message = `요청에 실패했습니다 (${res.status})`;
  try {
    const problem = (await res.json()) as ProblemDetail;
    message = problem.detail ?? problem.title ?? message;
  } catch {
    // JSON이 아니면 기본 메시지 사용
  }
  return new ApiError(res.status, message);
}

export async function listItems(): Promise<Item[]> {
  const res = await fetch(await backendUrl("/api/items"));
  if (!res.ok) {
    throw await toApiError(res);
  }
  return (await res.json()) as Item[];
}

export async function createItem(input: CreateItemInput): Promise<Item> {
  const res = await fetch(await backendUrl("/api/items"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    throw await toApiError(res);
  }
  return (await res.json()) as Item;
}
