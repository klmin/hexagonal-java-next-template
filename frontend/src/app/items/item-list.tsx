import Link from "next/link";
import { unstable_rethrow } from "next/navigation";

import { listItems, type Item } from "@/lib/api/items";

import { deleteItemAction } from "./actions";

// cacheComponents 환경이라 <Suspense> 안에서 렌더링해야 함
export async function ItemList() {
  let items: Item[];
  try {
    items = await listItems();
  } catch (err) {
    // try 안에서 notFound(), redirect() 등을 쓰면 Next.js 가 처리하게 다시 던진다
    unstable_rethrow(err);
    console.error("ItemList failed", err);
    return (
      <p className="text-sm text-red-600">
        목록을 불러오지 못했습니다. 백엔드가 실행 중인지 확인해 주세요.
      </p>
    );
  }

  if (items.length === 0) {
    return <p className="text-sm text-zinc-500">아직 항목이 없습니다.</p>;
  }

  return (
    <ul className="divide-y divide-zinc-200 dark:divide-zinc-800">
      {items.map((item) => (
        <li key={item.id} className="flex items-start justify-between gap-4 py-3">
          <div>
            <p className="font-medium">{item.name}</p>
            {item.description && <p className="text-sm text-zinc-500">{item.description}</p>}
          </div>
          <div className="flex shrink-0 items-center gap-3 text-sm">
            <Link href={`/items/${item.id}/edit`} className="underline">
              수정
            </Link>
            <form action={deleteItemAction.bind(null, item.id)}>
              <button type="submit" className="text-red-600 underline">
                삭제
              </button>
            </form>
          </div>
        </li>
      ))}
    </ul>
  );
}
