import { Suspense } from "react";

import { CreateItemForm } from "./items/create-item-form";
import { ItemList } from "./items/item-list";

export default function Home() {
  return (
    <main className="mx-auto flex w-full max-w-xl flex-col gap-8 px-4 py-12">
      <header>
        <h1 className="text-2xl font-semibold">Items</h1>
        <p className="text-sm text-zinc-500">
          Spring Boot <code>/api/items</code> 연동 샘플
        </p>
      </header>

      <section>
        <CreateItemForm />
      </section>

      <section>
        <Suspense
          fallback={
            <p role="status" className="text-sm text-zinc-500">
              불러오는 중...
            </p>
          }
        >
          <ItemList />
        </Suspense>
      </section>
    </main>
  );
}
