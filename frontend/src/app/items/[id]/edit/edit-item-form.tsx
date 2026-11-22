"use client";

import Link from "next/link";
import { useActionState } from "react";

import type { Item } from "@/lib/api/items";

import { updateItemAction, type UpdateItemState } from "../../actions";

const initialState: UpdateItemState = {};

const fieldClass =
  "rounded border border-zinc-300 px-3 py-2 dark:border-zinc-700 dark:bg-zinc-900 aria-invalid:border-red-600";

export function EditItemForm({ item }: { item: Item }) {
  const [state, formAction, pending] = useActionState(
    updateItemAction.bind(null, item.id),
    initialState,
  );
  const errorId = "edit-item-error";

  return (
    <form action={formAction} className="flex flex-col gap-3">
      <label className="flex flex-col gap-1 text-sm">
        이름
        <input
          name="name"
          maxLength={100}
          required
          defaultValue={state.values?.name ?? item.name}
          aria-invalid={state.error ? true : undefined}
          aria-describedby={state.error ? errorId : undefined}
          className={fieldClass}
        />
      </label>
      <label className="flex flex-col gap-1 text-sm">
        설명 (선택)
        <textarea
          name="description"
          maxLength={500}
          rows={2}
          defaultValue={state.values?.description ?? item.description ?? ""}
          className={fieldClass}
        />
      </label>
      <div className="flex items-center gap-3">
        <button
          type="submit"
          disabled={pending}
          className="rounded bg-zinc-900 px-4 py-2 text-white disabled:opacity-50 dark:bg-zinc-100 dark:text-zinc-900"
        >
          {pending ? "저장 중..." : "저장"}
        </button>
        <Link href="/" className="text-sm underline">
          취소
        </Link>
      </div>
      {state.error && (
        <p id={errorId} role="alert" className="text-sm text-red-600">
          {state.error}
        </p>
      )}
    </form>
  );
}
