import { Suspense } from "react";

import { Card, CardContent } from "@/components/ui/card";

import { CreateItemForm } from "./items/create-item-form";
import { ItemList } from "./items/item-list";

export default function Home() {
  return (
    <main className="mx-auto flex w-full max-w-xl flex-col gap-8 px-4 py-12">
      <header>
        <h1 className="text-2xl font-semibold">Items</h1>
        <p className="text-sm text-muted-foreground">
          Spring Boot <code>/api/items</code> 연동 샘플
        </p>
      </header>

      {/* 폼과 목록은 ?cursor= 를 읽는다(useSearchParams). 정적 export 빌드 조건이라 Suspense 로 감싼다 */}
      <Suspense
        fallback={
          <p role="status" className="text-sm text-muted-foreground">
            불러오는 중...
          </p>
        }
      >
        <Card>
          <CardContent>
            <CreateItemForm />
          </CardContent>
        </Card>

        <section>
          <ItemList />
        </section>
      </Suspense>
    </main>
  );
}
