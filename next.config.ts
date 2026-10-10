import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  outputFileTracingRoot: process.cwd(),
  turbopack: { root: process.cwd() },
  async rewrites() {
    return {
      beforeFiles: [
        {
          source: "/api/:path*",
          destination: `${process.env.WILDTRAIL_API_URL ?? "http://127.0.0.1:8080"}/api/:path*`,
        },
      ],
    };
  },
};

export default nextConfig;
