import type { ReactNode } from "react";

export const dynamic = "force-dynamic";
export const metadata = { title: "WildTrail Admin" };

/** Admin area root — deliberately shares NOTHING with the client website chrome. */
export default function AdminRootLayout({ children }: { children: ReactNode }) {
  return <div className="min-h-screen bg-slate-100 text-slate-800">{children}</div>;
}

