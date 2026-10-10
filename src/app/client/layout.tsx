import type { ReactNode } from "react";
import ClientShell from "@/components/client/ClientShell";

export const dynamic = "force-dynamic";

export default function ClientLayout({ children }: { children: ReactNode }) {
  return <ClientShell>{children}</ClientShell>;
}

