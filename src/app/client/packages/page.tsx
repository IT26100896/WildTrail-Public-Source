import { PageHero } from "@/components/client/ClientShell";
import { PackageExplorer } from "@/components/client/forms";
import { backendApi } from "@/lib/backend-api";
import type { PackageDTO } from "@/types/domain";
import type { DestinationDTO } from "@/types/domain";
import { IMG } from "@/lib/content";

export const metadata = { title: "Safari Packages — WildTrail" };

export default async function PackagesPage() {
  const [packages, destinations] = await Promise.all([
    backendApi<PackageDTO[]>("/api/packages"),
    backendApi<DestinationDTO[]>("/api/destinations"),
  ]);
  return (
    <>
      <PageHero eyebrow="Safari packages" title="Packages for every explorer" text="Prices are loaded live from our booking system and updated by our operations team." image={IMG.elephantsHerd} />
      <section className="mx-auto max-w-7xl px-5 py-20 lg:px-8">
        <PackageExplorer packages={packages} destinations={destinations.map((d) => ({ id: d.id, name: d.name }))} />
      </section>
    </>
  );
}

