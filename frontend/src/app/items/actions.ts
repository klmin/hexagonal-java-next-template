"use server";

import { refresh } from "next/cache";

import { ApiError } from "@/lib/api/client";
import { createItem } from "@/lib/api/items";

export type CreateItemState = {
  error?: string;
  // 실패했을 때 입력값을 다시 채우기 위한 값. 폼 액션이 끝나면 React가 폼을 비우기 때문
  values?: { name: string; description: string };
};

export async function createItemAction(
  _prev: CreateItemState,
  formData: FormData,
): Promise<CreateItemState> {
  const name = String(formData.get("name") ?? "").trim();
  const description = String(formData.get("description") ?? "").trim();
  const values = { name, description };

  if (!name) {
    return { error: "이름을 입력해 주세요.", values };
  }

  try {
    await createItem({ name, description: description || undefined });
  } catch (e) {
    if (e instanceof ApiError) {
      return { error: e.message, values };
    }
    console.error("createItemAction failed", e);
    return { error: "백엔드에 연결할 수 없습니다.", values };
  }

  refresh();
  return {};
}
