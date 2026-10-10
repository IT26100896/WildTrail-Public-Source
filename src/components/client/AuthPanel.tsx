/* eslint-disable @next/next/no-img-element */
import type { ReactNode } from "react";
import { IMG } from "@/lib/content";

export function AuthPanel({ title, subtitle, children }: { title: string; subtitle: string; children: ReactNode }) {
  return (
    <section className="relative flex min-h-screen items-center justify-center overflow-hidden bg-jungle-950 px-5 pb-16 pt-28">
      <img src={IMG.elephantsHerd} alt="" className="absolute inset-0 h-full w-full animate-kenburns object-cover opacity-40" />
      <div className="absolute inset-0 bg-gradient-to-br from-jungle-950/90 via-jungle-950/60 to-jungle-900/80" />
      <div className="relative w-full max-w-md animate-fade-up rounded-3xl bg-ivory p-8 shadow-2xl md:p-10">
        <p className="eyebrow">WildTrail account</p>
        <h1 className="mt-2 font-display text-3xl font-semibold text-jungle-950">{title}</h1>
        <p className="mt-1 mb-7 text-sm text-charcoal/60">{subtitle}</p>
        {children}
      </div>
    </section>
  );
}

