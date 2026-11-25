"use client";

import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "next/navigation";
import { Suspense } from "react";

import { NotFoundMessage } from "@/components/not-found-message";
import { Card, CardContent } from "@/components/ui/card";
import { ApiError } from "@/lib/api/client";
import { getItem, itemKeys, parseItemId } from "@/lib/api/items";

import { EditItemForm } from "./edit-item-form";

// 정적 export 는 빌드 때 모르는 id 로 경로(/items/[id])를 만들 수 없어서 /items/edit?id=1 처럼 쿼리로 받는다
export default function EditItemPage() {
  return (
    <main className="mx-auto flex w-full max-w-xl flex-col gap-8 px-4 py-12">
      <h1 className="text-2xl font-semibold">항목 수정</h1>
      {/* useSearchParams 를 쓰는 컴포넌트는 Suspense 안에 둔다 (정적 export 빌드 조건) */}
      <Suspense fallback={<Loading />}>
        <EditItem />
      </Suspense>
    </main>
  );
}

function EditItem() {
  const id = parseItemId(useSearchParams().get("id"));
  if (id === null) {
    return <NotFoundMessage />;
  }
  return <EditItemLoader id={id} />;
}

function EditItemLoader({ id }: { id: number }) {
  const { data, isPending, error } = useQuery({
    queryKey: itemKeys.detail(id),
    queryFn: ({ signal }) => getItem(id, signal),
  });

  if (isPending) {
    return <Loading />;
  }
  if (error instanceof ApiError && error.status === 404) {
    return <NotFoundMessage />;
  }
  if (error) {
    return (
      <p className="text-sm text-destructive">
        항목을 불러오지 못했습니다. 백엔드가 실행 중인지 확인해 주세요.
      </p>
    );
  }
  return (
    <Card>
      <CardContent>
        <EditItemForm item={data} />
      </CardContent>
    </Card>
  );
}

function Loading() {
  return (
    <p role="status" className="text-sm text-muted-foreground">
      불러오는 중...
    </p>
  );
}
