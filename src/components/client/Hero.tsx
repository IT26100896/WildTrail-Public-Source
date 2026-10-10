"use client";

/* eslint-disable @next/next/no-img-element */
import Link from "next/link";
import { useEffect, useMemo, useRef } from "react";
import { IMG } from "@/lib/content";

export default function Hero() {
  const bg = useRef<HTMLDivElement>(null);
  const particles = useMemo(
    () =>
      Array.from({ length: 22 }, (_, i) => ({
        left: (i * 37) % 100,
        top: 20 + ((i * 53) % 70),
        size: 2 + (i % 3),
        delay: (i * 0.7) % 9,
        dur: 7 + (i % 5) * 1.5,
      })),
    [],
  );

  useEffect(() => {
    let raf = 0;
    const onScroll = () => {
      cancelAnimationFrame(raf);
      raf = requestAnimationFrame(() => {
        if (bg.current) bg.current.style.transform = `translate3d(0, ${window.scrollY * 0.35}px, 0)`;
      });
    };
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => {
      window.removeEventListener("scroll", onScroll);
      cancelAnimationFrame(raf);
    };
  }, []);

  return (
    <section className="relative flex min-h-[100svh] items-center pb-24 pt-32 sm:pb-28 sm:pt-36 overflow-hidden bg-jungle-950">
      <div ref={bg} className="absolute inset-0 will-change-transform">
        <img src={IMG.hero} alt="Sri Lankan leopard drinking at a waterhole" className="h-full w-full animate-kenburns object-cover" />
      </div>
      <div className="absolute inset-0 bg-gradient-to-r from-jungle-950/90 via-jungle-950/50 to-jungle-950/20" />
      <div className="absolute inset-0 bg-gradient-to-t from-jungle-950 via-transparent to-jungle-950/40" />

      {/* floating fireflies */}
      <div className="pointer-events-none absolute inset-0" aria-hidden>
        {particles.map((p, i) => (
          <span
            key={i}
            className="absolute animate-float rounded-full bg-sand shadow-[0_0_10px_2px_rgba(216,192,138,0.6)]"
            style={{ left: `${p.left}%`, top: `${p.top}%`, width: p.size, height: p.size, animationDelay: `${p.delay}s`, animationDuration: `${p.dur}s` }}
          />
        ))}
      </div>

      <div className="relative mx-auto w-full max-w-7xl px-5 lg:px-8">
        <p className="eyebrow animate-fade-up">Sri Lanka · Wildlife Safaris</p>
        <h1 className="mt-5 animate-fade-up font-display text-[clamp(3.5rem,8vw,7.5rem)] font-semibold leading-[0.95] text-white [animation-delay:150ms]">
          Enter<br />the <em className="text-gold">Wild.</em>
        </h1>
        <p className="mt-7 max-w-xl animate-fade-up text-lg leading-relaxed text-white/80 [animation-delay:300ms]">
          Discover Sri Lanka&apos;s wilderness through unforgettable safari experiences — leopards at dawn, elephants at dusk.
        </p>
        <div className="mt-10 flex animate-fade-up flex-wrap gap-4 [animation-delay:450ms]">
          <Link href="/client/safaris" className="btn-ghost">Explore Safaris</Link>
          <Link href="/client/booking" className="btn-gold">Book Your Safari</Link>
        </div>
        <div className="mt-10 hidden animate-fade-up flex-wrap gap-x-10 gap-y-5 text-white [animation-delay:600ms] sm:flex">
          {[["Explore", "National Parks"], ["Cash", "Pay on arrival"], ["24h", "Cancellation notice"]].map(([n, l]) => (
            <div key={l}>
              <p className="font-display text-3xl text-gold">{n}</p>
              <p className="text-xs uppercase tracking-[0.2em] text-white/60">{l}</p>
            </div>
          ))}
        </div>
      </div>

      <a href="#quick-booking" className="absolute bottom-8 left-1/2 flex -translate-x-1/2 flex-col items-center gap-2 text-[10px] uppercase tracking-[0.3em] text-white/60" aria-label="Scroll down">
        <span className="flex h-10 w-6 justify-center rounded-full border border-white/40 pt-2">
          <span className="h-2 w-1 animate-scroll-dot rounded-full bg-gold" />
        </span>
        Scroll
      </a>
    </section>
  );
}

