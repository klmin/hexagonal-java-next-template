import Link from "next/link";

export default function NotFound() {
  return (
    <main className="mx-auto flex w-full max-w-xl flex-col gap-4 px-4 py-12">
      <h1 className="text-xl font-semibold">페이지를 찾을 수 없습니다</h1>
      <Link href="/" className="text-sm underline">
        처음으로
      </Link>
    </main>
  );
}
