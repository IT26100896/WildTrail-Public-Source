/* eslint-disable @next/next/no-img-element */
import { PageHero, SectionHeading } from "@/components/client/ClientShell";
import { CallToAction, WhyChoose } from "@/components/client/sections";
import { backendApi } from "@/lib/backend-api";
import { IMG, px } from "@/lib/content";

export const metadata = { title: "About — WildTrail" };



function Editorial({ eyebrow, title, text, img, flip = false }: { eyebrow: string; title: string; text: string[]; img: string; flip?: boolean }) {
  return (
    <section className="mx-auto grid max-w-7xl items-center gap-12 px-5 py-20 lg:grid-cols-2 lg:px-8">
      <img src={img} alt="" loading="lazy" className={`reveal h-[460px] w-full rounded-2xl object-cover ${flip ? "lg:order-2" : ""}`} />
      <div className="reveal">
        <p className="eyebrow">{eyebrow}</p>
        <h2 className="mt-3 font-display text-4xl font-semibold text-jungle-950 md:text-5xl">{title}</h2>
        {text.map((t) => <p key={t} className="mt-5 leading-relaxed text-charcoal/70">{t}</p>)}
      </div>
    </section>
  );
}

export default async function AboutPage() {
  const guides = await backendApi<{ name: string; role: string }[]>("/api/team");
  return (
    <>
      <PageHero eyebrow="About WildTrail" title="Plan your journey into the wild." image={IMG.leopardTree} />
      <Editorial
        eyebrow="Our story"
        title="Safari planning made simple."
        text={[
          "Browse safari destinations, compare package options and choose an available vehicle for your visit.",
          "Your account keeps your reservations, payment statements and reviews together. Our staff manage packages, vehicles and guide assignments.",
        ]}
        img={IMG.jeepRoad}
      />
      <section className="bg-jungle-950 py-20 text-white">
        <div className="mx-auto grid max-w-7xl gap-10 px-5 md:grid-cols-3 lg:px-8">
          {[
            ["Our mission", "To connect people with Sri Lanka's wildlife in ways that inspire them to protect it."],
            ["Our vision", "A future where local communities thrive because their wilderness thrives."],
            ["Our values", "Patience over pursuit. Respect over spectacle. Community over profit."],
          ].map(([t, d]) => (
            <div key={t} className="reveal border-l border-gold/40 pl-6">
              <h3 className="font-display text-2xl text-gold">{t}</h3>
              <p className="mt-3 text-white/70">{d}</p>
            </div>
          ))}
        </div>
      </section>
      <section className="mx-auto max-w-7xl px-5 py-20 lg:px-8">
        <SectionHeading eyebrow="Our guides" title="The eyes and ears of the jungle" />
        {!guides.length && <p className="text-center text-charcoal/60">Guide details will appear here once staff have registered the safari team.</p>}
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {guides.map((g, i) => (
            <div key={g.name} className={`reveal reveal-delay-${i % 3} card p-7 text-center`}>
              <span className="mx-auto grid h-20 w-20 place-items-center rounded-full border-2 border-gold bg-jungle-900 font-display text-2xl text-gold">{g.name.split(" ").map((s) => s[0]).join("")}</span>
              <h3 className="mt-4 font-display text-lg font-semibold text-jungle-950">{g.name}</h3>
              <p className="text-xs font-semibold uppercase tracking-wider text-gold-dark">{g.role}</p>
            </div>
          ))}
        </div>
      </section>
      <Editorial
        flip
        eyebrow="Responsible tourism"
        title="We leave nothing but tyre tracks."
        text={[
          "Our drivers follow strict sighting etiquette: no more than three jeeps per sighting, engines off, and a minimum distance from every animal. We never off-road or use calls to lure wildlife.",
          "Please keep a safe distance from wildlife, follow your guide and leave no litter in the park.",
        ]}
        img={px(32782089, 1400)}
      />
      <WhyChoose />
      <CallToAction />
    </>
  );
}

