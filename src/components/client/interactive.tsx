"use client";

/* eslint-disable @next/next/no-img-element */
import { useCallback, useEffect, useState } from "react";
import { fmtDate } from "@/lib/content";

// ===================== Masonry gallery + lightbox =====================
export function Gallery({ items }: { items: { src: string; title: string; tall?: boolean }[] }) {
  const [idx, setIdx] = useState<number | null>(null);
  const close = useCallback(() => setIdx(null), []);
  const step = useCallback((d: number) => setIdx((i) => (i === null ? i : (i + d + items.length) % items.length)), [items.length]);

  useEffect(() => {
    if (idx === null) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") close();
      if (e.key === "ArrowRight") step(1);
      if (e.key === "ArrowLeft") step(-1);
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [idx, close, step]);

  return (
    <>
      <div className="columns-1 gap-4 sm:columns-2 lg:columns-3 [&>*]:mb-4">
        {items.map((g, i) => (
          <button key={g.src + i} onClick={() => setIdx(i)} className="reveal group relative block w-full overflow-hidden rounded-xl break-inside-avoid">
            <img src={g.src} alt={g.title} loading="lazy" className={`w-full object-cover transition duration-700 group-hover:scale-110 ${g.tall ? "h-[420px]" : "h-60"}`} />
            <span className="absolute inset-0 flex items-end bg-gradient-to-t from-jungle-950/80 to-transparent p-5 opacity-0 transition group-hover:opacity-100">
              <span className="font-display text-lg text-white">{g.title}</span>
              <span className="ml-auto grid h-9 w-9 place-items-center rounded-full bg-gold text-jungle-950">⤢</span>
            </span>
          </button>
        ))}
      </div>
      {idx !== null && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center bg-jungle-950/95 p-4 backdrop-blur-sm" onClick={close} role="dialog" aria-modal>
          <button className="absolute right-5 top-5 grid h-11 w-11 place-items-center rounded-full border border-white/20 text-xl text-white hover:bg-white/10" aria-label="Close">✕</button>
          <button onClick={(e) => { e.stopPropagation(); step(-1); }} className="absolute left-3 grid h-12 w-12 place-items-center rounded-full bg-white/10 text-2xl text-white hover:bg-gold hover:text-jungle-950 md:left-8" aria-label="Previous">‹</button>
          <figure className="max-h-full max-w-5xl animate-fade-up" onClick={(e) => e.stopPropagation()}>
            <img src={items[idx].src.replace(/w=\d+/, "w=1800")} alt={items[idx].title} className="max-h-[80vh] w-auto rounded-lg object-contain" />
            <figcaption className="mt-4 flex justify-between text-sm text-white/70">
              <span className="font-display text-lg text-white">{items[idx].title}</span>
              <span>{idx + 1} / {items.length}</span>
            </figcaption>
          </figure>
          <button onClick={(e) => { e.stopPropagation(); step(1); }} className="absolute right-3 grid h-12 w-12 place-items-center rounded-full bg-white/10 text-2xl text-white hover:bg-gold hover:text-jungle-950 md:right-8" aria-label="Next">›</button>
        </div>
      )}
    </>
  );
}

// ===================== Review carousel =====================
export type ReviewItem = Omit<import("@/types/domain").ReviewRow, "reviewType">;

export function ReviewCarousel({ reviews }: { reviews: ReviewItem[] }) {
  const [i, setI] = useState(0);
  const [paused, setPaused] = useState(false);
  const n = reviews.length;
  const current = n ? i % n : 0;
  useEffect(() => {
    if (paused || n < 2) return;
    const t = setInterval(() => setI((x) => (x + 1) % n), 6000);
    return () => clearInterval(t);
  }, [paused, n]);
  if (!n) return <p className="text-center text-white/60">No reviews yet.</p>;

  return (
    <div className="relative mx-auto max-w-4xl" onMouseEnter={() => setPaused(true)} onMouseLeave={() => setPaused(false)}>
      <div className="overflow-hidden">
        <div className="flex transition-transform duration-700 ease-out" style={{ transform: `translateX(-${current * 100}%)` }}>
          {reviews.map((r) => (
            <figure key={r.id} className="w-full shrink-0 px-2 text-center">
              <div className="font-display text-7xl leading-none text-gold/40">&ldquo;</div>
              <blockquote className="mx-auto -mt-4 max-w-3xl font-display text-xl italic leading-relaxed text-white md:text-2xl">{r.comment}</blockquote>
              <figcaption className="mt-8 flex flex-col items-center gap-3">
                <span className="grid h-14 w-14 place-items-center rounded-full border-2 border-gold bg-jungle-800 font-display text-lg text-gold">
                  {r.authorName.split(" ").map((s) => s[0]).join("").slice(0, 2)}
                </span>
                <span className="text-sm font-semibold text-white">{r.authorName}</span>
                {r.authorRole === "ROLE_ADMIN" && <span className="text-xs text-gold">Administrator</span>}
                {r.sample && <span className="rounded-full border border-gold/40 px-3 py-1 text-xs text-gold">Fictional sample — campus demonstration</span>}
                <span className="text-xs text-white/50">{r.country ?? "Traveller"} · {fmtDate(r.createdAt)}</span>
                <span className="text-gold">{"★".repeat(r.rating)}<span className="text-white/20">{"★".repeat(5 - r.rating)}</span></span>
              </figcaption>
            </figure>
          ))}
        </div>
      </div>
      <div className="mt-8 flex items-center justify-center gap-4">
        <button onClick={() => setI((i - 1 + n) % n)} className="grid h-10 w-10 place-items-center rounded-full border border-white/20 text-white hover:border-gold hover:text-gold" aria-label="Previous review">‹</button>
        {reviews.map((r, k) => (
          <button key={r.id} onClick={() => setI(k)} className={`h-1.5 rounded-full transition-all ${k === current ? "w-8 bg-gold" : "w-1.5 bg-white/30"}`} aria-label={`Review ${k + 1}`} />
        ))}
        <button onClick={() => setI((i + 1) % n)} className="grid h-10 w-10 place-items-center rounded-full border border-white/20 text-white hover:border-gold hover:text-gold" aria-label="Next review">›</button>
      </div>
    </div>
  );
}

// ===================== FAQ accordion =====================
export function Faq({ items }: { items: { q: string; a: string }[] }) {
  const [open, setOpen] = useState<number | null>(0);
  return (
    <div className="divide-y divide-jungle-900/10 rounded-2xl border border-jungle-900/10 bg-white">
      {items.map((f, i) => (
        <div key={f.q}>
          <button onClick={() => setOpen(open === i ? null : i)} className="flex w-full items-center justify-between gap-4 px-6 py-5 text-left" aria-expanded={open === i}>
            <span className="font-display text-lg font-semibold text-jungle-950">{f.q}</span>
            <span className={`grid h-8 w-8 shrink-0 place-items-center rounded-full border border-gold/50 text-gold transition-transform duration-300 ${open === i ? "rotate-45 bg-gold text-jungle-950" : ""}`}>+</span>
          </button>
          <div className={`grid transition-all duration-500 ${open === i ? "grid-rows-[1fr]" : "grid-rows-[0fr]"}`}>
            <div className="overflow-hidden"><p className="px-6 pb-6 text-sm leading-relaxed text-charcoal/70">{f.a}</p></div>
          </div>
        </div>
      ))}
    </div>
  );
}

