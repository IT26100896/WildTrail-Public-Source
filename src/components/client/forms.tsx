"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import type { PackageDTO } from "@/types/domain";
import type { BookingDetail } from "@/types/domain";
import { api } from "@/lib/api";
import { fmtDate, fmtTime, money } from "@/lib/content";
import { PackageCard } from "./cards";

function Alert({ kind, children }: { kind: "error" | "success"; children: React.ReactNode }) {
  return (
    <div className={`rounded-xl px-4 py-3 text-sm ${kind === "error" ? "bg-red-50 text-red-700 ring-1 ring-red-200" : "bg-emerald-50 text-emerald-800 ring-1 ring-emerald-200"}`}>
      {children}
    </div>
  );
}

// ===================== Package explorer (filter) =====================
export function PackageExplorer({ packages, destinations }: { packages: PackageDTO[]; destinations: { id: number; name: string }[] }) {
  const [dest, setDest] = useState<number | 0>(0);
  const [type, setType] = useState("");
  const [sort, setSort] = useState("featured");
  const types = useMemo(() => Array.from(new Map(packages.map((p) => [p.type, p.typeLabel])).entries()), [packages]);
  const list = useMemo(() => {
    const l = packages.filter((p) => (!dest || p.destinationId === dest) && (!type || p.type === type));
    if (sort === "low") l.sort((a, b) => a.basePrice - b.basePrice);
    if (sort === "high") l.sort((a, b) => b.basePrice - a.basePrice);
    return l;
  }, [packages, dest, type, sort]);

  const chip = (on: boolean) => `rounded-full px-4 py-2 text-xs font-semibold tracking-wide transition ${on ? "bg-jungle-900 text-gold" : "bg-white text-jungle-900 ring-1 ring-jungle-900/15 hover:ring-gold"}`;

  return (
    <>
      <div className="mb-10 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex flex-wrap gap-2">
          <button className={chip(!type)} onClick={() => setType("")}>All types</button>
          {types.map(([t, l]) => <button key={t} className={chip(type === t)} onClick={() => setType(t)}>{l}</button>)}
        </div>
        <div className="flex gap-3">
          <select className="input !w-auto" value={dest} onChange={(e) => setDest(Number(e.target.value))}>
            <option value={0}>All destinations</option>
            {destinations.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </select>
          <select className="input !w-auto" value={sort} onChange={(e) => setSort(e.target.value)}>
            <option value="featured">Featured</option>
            <option value="low">Price: low → high</option>
            <option value="high">Price: high → low</option>
          </select>
        </div>
      </div>
      {list.length ? (
        <div className="grid gap-7 sm:grid-cols-2 lg:grid-cols-3">
          {list.map((p, i) => <PackageCard key={p.id} p={p} i={i} />)}
        </div>
      ) : (
        <p className="py-20 text-center text-charcoal/60">No packages match your filters.</p>
      )}
    </>
  );
}

// ===================== Login / Register =====================
export function AuthForm({ mode }: { mode: "login" | "register" }) {
  const [f, setF] = useState({ fullName: "", email: "", phone: "", password: "" });
  const [err, setErr] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr("");
    setBusy(true);
    try {
      const r = await api<{ redirect: string }>(`/api/auth/${mode}`, "POST", f);
      const next = new URLSearchParams(window.location.search).get("next");
      // A full navigation discards prefetched pages from the previous signed-in role.
      window.location.assign(r.redirect.startsWith("/admin") ? r.redirect : next && next.startsWith("/client/") ? next : r.redirect);
    } catch (e) {
      setErr((e as Error).message);
      setBusy(false);
    }
  };

  return (
    <form onSubmit={submit} className="space-y-4">
      {err && <Alert kind="error">{err}</Alert>}
      {mode === "register" && (
        <>
          <div><label htmlFor="full-name" className="label">Full name</label><input id="full-name" className="input" required value={f.fullName} onChange={(e) => setF({ ...f, fullName: e.target.value })} /></div>
          <div><label htmlFor="phone" className="label">Phone</label><input id="phone" className="input" value={f.phone} onChange={(e) => setF({ ...f, phone: e.target.value })} placeholder="+94 77 123 4567" /></div>
        </>
      )}
      <div><label htmlFor="email" className="label">Email</label><input id="email" type="email" className="input" required value={f.email} onChange={(e) => setF({ ...f, email: e.target.value })} /></div>
      <div><label htmlFor="password" className="label">Password</label><input id="password" type="password" className="input" required minLength={mode === "register" ? 8 : undefined} value={f.password} onChange={(e) => setF({ ...f, password: e.target.value })} /></div>
      <button disabled={busy} className="btn-gold w-full">{busy ? "Please wait…" : mode === "login" ? "Sign in" : "Create account"}</button>
      <p className="text-center text-sm text-charcoal/60">
        {mode === "login" ? (
          <>New to WildTrail? <Link href="/client/register" className="font-semibold text-jungle-800 hover:text-gold">Create an account</Link></>
        ) : (
          <>Already have an account? <Link href="/client/login" className="font-semibold text-jungle-800 hover:text-gold">Sign in</Link></>
        )}
      </p>
    </form>
  );
}

