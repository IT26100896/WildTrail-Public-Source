"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import type { DestinationDTO } from "@/types/domain";

const TYPES = [
  ["", "Any safari type"],
  ["MORNING", "Morning Safari"],
  ["EVENING", "Evening Safari"],
  ["HALF_DAY", "Half Day Safari"],
  ["FULL_DAY", "Full Day Safari"],
  ["PRIVATE", "Private Safari"],
  ["FAMILY", "Family Safari"],
  ["PHOTOGRAPHY", "Photography Safari"],
];

export default function QuickBooking({ destinations }: { destinations: DestinationDTO[] }) {
  const router = useRouter();
  const [tomorrow] = useState(() => new Intl.DateTimeFormat("en-CA", { timeZone: "Asia/Colombo", year: "numeric", month: "2-digit", day: "2-digit" }).format(new Date(Date.now() + 864e5)));
  const [f, setF] = useState({ destination: "", date: tomorrow, type: "", guests: "2" });

  const submit = (e: React.FormEvent) => {
    // Carry the selected filters to the full booking wizard.
    e.preventDefault();
    const q = new URLSearchParams(Object.entries(f).filter(([, v]) => v));
    router.push(`/client/booking?${q.toString()}`);
  };

  const field = "min-w-0 w-full bg-transparent text-sm font-medium text-jungle-950 outline-none";

  return (
    <section id="quick-booking" className="relative z-10 scroll-mt-24 mx-auto max-w-6xl px-5 py-6">
      <form onSubmit={submit} className="reveal grid gap-px overflow-hidden rounded-2xl bg-jungle-900/10 shadow-2xl shadow-jungle-950/30 sm:grid-cols-2 lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)_minmax(0,0.8fr)_auto]">
        <label className="min-w-0 bg-white px-4 py-5">
          <span className="label">📍 Destination</span>
          <select className={field} value={f.destination} onChange={(e) => setF({ ...f, destination: e.target.value })}>
            <option value="">All destinations</option>
            {destinations.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </select>
        </label>
        <label className="min-w-0 bg-white px-4 py-5">
          <span className="label">📅 Safari Date</span>
          <input type="date" min={tomorrow} className={field} value={f.date} onChange={(e) => setF({ ...f, date: e.target.value })} />
        </label>
        <label className="min-w-0 bg-white px-4 py-5">
          <span className="label">🦁 Safari Type</span>
          <select className={field} value={f.type} onChange={(e) => setF({ ...f, type: e.target.value })}>
            {TYPES.map(([v, l]) => <option key={v} value={v}>{l}</option>)}
          </select>
        </label>
        <label className="min-w-0 bg-white px-4 py-5">
          <span className="label">👥 Guests</span>
          <select className={field} value={f.guests} onChange={(e) => setF({ ...f, guests: e.target.value })}>
            {Array.from({ length: 8 }, (_, i) => <option key={i} value={i + 1}>{i + 1} {i ? "Guests" : "Guest"}</option>)}
          </select>
        </label>
        <button className="sm:col-span-2 lg:col-span-1 bg-gold px-5 py-5 text-xs font-bold uppercase tracking-[0.2em] text-jungle-950 transition hover:bg-sand">
          Check Availability
        </button>
      </form>
    </section>
  );
}

