import { PageHero, SectionHeading } from "@/components/client/ClientShell";
import { DestinationCard } from "@/components/client/cards";
import { CallToAction } from "@/components/client/sections";
import { backendApi } from "@/lib/backend-api";
import type { DestinationDTO } from "@/types/domain";
import { IMG } from "@/lib/content";

export const metadata = { title: "Destinations — WildTrail" };

export default async function DestinationsPage() {
  const destinations = await backendApi<DestinationDTO[]>("/api/destinations");
  return (
    <>
      <PageHero eyebrow="Destinations" title="Sri Lanka's wildest corners" text="Six national parks, each with its own character, landscapes and star species." image={IMG.lake} />
      <section className="bg-jungle-950 py-24">
        <div className="mx-auto max-w-7xl px-5 lg:px-8">
          <SectionHeading light eyebrow="Choose your park" title="Where will the trail take you?" text="Hover a card to see what lives there and which safaris we run." />
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {destinations.map((d, i) => <DestinationCard key={d.id} d={d} i={i} />)}
          </div>
        </div>
      </section>
      <CallToAction />
    </>
  );
}

