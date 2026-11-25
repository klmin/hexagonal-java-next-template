import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
import type { ReactElement } from "react";

// TanStack Query 를 쓰는 컴포넌트용 render. 테스트마다 새 QueryClient 를 써서 캐시가 섞이지 않게 하고,
// 실패한 요청을 다시 보내지 않게 해서 오류 화면이 바로 나오게 한다
export function renderWithQuery(ui: ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return {
    queryClient,
    ...render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>),
  };
}
