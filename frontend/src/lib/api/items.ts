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

export function getItem(id: number): Promise<Item> {
  return request<Item>(`/api/items/${id}`);
}

export function updateItem(id: number, input: UpdateItemInput): Promise<Item> {
  return request<Item>(`/api/items/${id}`, {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function deleteItem(id: number): Promise<void> {
  return request<void>(`/api/items/${id}`, { method: "DELETE" });
}

export function createItem(input: CreateItemInput): Promise<Item> {
  return request<Item>("/api/items", {
    method: "POST",
    body: JSON.stringify(input),
  });
}