// ===================== Contact =====================
export function ContactForm() {
  const [f, setF] = useState({ name: "", email: "", phone: "", message: "" });
  const [state, setState] = useState<{ ok?: boolean; msg?: string; busy?: boolean }>({});
  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setState({ busy: true });
    try {
      await api("/api/contact", "POST", f);
      setF({ name: "", email: "", phone: "", message: "" });
      setState({ ok: true, msg: "Thank you! Your message has been recorded for our safari team." });
    } catch (e) {
      setState({ ok: false, msg: (e as Error).message });
    }
  };
  return (
    <form onSubmit={submit} className="space-y-4">
      {state.msg && <Alert kind={state.ok ? "success" : "error"}>{state.msg}</Alert>}
      <div className="grid gap-4 sm:grid-cols-2">
        <div><label htmlFor="name" className="label">Name</label><input id="name" className="input" required value={f.name} onChange={(e) => setF({ ...f, name: e.target.value })} /></div>
        <div><label htmlFor="email" className="label">Email</label><input id="email" type="email" className="input" required value={f.email} onChange={(e) => setF({ ...f, email: e.target.value })} /></div>
      </div>
      <div><label htmlFor="phone" className="label">Phone</label><input id="phone" className="input" value={f.phone} onChange={(e) => setF({ ...f, phone: e.target.value })} /></div>
      <div><label htmlFor="message" className="label">Message</label><textarea id="message" className="input min-h-36" required value={f.message} onChange={(e) => setF({ ...f, message: e.target.value })} /></div>
      <button disabled={state.busy} className="btn-gold">{state.busy ? "Sending…" : "Send message"}</button>
    </form>
  );
}

// ===================== Review submission =====================
export function ReviewForm({ defaultName }: { defaultName?: string }) {
  const [f, setF] = useState({ authorName: defaultName ?? "", country: "", rating: 5, comment: "" });
  const [state, setState] = useState<{ ok?: boolean; msg?: string; busy?: boolean }>({});
  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setState({ busy: true });
    try {
      const r = await api<{ message: string }>("/api/reviews", "POST", f);
      setF({ ...f, comment: "" });
      setState({ ok: true, msg: r.message });
    } catch (e) {
      setState({ ok: false, msg: (e as Error).message });
    }
  };
  return (
    <form onSubmit={submit} className="space-y-4">
      {state.msg && <Alert kind={state.ok ? "success" : "error"}>{state.msg}</Alert>}
      <div className="grid gap-4 sm:grid-cols-2">
        <div><label htmlFor="your-name" className="label">Your name</label><input id="your-name" className="input" required value={f.authorName} onChange={(e) => setF({ ...f, authorName: e.target.value })} /></div>
        <div><label htmlFor="country" className="label">Country</label><input id="country" className="input" value={f.country} onChange={(e) => setF({ ...f, country: e.target.value })} /></div>
      </div>
      <div>
        <label className="label">Rating</label>
        <div className="flex gap-1">
          {[1, 2, 3, 4, 5].map((n) => (
            <button type="button" key={n} onClick={() => setF({ ...f, rating: n })} className={`text-3xl transition hover:scale-110 ${n <= f.rating ? "text-gold" : "text-charcoal/20"}`} aria-label={`${n} stars`}>★</button>
          ))}
        </div>
      </div>
      <div><label htmlFor="your-experience" className="label">Your experience</label><textarea id="your-experience" className="input min-h-32" required value={f.comment} onChange={(e) => setF({ ...f, comment: e.target.value })} /></div>
      <button disabled={state.busy} className="btn-gold">{state.busy ? "Submitting…" : "Submit review"}</button>
    </form>
  );
}

// ===================== My bookings =====================
const STATUS_STYLE: Record<string, string> = {
  PENDING: "bg-amber-100 text-amber-800",
  CONFIRMED: "bg-emerald-100 text-emerald-800",
  COMPLETED: "bg-sky-100 text-sky-800",
  CANCELLED: "bg-red-100 text-red-700",
};

export function StatusBadge({ s }: { s: string | null }) {
  return <span className={`rounded-full px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider ${STATUS_STYLE[s ?? ""] ?? "bg-slate-100 text-slate-700"}`}>{s ?? "—"}</span>;
}

