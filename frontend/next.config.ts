import type { NextConfig } from "next";

// 개발 서버(next dev)에서만 /api 를 백엔드로 넘긴다. 빌드 결과(out/)에서는 nginx 가 같은 일을 한다 (nginx/default.conf.template)
const devBackendUrl = process.env.DEV_BACKEND_URL ?? "http://localhost:8080";
const isDev = process.env.NODE_ENV === "development";

const nextConfig: NextConfig = {
  // CSR: next build 가 정적 파일(out/)을 만든다. Node 서버 없이 nginx 등으로 서빙한다.
  // 정적 export 는 rewrites 를 지원하지 않아서 개발 서버에서는 끈다
  output: isDev ? undefined : "export",
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
  ...(isDev && {
    async rewrites() {
      return [{ source: "/api/:path*", destination: `${devBackendUrl}/api/:path*` }];
    },
  }),
};

export default nextConfig;
