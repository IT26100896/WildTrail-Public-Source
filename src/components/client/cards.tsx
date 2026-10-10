/* eslint-disable @next/next/no-img-element */
import Link from "next/link";
import type { PackageDTO } from "@/types/domain";
import type { VehicleDTO } from "@/types/domain";
import type { DestinationDTO } from "@/types/domain";
import { fmtTime, money } from "@/lib/content";

export function DestinationCard({ d, i = 0 }: { d: DestinationDTO; i?: number }) {
  return (
    <article className={`reveal reveal-delay-${i % 3} group relative h-[460px] overflow-hidden rounded-2xl bg-jungle-950`}>
      <img src={d.imageUrl} alt={d.name} loading="lazy" className="absolute inset-0 h-full w-full object-cover transition duration-[1.2s] group-hover:scale-110" />
      <div className="absolute inset-0 bg-gradient-to-t from-jungle-950 via-jungle-950/50 to-transparent transition group-hover:via-jungle-950/70" />
      <div className="absolute right-4 top-4 rounded-full bg-jungle-950/70 px-3 py-1 text-xs font-semibold text-gold backdrop-blur">
        from {money(d.startingPrice)}
      </div>
      <div className="absolute inset-x-0 bottom-0 p-6 text-white">
        <p className="text-[11px] uppercase tracking-[0.25em] text-sand">📍 {d.location}</p>
        <h3 className="mt-1 font-display text-2xl font-semibold">{d.name}</h3>
        <div className="grid grid-rows-[0fr] transition-all duration-500 group-hover:grid-rows-[1fr]">
          <div className="overflow-hidden">
            <p className="mt-3 text-sm leading-relaxed text-white/75">{d.description}</p>
            <dl className="mt-3 space-y-1 text-xs">
              <div><dt className="inline text-gold">Best wildlife: </dt><dd className="inline text-white/80">{d.bestWildlife}</dd></div>
              <div><dt className="inline text-gold">Safari types: </dt><dd className="inline text-white/80">{d.safariTypes}</dd></div>
            </dl>
          </div>
        </div>
        <Link href={`/client/booking?destination=${d.id}`} className="mt-4 inline-flex items-center gap-2 text-xs font-bold uppercase tracking-[0.2em] text-gold transition group-hover:gap-3">
          Explore <span aria-hidden>→</span>
        </Link>
      </div>
    </article>
  );
}

export function PackageCard({ p, i = 0 }: { p: PackageDTO; i?: number }) {
  return (
    <article className={`reveal reveal-delay-${i % 3} card group flex flex-col overflow-hidden transition duration-500 hover:-translate-y-1.5 hover:shadow-2xl`}>
      <div className="relative h-56 overflow-hidden">
        <img src={p.imageUrl} alt={p.name} loading="lazy" className="h-full w-full object-cover transition duration-700 group-hover:scale-110" />
        <span className="absolute left-4 top-4 rounded-full bg-gold px-3 py-1 text-[10px] font-bold uppercase tracking-widest text-jungle-950">
          {p.typeLabel}
        </span>
      </div>
      <div className="flex flex-1 flex-col p-6">
        <p className="text-xs font-medium uppercase tracking-[0.2em] text-jungle-700">📍 {p.destinationName}</p>
        <h3 className="mt-1.5 font-display text-xl font-semibold text-jungle-950">{p.name}</h3>
        <p className="mt-2 line-clamp-2 text-sm text-charcoal/70">{p.description}</p>
        <div className="mt-4 grid grid-cols-3 gap-2 border-y border-jungle-900/10 py-3 text-center text-xs text-charcoal/70">
          <div><div className="text-base">⏱</div>{p.durationHours} hrs</div>
          <div><div className="text-base">🌅</div>{fmtTime(p.startTime)}</div>
          <div><div className="text-base">👥</div>Max {p.maxGuests}</div>
        </div>
        <div className="mt-auto flex items-end justify-between pt-5">
          <div>
            <p className="text-[10px] uppercase tracking-widest text-charcoal/50">From</p>
            <p className="font-display text-2xl font-bold text-jungle-900">{money(p.basePrice)}</p>
            <p className="text-[11px] text-charcoal/50">{p.pricingNote}</p>
          </div>
          <Link href={`/client/booking?destination=${p.destinationId}&package=${p.id}`} className="btn-dark !px-5 !py-2.5">
            Book Now
          </Link>
        </div>
      </div>
    </article>
  );
}

