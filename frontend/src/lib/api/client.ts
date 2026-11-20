import "server-only";

import { connection } from "next/server";

import { serverEnv } from "@/lib/env";

// 백엔드 오류 응답 (RFC 9457 Problem Details)
type ProblemDetail = {
  title?: string;
  status?: number;
  detail?: string;
  errors?: { field: string; message: string }[];
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

async function toApiError(res: Response): Promise<ApiError> {
  let message = `요청에 실패했습니다 (${res.status})`;
  try {
    const problem = (await res.json()) as ProblemDetail;
    message = problem.detail ?? problem.title ?? message;
    if (problem.errors?.length) {
      message += ` (${problem.errors.map((e) => `${e.field}: ${e.message}`).join(", ")})`;
    }
  } catch {
    // JSON이 아니면 기본 메시지 사용
  }
  return new ApiError(res.status, message);
}

export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  // 빌드 시점이 아니라 요청 시점의 환경변수를 읽도록
  await connection();
  const url = new URL(path, serverEnv().BACKEND_URL);

  const res = await fetch(url, {
    // 백엔드가 응답하지 않을 때 화면이 계속 로딩 상태로 남지 않게
    signal: AbortSignal.timeout(10_000),
    ...init,
    headers: { "Content-Type": "application/json", ...init?.headers },
  });
  if (!res.ok) {
    throw await toApiError(res);
  }
  return (await res.json()) as T;
}
