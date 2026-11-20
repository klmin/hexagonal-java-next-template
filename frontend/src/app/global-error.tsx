"use client";

import { useEffect } from "react";

import "./globals.css";

// 루트 layout 에서 난 오류는 error.tsx 가 잡지 못한다. layout 을 대신하므로 html, body 를 직접 둔다
export default function GlobalError({ error, retry }: { error: unknown; retry: () => void }) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <html lang="ko">
      <body>
        <main className="mx-auto flex w-full max-w-xl flex-col gap-4 px-4 py-12">
          <h1 className="text-xl font-semibold">문제가 발생했습니다</h1>
          <p className="text-sm text-zinc-500">잠시 후 다시 시도해 주세요.</p>
          <button
            type="button"
            onClick={() => retry()}
            className="self-start rounded bg-zinc-900 px-4 py-2 text-white dark:bg-zinc-100 dark:text-zinc-900"
          >
            다시 시도
          </button>
        </main>
      </body>
    </html>
  );
}
