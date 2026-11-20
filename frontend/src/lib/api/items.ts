import "server-only";

import { connection } from "next/server";

export type Item = {
  id: number;
  name: string;
  description: string | null;
  createdAt: string;
};

export type CreateItemInput = {
  name: string;
  description?: string;
};

/** RFC 9457 Problem Details (백엔드 오류 응답 형식) */
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
  // 런타임 환경변수를 읽도록 프리렌더를 여기서 멈춘다 (빌드 시점 값 고정 방지).
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
    // 본문이 JSON이 아니면 기본 메시지를 쓴다.
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
