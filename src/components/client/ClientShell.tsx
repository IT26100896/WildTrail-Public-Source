import type { ReactNode } from "react";
import { getWebsiteSession } from "@/lib/session";
import Footer from "./Footer";
import Navbar from "./Navbar";
import RevealObserver from "./RevealObserver";

/** Shared website frame for visitors, customers and signed-in administrators. */
export default async function ClientShell({ children }: { children: ReactNode }) {
  const me = await getWebsiteSession();
  return (
    <>
      <Navbar userName={me?.name ?? null} userRole={me?.role} userEmail={me?.email} />
      <main className="min-h-screen">{children}</main>
      <Footer />
      <RevealObserver />
    </>
  );
}

export function PageHero({ eyebrow, title, text, image }: { eyebrow: string; title: string; text?: string; image: string }) {
  return (
    <section className="relative flex h-[52vh] min-h-[380px] items-end overflow-hidden bg-jungle-950">
      {/* eslint-disable-next-line @next/next/no-img-element */}
      <img src={image} alt="" className="absolute inset-0 h-full w-full animate-kenburns object-cover opacity-70" />
      <div className="absolute inset-0 bg-gradient-to-t from-jungle-950 via-jungle-950/40 to-jungle-950/30" />
      <div className="relative mx-auto w-full max-w-7xl px-5 pb-14 lg:px-8">
        <p className="eyebrow animate-fade-up">{eyebrow}</p>
        <h1 className="mt-3 max-w-3xl animate-fade-up font-display text-4xl font-semibold text-white [animation-delay:120ms] md:text-6xl">
          {title}
        </h1>
        {text && <p className="mt-4 max-w-2xl animate-fade-up text-white/75 [animation-delay:240ms]">{text}</p>}
      </div>
    </section>
  );
}

export function SectionHeading({ eyebrow, title, text, light = false, center = true }: { eyebrow: string; title: string; text?: string; light?: boolean; center?: boolean }) {
  return (
    <div className={`reveal mb-12 ${center ? "mx-auto max-w-2xl text-center" : "max-w-2xl"}`}>
      <p className="eyebrow">{eyebrow}</p>
      <h2 className={`mt-3 font-display text-3xl font-semibold md:text-5xl ${light ? "text-white" : "text-jungle-950"}`}>{title}</h2>
      {center && <div className="mx-auto mt-5 h-px w-16 bg-gold" />}
      {text && <p className={`mt-5 leading-relaxed ${light ? "text-white/70" : "text-charcoal/70"}`}>{text}</p>}
    </div>
  );
}

