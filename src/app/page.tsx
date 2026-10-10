import ClientShell from "@/components/client/ClientShell";
import HomePage from "@/components/client/sections";

export const dynamic = "force-dynamic";

export default function Root() {
  return (
    <ClientShell>
      <HomePage />
    </ClientShell>
  );
}

