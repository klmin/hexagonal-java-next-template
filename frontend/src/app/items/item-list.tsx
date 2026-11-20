import { listItems, type Item } from "@/lib/api/items";

/** 요청마다 백엔드에서 새로 읽는다. 반드시 <Suspense> 안에서 렌더링한다. */
export async function ItemList() {
  let items: Item[];
  try {
    items = await listItems();
  } catch {
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
        <li key={item.id} className="py-3">
          <p className="font-medium">{item.name}</p>
          {item.description && <p className="text-sm text-zinc-500">{item.description}</p>}
        </li>
      ))}
    </ul>
  );
}
