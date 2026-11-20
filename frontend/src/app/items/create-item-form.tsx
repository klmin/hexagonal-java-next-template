"use client";

import { useActionState } from "react";

import { createItemAction, type CreateItemState } from "./actions";

const initialState: CreateItemState = {};

export function CreateItemForm() {
  const [state, formAction, pending] = useActionState(createItemAction, initialState);

  return (
    <form action={formAction} className="flex flex-col gap-3">
      <input
        name="name"
        placeholder="이름"
        maxLength={100}
        required
        className="rounded border border-zinc-300 px-3 py-2 dark:border-zinc-700 dark:bg-zinc-900"
      />
      <textarea
        name="description"
        placeholder="설명 (선택)"
        maxLength={500}
        rows={2}
        className="rounded border border-zinc-300 px-3 py-2 dark:border-zinc-700 dark:bg-zinc-900"
      />
      <button
        type="submit"
        disabled={pending}
        className="self-start rounded bg-zinc-900 px-4 py-2 text-white disabled:opacity-50 dark:bg-zinc-100 dark:text-zinc-900"
      >
        {pending ? "추가 중..." : "추가"}
      </button>
      {state.error && (
        <p role="alert" className="text-sm text-red-600">
          {state.error}
        </p>
      )}
    </form>
  );
}
