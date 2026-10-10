/* eslint-disable @next/next/no-img-element */
import Link from "next/link";
import { PageHero, SectionHeading } from "@/components/client/ClientShell";
import { HowItWorks, CallToAction } from "@/components/client/sections";
import { backendApi } from "@/lib/backend-api";
import type { PackageDTO } from "@/types/domain";
import { IMG, fmtTime, money } from "@/lib/content";

export const metadata = { title: "Safari Experiences — WildTrail" };

export default async function SafarisPage() {
  const packages = await backendApi<PackageDTO[]>("/api/packages");
  // Group packages by their polymorphic type label
  const groups = new Map<string, typeof packages>();
  for (const p of packages) groups.set(p.typeLabel, [...(groups.get(p.typeLabel) ?? []), p]);

  return (
    <>
      <PageHero eyebrow="Safari experiences" title="Every safari tells a different story." text="Sunrise game drives, golden-hour photography, private expeditions — choose the rhythm of your adventure." image={IMG.jeepGrass} />
      <section className="mx-auto max-w-7xl px-5 py-24 lg:px-8">
        <SectionHeading eyebrow="Safari types" title="Find your style" />
        <div className="space-y-8">
          {[...groups.entries()].map(([label, list], gi) => (
            <article key={label} className={`reveal card grid overflow-hidden md:grid-cols-[2fr_3fr] ${gi % 2 ? "md:[&>*:first-child]:order-2" : ""}`}>
              <img src={list[0].imageUrl} alt={label} className="h-72 w-full object-cover md:h-full" loading="lazy" />
              <div className="p-8 md:p-10">
                <p className="eyebrow">{list[0].pricingNote}</p>
                <h3 className="mt-2 font-display text-3xl font-semibold text-jungle-950">{label}</h3>
                <ul className="mt-5 flex flex-wrap gap-2">
                  {list[0].inclusions.map((inc) => (
                    <li key={inc} className="rounded-full bg-cream px-3 py-1 text-xs text-jungle-900">✓ {inc}</li>
                  ))}
                </ul>
                <div className="mt-6 divide-y divide-jungle-900/10 border-t border-jungle-900/10">
                  {list.map((p) => (
                    <div key={p.id} className="flex flex-wrap items-center justify-between gap-3 py-4">
                      <div>
                        <p className="font-semibold text-jungle-950">{p.name}</p>
                        <p className="text-xs text-charcoal/60">📍 {p.destinationName} · ⏱ {p.durationHours} hrs · 🌅 {fmtTime(p.startTime)} · 👥 max {p.maxGuests}</p>
                      </div>
                      <div className="flex items-center gap-4">
                        <span className="font-display text-xl font-bold text-jungle-900">{money(p.basePrice)}</span>
                        <Link href={`/client/booking?destination=${p.destinationId}&package=${p.id}`} className="btn-gold !px-5 !py-2">Book</Link>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </article>
          ))}
        </div>
      </section>
      <HowItWorks />
      <CallToAction />
    </>
  );
}

