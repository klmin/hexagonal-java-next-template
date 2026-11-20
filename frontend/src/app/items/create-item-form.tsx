"use client";

import { useActionState } from "react";

import { createItemAction, type CreateItemState } from "./actions";

const initialState: CreateItemState = {};

const fieldClass =
  "rounded border border-zinc-300 px-3 py-2 dark:border-zinc-700 dark:bg-zinc-900 aria-invalid:border-red-600";

export function CreateItemForm() {
  const [state, formAction, pending] = useActionState(createItemAction, initialState);
  const errorId = "create-item-error";

  return (
    <form action={formAction} className="flex flex-col gap-3">
      <label className="flex flex-col gap-1 text-sm">
        이름
        <input
          name="name"
          maxLength={100}
          required
          defaultValue={state.values?.name}
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
          defaultValue={state.values?.description}
          className={fieldClass}
        />
      </label>
      <button
        type="submit"
        disabled={pending}
        className="self-start rounded bg-zinc-900 px-4 py-2 text-white disabled:opacity-50 dark:bg-zinc-100 dark:text-zinc-900"
      >
        {pending ? "추가 중..." : "추가"}
      </button>
      {state.error && (
        <p id={errorId} role="alert" className="text-sm text-red-600">
          {state.error}
        </p>
      )}
    </form>
  );
}
