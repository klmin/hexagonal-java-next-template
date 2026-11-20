"use server";

import { refresh } from "next/cache";

import { ApiError } from "@/lib/api/client";
import { createItem } from "@/lib/api/items";

export type CreateItemState = {
  error?: string;
};

export async function createItemAction(
  _prev: CreateItemState,
  formData: FormData,
): Promise<CreateItemState> {
  const name = String(formData.get("name") ?? "").trim();
  const description = String(formData.get("description") ?? "").trim();

  if (!name) {
    return { error: "이름을 입력해 주세요." };
  }

  try {
    await createItem({ name, description: description || undefined });
  } catch (e) {
    if (e instanceof ApiError) {
      return { error: e.message };
    }
    return { error: "백엔드에 연결할 수 없습니다." };
  }

  refresh();
  return {};
}