export function VehicleCard({ v, i = 0 }: { v: VehicleDTO; i?: number }) {
  return (
    <article className={`reveal reveal-delay-${i % 3} group overflow-hidden rounded-2xl border border-white/10 bg-jungle-900/60 text-white transition hover:border-gold/50`}>
      <div className="relative h-52 overflow-hidden">
        <img src={v.imageUrl} alt={v.name} loading="lazy" className="h-full w-full object-cover transition duration-700 group-hover:scale-105" />
        <span className={`absolute right-3 top-3 rounded-full px-3 py-1 text-[10px] font-bold uppercase tracking-widest ${v.available ? "bg-emerald-500/90 text-white" : "bg-red-500/90 text-white"}`}>
          {v.available ? "Available" : "Unavailable"}
        </span>
      </div>
      <div className="p-6">
        <p className="eyebrow !text-[10px]">{v.categoryLabel}</p>
        <h3 className="mt-1 font-display text-xl font-semibold">{v.name}</h3>
        <div className="mt-3 flex gap-4 text-xs text-white/70">
          <span>👥 {v.capacity} seats</span>
          {v.fourByFour && <span>⚙️ 4x4</span>}
        </div>
        <ul className="mt-4 flex flex-wrap gap-1.5">
          {v.allFeatures.slice(0, 6).map((f) => (
            <li key={f} className="rounded-full border border-white/10 bg-white/5 px-2.5 py-1 text-[11px] text-white/80">{f}</li>
          ))}
        </ul>
        <div className="mt-5 flex items-end justify-between border-t border-white/10 pt-4">
          <div>
            <p className="font-display text-2xl font-bold text-gold">{money(v.price)}</p>
            <p className="text-[11px] text-white/50">per safari hire</p>
          </div>
          <Link href="/client/booking" className="text-xs font-bold uppercase tracking-widest text-gold hover:text-sand">Reserve →</Link>
        </div>
      </div>
    </article>
  );
}

export function WildlifeCard({ w, i = 0 }: { w: { name: string; latin: string; img: string; desc: string; where: string; best: string }; i?: number }) {
  return (
    <article className={`reveal reveal-delay-${i % 3} group relative h-96 overflow-hidden rounded-2xl`}>
      <img src={w.img} alt={w.name} loading="lazy" className="absolute inset-0 h-full w-full object-cover transition duration-700 group-hover:scale-110" />
      <div className="absolute inset-0 bg-gradient-to-t from-jungle-950/95 via-jungle-950/20 to-transparent" />
      <div className="absolute inset-x-0 bottom-0 translate-y-[calc(100%-5.5rem)] p-6 text-white transition-transform duration-500 group-hover:translate-y-0">
        <h3 className="font-display text-2xl font-semibold">{w.name}</h3>
        <p className="text-xs italic text-sand">{w.latin}</p>
        <p className="mt-4 text-sm text-white/80">{w.desc}</p>
        <div className="mt-4 grid grid-cols-2 gap-3 text-xs">
          <div><p className="text-gold">Where to spot</p><p className="text-white/80">{w.where}</p></div>
          <div><p className="text-gold">Best time</p><p className="text-white/80">{w.best}</p></div>
        </div>
      </div>
    </article>
  );
}

export function Stars({ n, size = "text-sm" }: { n: number; size?: string }) {
  return (
    <span className={`${size} tracking-wider`} aria-label={`${n} out of 5 stars`}>
      <span className="text-gold">{"★".repeat(n)}</span>
      <span className="text-charcoal/20">{"★".repeat(5 - n)}</span>
    </span>
  );
}

