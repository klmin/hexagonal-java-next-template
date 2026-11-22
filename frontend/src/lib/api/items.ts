import "server-only";

import { request } from "./client";
import type { components } from "./schema";

// 백엔드 openapi/openapi.json 에서 생성한 타입 (npm run api:types)
export type Item = components["schemas"]["ItemResponse"];
export type CreateItemInput = components["schemas"]["CreateItemRequest"];
export type UpdateItemInput = components["schemas"]["UpdateItemRequest"];

export function listItems(): Promise<Item[]> {
  return request<Item[]>("/api/items");
}

// Server Action 인자는 브라우저를 거쳐 오므로 그대로 믿지 않는다.
// 양의 정수가 아니면 막아서 /api/items/../.. 같은 경로로 백엔드의 다른 API를 부르지 못하게 한다
export function isItemId(value: unknown): value is number {
  return typeof value === "number" && Number.isSafeInteger(value) && value > 0;
}

function itemPath(id: number): string {
  if (!isItemId(id)) {
    throw new Error(`invalid item id: ${String(id)}`);
  }
  return `/api/items/${encodeURIComponent(String(id))}`;
}

export function getItem(id: number): Promise<Item> {
  return request<Item>(itemPath(id));
}

export function updateItem(id: number, input: UpdateItemInput): Promise<Item> {
  return request<Item>(itemPath(id), {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function deleteItem(id: number): Promise<void> {
  return request<void>(itemPath(id), { method: "DELETE" });
}

export function createItem(input: CreateItemInput): Promise<Item> {
  return request<Item>("/api/items", {
    method: "POST",
    body: JSON.stringify(input),
  });
}
