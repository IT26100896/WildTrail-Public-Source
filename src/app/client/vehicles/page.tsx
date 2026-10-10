import { PageHero, SectionHeading } from "@/components/client/ClientShell";
import { VehicleCard } from "@/components/client/cards";
import { backendApi } from "@/lib/backend-api";
import type { VehicleDTO } from "@/types/domain";
import { IMG } from "@/lib/content";

export const metadata = { title: "Safari Vehicles — WildTrail" };

export default async function VehiclesPage() {
  const vehicles = await backendApi<VehicleDTO[]>("/api/vehicles");
  return (
    <>
      <PageHero eyebrow="Our fleet" title="Built for the wild. Tuned for comfort." text="Every jeep is 4x4, serviced weekly and driven by a licensed safari driver." image={IMG.jeepRoad} />
      <section className="bg-jungle-950 py-24">
        <div className="mx-auto max-w-7xl px-5 lg:px-8">
          <SectionHeading light eyebrow="Safari vehicles" title="Pick your ride" text="Vehicle hire is priced per safari; full-day safaris count as a double hire. Luxury jeeps include a personal concierge." />
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {vehicles.map((v, i) => <VehicleCard key={v.id} v={v} i={i} />)}
          </div>
        </div>
      </section>
    </>
  );
}

