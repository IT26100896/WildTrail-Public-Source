/* eslint-disable @next/next/no-img-element */
import Link from "next/link";
import { backendApi } from "@/lib/backend-api";
import type { ReviewRow } from "@/types/domain";
import type { PackageDTO } from "@/types/domain";
import type { VehicleDTO } from "@/types/domain";
import type { DestinationDTO } from "@/types/domain";
import { FAQ, GALLERY, IMG, STEPS, WHY, WILDLIFE } from "@/lib/content";
import { SectionHeading } from "./ClientShell";
import { DestinationCard, PackageCard, VehicleCard, WildlifeCard } from "./cards";
import Hero from "./Hero";
import { Faq, Gallery, ReviewCarousel, type ReviewItem } from "./interactive";
import QuickBooking from "./QuickBooking";

export async function loadPublicReviews(): Promise<ReviewItem[]> {
  return (await backendApi<ReviewRow[]>("/api/reviews")).map((review) => ({
    ...review, createdAt: new Date(review.createdAt).toISOString(),
  }));
}

export function WhyChoose() {
  return (
    <section className="mx-auto max-w-7xl px-5 py-24 lg:px-8">
      <div className="grid items-center gap-14 lg:grid-cols-2">
        <div className="reveal relative">
          <img src={IMG.elephantJungle} alt="Elephant in the jungle" className="h-[520px] w-full rounded-2xl object-cover" />
          <div className="absolute -bottom-8 -right-4 hidden w-60 rounded-2xl bg-jungle-900 p-6 text-white shadow-2xl md:block">
            <p className="font-display text-3xl text-gold">Your safari</p>
            <p className="mt-1 text-sm text-white/70">Choose a park, package and vehicle that suit your visit.</p>
          </div>
        </div>
        <div>
          <SectionHeading center={false} eyebrow="Why choose WildTrail?" title="Safaris crafted by people who live beside the wild." text="We are a family of trackers, drivers and naturalists from the villages bordering Yala. Every safari is designed around respect for wildlife — and your comfort." />
          <div className="grid gap-5 sm:grid-cols-2">
            {WHY.map((w, i) => (
              <div key={w.title} className={`reveal reveal-delay-${i % 3} rounded-2xl border border-jungle-900/10 bg-white p-6 transition hover:-translate-y-1 hover:border-gold/50 hover:shadow-lg`}>
                <div className="text-3xl">{w.icon}</div>
                <h3 className="mt-3 font-display text-lg font-semibold text-jungle-950">{w.title}</h3>
                <p className="mt-1.5 text-sm text-charcoal/70">{w.text}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

export function HowItWorks() {
  // The booking steps link to the full wizard, where each choice is validated.
  return (
    <section className="bg-cream/60 py-24">
      <div className="mx-auto max-w-7xl px-5 lg:px-8">
        <SectionHeading eyebrow="How it works" title="Five steps to the wild" />
        <ol className="relative grid gap-8 md:grid-cols-5">
          <div className="pointer-events-none absolute left-[10%] right-[10%] top-10 hidden h-px border-t border-dashed border-gold/60 md:block" />
          {STEPS.map((s, i) => (
            <li key={s.n} className={`reveal reveal-delay-${i % 3} relative text-center`}>
              <Link href={s.href} aria-label={s.title} className="group block h-full rounded-2xl p-2 transition hover:bg-white/40 focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-gold">
              <div className="mx-auto grid h-20 w-20 place-items-center rounded-full border border-gold/40 bg-ivory text-3xl shadow-lg transition group-hover:bg-jungle-900 group-focus-visible:bg-jungle-900">
                <span className="animate-bob" style={{ animationDelay: `${i * 0.3}s` }}>{s.icon}</span>
              </div>
              <p className="mt-4 font-display text-sm font-bold text-gold">{s.n}</p>
              <h3 className="font-display text-lg font-semibold text-jungle-950">{s.title}</h3>
              <p className="mt-1 text-sm text-charcoal/65">{s.text}</p>
              </Link>
            </li>
          ))}
        </ol>
      </div>
    </section>
  );
}

export function CallToAction() {
  return (
    <section className="relative overflow-hidden py-32">
      <img src={IMG.sunset} alt="" className="absolute inset-0 h-full w-full object-cover" loading="lazy" />
      <div className="absolute inset-0 bg-jungle-950/70" />
      <div className="reveal relative mx-auto max-w-3xl px-5 text-center">
        <p className="eyebrow">Your adventure awaits</p>
        <h2 className="mt-4 font-display text-4xl font-semibold text-white md:text-6xl">The leopard won&apos;t wait. <em className="text-gold">Neither should you.</em></h2>
        <p className="mx-auto mt-5 max-w-xl text-white/75">Reserve your jeep today. Changes and cancellation are available up to 24 hours before departure.</p>
        <div className="mt-9 flex flex-wrap justify-center gap-4">
          <Link href="/client/booking" className="btn-gold">Book Your Safari</Link>
          <Link href="/client/contact" className="btn-ghost">Talk to an Expert</Link>
        </div>
      </div>
    </section>
  );
}

export function FaqSection() {
  return (
    <section className="mx-auto max-w-4xl px-5 py-24">
      <SectionHeading eyebrow="FAQ" title="Questions before the wild?" />
      <div className="reveal"><Faq items={FAQ} /></div>
    </section>
  );
}

export default async function HomePage() {
  const [destinations, packages, vehicles, reviews] = await Promise.all([
    backendApi<DestinationDTO[]>("/api/destinations"),
    backendApi<PackageDTO[]>("/api/packages"),
    backendApi<VehicleDTO[]>("/api/vehicles"),
    loadPublicReviews(),
  ]);

  return (
    <>
      {/* 1. Hero */}
      <Hero />
      {/* 2. Quick booking */}
      <QuickBooking destinations={destinations} />
      {/* 3. Why choose */}
      <WhyChoose />

      {/* 4. Destinations */}
      <section className="bg-jungle-950 py-24">
        <div className="mx-auto max-w-7xl px-5 lg:px-8">
          <SectionHeading light eyebrow="Safari destinations" title="Six parks. Endless stories." text="From leopard country in the deep south to the elephant gathering of the north-central plains." />
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {destinations.map((d, i) => <DestinationCard key={d.id} d={d} i={i} />)}
          </div>
        </div>
      </section>

      {/* 5. Packages */}
      <section className="mx-auto max-w-7xl px-5 py-24 lg:px-8">
        <SectionHeading eyebrow="Popular safari packages" title="Choose your experience" />
        <div className="grid gap-7 sm:grid-cols-2 lg:grid-cols-3">
          {packages.slice(0, 6).map((p, i) => <PackageCard key={p.id} p={p} i={i} />)}
        </div>
        <div className="mt-12 text-center"><Link href="/client/packages" className="btn-dark">View all packages</Link></div>
      </section>

      {/* 6. Wildlife */}
      <section className="bg-cream/60 py-24">
        <div className="mx-auto max-w-7xl px-5 lg:px-8">
          <SectionHeading eyebrow="Wildlife highlights" title="Who you'll meet out there" text="Hover over each card to learn where and when to find them." />
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
            {WILDLIFE.map((w, i) => (
              <div key={w.name} className={i === 0 ? "sm:col-span-2 lg:row-span-1" : ""}><WildlifeCard w={w} i={i} /></div>
            ))}
          </div>
        </div>
      </section>

      {/* 7. Vehicles */}
      <section className="relative overflow-hidden bg-jungle-950 py-24">
        <div className="pointer-events-none absolute -left-32 top-20 h-96 w-96 rounded-full bg-jungle-800/50 blur-3xl" />
        <div className="relative mx-auto max-w-7xl px-5 lg:px-8">
          <SectionHeading light eyebrow="Safari vehicles" title="Ride in comfort, deep into the wild" />
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {vehicles.map((v, i) => <VehicleCard key={v.id} v={v} i={i} />)}
          </div>
        </div>
      </section>

      {/* 8. How it works */}
      <HowItWorks />

      {/* 9. Gallery */}
      <section className="mx-auto max-w-7xl px-5 py-24 lg:px-8">
        <SectionHeading eyebrow="Gallery" title="Moments from the trail" />
        <Gallery items={GALLERY.slice(0, 6)} />
        <div className="mt-8 text-center"><Link href="/client/gallery" className="btn-dark">Open full gallery</Link></div>
      </section>

      {/* 10. Reviews */}
      <section className="bg-jungle-900 py-24">
        <div className="mx-auto max-w-7xl px-5 lg:px-8">
          <SectionHeading light eyebrow="Guest reviews" title="Stories from our travellers" />
          <ReviewCarousel reviews={reviews} />
        </div>
      </section>

      {/* 11. CTA */}
      <CallToAction />

      {/* 12. About */}
      <section className="mx-auto grid max-w-7xl items-center gap-14 px-5 py-24 lg:grid-cols-2 lg:px-8">
        <div>
          <SectionHeading center={false} eyebrow="About WildTrail" title="Born on the edge of Yala." text="WildTrail began with one jeep and a tracker named Sunil who knew every waterhole in Block 1. Today our team of 40 guides shares that same patience and passion — with a promise to protect the wilderness we call home." />
          <Link href="/client/about" className="reveal btn-dark">Our story</Link>
        </div>
        <div className="reveal grid grid-cols-2 gap-4">
          <img src={IMG.leopardTree} alt="Leopard on a tree" className="h-72 w-full rounded-2xl object-cover" loading="lazy" />
          <img src={IMG.jeepGroup} alt="Safari group" className="mt-12 h-72 w-full rounded-2xl object-cover" loading="lazy" />
        </div>
      </section>

      {/* 13. FAQ */}
      <div className="bg-cream/50"><FaqSection /></div>
      {/* 14. Footer → rendered by ClientShell */}
    </>
  );
}

