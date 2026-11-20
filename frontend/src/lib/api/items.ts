import "server-only";

import { request } from "./client";
import type { components } from "./schema";

// 백엔드 openapi/openapi.json 에서 생성한 타입 (npm run api:types)
export type Item = components["schemas"]["ItemResponse"];
export type CreateItemInput = components["schemas"]["CreateItemRequest"];

export function listItems(): Promise<Item[]> {
  return request<Item[]>("/api/items");
}

export function createItem(input: CreateItemInput): Promise<Item> {
  return request<Item>("/api/items", {
    method: "POST",
    body: JSON.stringify(input),
  });
}