export function MyBookingsList({ initial }: { initial: BookingDetail[] }) {
  const [list, setList] = useState(initial);
  const [err, setErr] = useState("");
  const [edit, setEdit] = useState<BookingDetail | null>(null);
  const [busy, setBusy] = useState(false);
  const save = async (e: React.FormEvent) => {
    e.preventDefault(); if (!edit || busy) return; setBusy(true); setErr("");
    try { const updated = await api<BookingDetail>(`/api/bookings/mine/${edit.id}`, "PUT", edit); setList(l => l.map(b => b.id === updated.id ? updated : b)); setEdit(null); } catch(e) { setErr((e as Error).message); } finally { setBusy(false); }
  };
  const cancel = async (id: number) => {
    if (!confirm("Cancel this booking? Any cash refund must be handled by staff.")) return;
    setErr("");
    try {
      const updated = await api<BookingDetail>("/api/bookings/mine", "POST", { id });
      setList((l) => l.map((b) => (b.id === id ? updated : b)));
    } catch (e) {
      setErr((e as Error).message);
    }
  };
  if (!list.length) {
    return (
      <div className="card p-12 text-center">
        <p className="text-5xl">🐾</p>
        <h3 className="mt-4 font-display text-2xl text-jungle-950">No safaris booked yet</h3>
        <p className="mt-2 text-charcoal/60">Your adventures will appear here once you book.</p>
        <Link href="/client/booking" className="btn-gold mt-6">Book your first safari</Link>
      </div>
    );
  }
  return (
    <div className="space-y-4">
      {err && <Alert kind="error">{err}</Alert>}
      {edit && <form className="card grid gap-4 p-6 sm:grid-cols-2" onSubmit={save}><h2 className="font-display text-xl sm:col-span-2">Change booking {edit.reference}</h2><div><label htmlFor="edit-date" className="label">Safari date</label><input id="edit-date" type="date" className="input" required value={edit.safariDate} onChange={e => setEdit({ ...edit, safariDate: e.target.value })} /></div><div><label htmlFor="edit-slot" className="label">Departure time</label><input id="edit-slot" type="time" className="input" required value={edit.timeSlot} onChange={e => setEdit({ ...edit, timeSlot: e.target.value })} /></div><div><label htmlFor="edit-guests" className="label">Guests</label><input id="edit-guests" type="number" min={1} max={20} className="input" required value={edit.guests} onChange={e => setEdit({ ...edit, guests: Number(e.target.value) })} /></div><div><label htmlFor="edit-requests" className="label">Special requests</label><textarea id="edit-requests" className="input" maxLength={1000} value={edit.specialRequests ?? ""} onChange={e => setEdit({ ...edit, specialRequests: e.target.value })} /></div><p className="text-xs sm:col-span-2">Changes require 24 hours notice. Unpaid bookings are repriced; paid bookings must keep the same total.</p><div><button disabled={busy} className="btn-dark">Save changes</button><button type="button" className="ml-4 text-sm" onClick={() => setEdit(null)}>Close</button></div></form>}
      {list.map((b) => (
        <article key={b.id} className="card flex flex-col gap-5 p-6 md:flex-row md:items-center">
          <div className="grid h-16 w-16 shrink-0 place-items-center rounded-xl bg-jungle-900 text-center text-gold">
            <div><p className="font-display text-2xl leading-none">{new Date(`${b.safariDate}T00:00:00`).getDate()}</p><p className="text-[10px] uppercase">{new Date(`${b.safariDate}T00:00:00`).toLocaleString("en", { month: "short" })}</p></div>
          </div>
          <div className="flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <span className="font-mono text-xs text-charcoal/50">{b.reference}</span>
              <StatusBadge s={b.status} />
            </div>
            <h3 className="mt-1 font-display text-xl font-semibold text-jungle-950">{b.packageName}</h3>
            <p className="text-sm text-charcoal/60">📍 {b.destinationName} · 🚙 {b.vehicleName} · 👥 {b.guests} · 🕔 {fmtTime(b.timeSlot)} · {fmtDate(b.safariDate)}</p>
            <p className="mt-1 text-xs text-charcoal/50">Payment: {b.paymentMethod} · {b.paymentStatus}</p>
          </div>
          <div className="text-right">
            <p className="font-display text-2xl font-bold text-jungle-900">{money(b.totalAmount)}</p>
            <Link href={`/client/receipts/${b.id}`} className="mt-2 block text-xs underline">View statement / receipt</Link>
            {(b.status === "PENDING" || b.status === "CONFIRMED") && (
              <div><button onClick={() => setEdit(b)} className="mt-2 mr-4 text-xs font-semibold underline">Change booking</button><button onClick={() => cancel(b.id)} className="mt-2 text-xs font-semibold text-red-600 hover:underline">Cancel booking</button></div>
            )}
          </div>
        </article>
      ))}
    </div>
  );
}

