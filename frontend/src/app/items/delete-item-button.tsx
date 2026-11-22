"use client";

import { useActionState } from "react";

import { deleteItemAction, type DeleteItemState } from "./actions";

const initialState: DeleteItemState = {};

export function DeleteItemButton({ id, name }: { id: number; name: string }) {
  const [state, formAction, pending] = useActionState(
    deleteItemAction.bind(null, id),
    initialState,
  );

  return (
    <form action={formAction} className="flex flex-col items-end gap-1">
      <button
        type="submit"
        disabled={pending}
        aria-label={`${name} 삭제`}
        className="text-red-600 underline disabled:opacity-50"
      >
        {pending ? "삭제 중..." : "삭제"}
      </button>
      {state.error && (
        <p role="alert" className="text-xs text-red-600">
          {state.error}
        </p>
      )}
    </form>
  );
}
