"use client";

/* eslint-disable @next/next/no-img-element */
import Link from "next/link";
import PaymentSummary from "./PaymentSummary";
import { useEffect, useMemo, useState } from "react";
import type { PackageDTO } from "@/types/domain";
import type { VehicleDTO } from "@/types/domain";
import type { BookingDetail } from "@/types/domain";
import type { DestinationDTO } from "@/types/domain";
import { api } from "@/lib/api";
import { fmtDate, fmtTime, money } from "@/lib/content";

const STEPS = ["Destination", "Package", "Date & Time", "Vehicle", "Your Details", "Payment", "Confirmation"];
const MORNING_SLOTS = ["05:30", "06:00"];
const EVENING_SLOTS = ["14:00", "14:30"];

function slotsFor(p?: PackageDTO) {
  if (!p) return [];
  // The package start time is always offered; extra departures depend on its type.
  let base = MORNING_SLOTS;
  if (p.type === "EVENING" || p.type === "FAMILY") {
    base = EVENING_SLOTS;
  } else if (p.type === "PRIVATE") {
    base = [...MORNING_SLOTS, ...EVENING_SLOTS];
  }
  return Array.from(new Set([p.startTime, ...base])).sort();
}

interface Props {
  destinations: DestinationDTO[];
  packages: PackageDTO[];
  vehicles: VehicleDTO[];
  user: { name: string; email: string } | null;
  initial: { destination: number; package: number; date: string; type: string; guests: number };
}

export default function BookingWizard({ destinations, packages, vehicles, user, initial }: Props) {
  // Search links can preselect a package, destination, date and guest count.
  const [tomorrow] = useState(() => new Intl.DateTimeFormat("en-CA", { timeZone: "Asia/Colombo", year: "numeric", month: "2-digit", day: "2-digit" }).format(new Date(Date.now() + 864e5)));
  const initPkg = packages.find((p) => p.id === initial.package);
  const initDest = initPkg?.destinationId ?? (destinations.some((d) => d.id === initial.destination) ? initial.destination : 0);

  const [step, setStep] = useState(initPkg ? 3 : initDest ? 2 : 1);
  const [destId, setDestId] = useState(initDest);
  const [pkgId, setPkgId] = useState(initPkg?.id ?? 0);
  const [date, setDate] = useState(initial.date && initial.date >= tomorrow ? initial.date : tomorrow);
  const [slot, setSlot] = useState(initPkg?.startTime ?? "");
  const [guests, setGuests] = useState(Math.max(1, Math.min(initial.guests, initPkg?.maxGuests ?? 8)));
  const [vehicleId, setVehicleId] = useState(0);
  // Keep results tied to the exact choices that produced them.
  const availabilityKey = `${date}:${slot}:${pkgId}`;
  const [availability, setAvailability] = useState<{ key: string; ids: number[] } | null>(null);
  const freeIds = availability?.key === availabilityKey ? availability.ids : null;
  const quoteKey = `${pkgId}:${vehicleId}:${guests}`;
  const [quoted, setQuoted] = useState<{ key: string; data: { packageCost: number; vehicleCost: number; total: number } } | null>(null);
  const quote = quoted?.key === quoteKey ? quoted.data : null;
  const [cust, setCust] = useState({ fullName: user?.name ?? "", email: user?.email ?? "", phone: "", country: "", pickupPlace: "", specialRequests: "" });
  const [pay, setPay] = useState({ paymentMethod: "CASH" });
  const [err, setErr] = useState("");
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState<BookingDetail | null>(null);

  const dest = destinations.find((d) => d.id === destId);
  const pkg = packages.find((p) => p.id === pkgId);
  const vehicle = vehicles.find((v) => v.id === vehicleId);
  const destPackages = useMemo(
    () => packages.filter((p) => p.destinationId === destId).sort((a, b) => Number(b.type === initial.type) - Number(a.type === initial.type)),
    [packages, destId, initial.type],
  );

  // Ask the server which vehicles remain free for this departure.
  useEffect(() => {
    if (step !== 4 || !slot) return;
    let current = true;
    api<{ availableVehicleIds: number[] }>(`/api/bookings/quote?availability=1&date=${date}&slot=${slot}&packageId=${pkgId}`)
      .then((r) => { if (current) setAvailability({ key: availabilityKey, ids: r.availableVehicleIds }); })
      .catch(() => { if (!current) return; setAvailability({ key: availabilityKey, ids: [] }); setErr("Unable to check availability. Please return to the previous step and retry."); });
    return () => { current = false; };
  }, [step, date, slot, pkgId, availabilityKey]);

  // The backend calculates the price; the browser only displays the returned quote.
  useEffect(() => {
    if (!pkgId || !vehicleId) return;
    let current = true;
    api<{ packageCost: number; vehicleCost: number; total: number }>(`/api/bookings/quote?packageId=${pkgId}&vehicleId=${vehicleId}&guests=${guests}`)
      .then((q) => { if (current) setQuoted({ key: quoteKey, data: q }); })
      .catch(() => { if (current) setErr("Unable to calculate your price. Please choose the vehicle again."); });
    return () => { current = false; };
  }, [pkgId, vehicleId, guests, quoteKey]);

  const go = (n: number) => {
    // Clear the previous step's error before moving to another part of the form.
    setErr("");
    setStep(n);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const next = () => {
    // Validate the current step before allowing the customer to continue.
    if (step === 1 && !destId) return setErr("Please choose a destination");
    if (step === 2 && !pkgId) return setErr("Please choose a safari package");
    if (step === 3) {
      if (!date || date < tomorrow) return setErr("Please choose a future date");
      if (!slot) return setErr("Please choose a time slot");
    }
    if (step === 4 && !vehicleId) return setErr("Please choose a vehicle");
    if (step === 5) {
      if (!cust.fullName.trim() || !cust.phone.trim() || !cust.pickupPlace.trim()) return setErr("Name, phone and pickup place are required");
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cust.email)) return setErr("Please enter a valid email");
    }
    go(step + 1);
  };

  const confirm = async () => {
    // The API checks all rules again before it stores booking and payment records.
    if (busy || !quote) return setErr("Please wait for a valid price quote before booking.");
    setErr("");
    setBusy(true);
    try {
      const r = await api<BookingDetail>("/api/bookings", "POST", { packageId: pkgId, vehicleId, safariDate: date, timeSlot: slot, guests, ...cust, ...pay, cardDemo: pay.paymentMethod === "CARD" });
      setResult(r);
      go(7);
    } catch (e) {
      setErr((e as Error).message);
    } finally {
      setBusy(false);
    }
  };


  return (
    <div>
      {/* Completed steps can be revisited; confirmation is the final step. */}
      <ol className="mb-10 flex items-center justify-between gap-1 overflow-x-auto pb-2 print:hidden">
        {STEPS.map((s, i) => {
          const n = i + 1;
          const done = n < step;
          const cur = n === step;
          return (
            <li key={s} className="flex flex-1 items-center gap-1 last:flex-none">
              <button
                disabled={!done || step === 7}
                onClick={() => go(n)}
                className="flex shrink-0 flex-col items-center gap-1.5"
              >
                <span className={`grid h-11 w-11 place-items-center rounded-full font-display text-sm font-bold transition-all ${cur ? "scale-110 bg-gold text-jungle-950 shadow-lg shadow-gold/40" : done ? "bg-jungle-900 text-gold" : "border border-jungle-900/20 bg-white text-charcoal/40"}`}>
                  {done ? "✓" : String(n).padStart(2, "0")}
                </span>
                <span className={`hidden text-[10px] font-semibold uppercase tracking-wider md:block ${cur ? "text-jungle-950" : "text-charcoal/45"}`}>{s}</span>
              </button>
              {n < STEPS.length && <span className={`mx-1 h-px flex-1 transition-colors ${done ? "bg-gold" : "bg-jungle-900/15"} md:mb-5`} />}
            </li>
          );
        })}
      </ol>

      <div className={`grid gap-8 ${step < 7 ? "lg:grid-cols-[1fr_340px]" : ""}`}>
        <div key={step} className="animate-fade-up">
          {err && <div className="mb-5 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700 ring-1 ring-red-200">{err}</div>}

          {step === 1 && (
            // Step 1: choose a park; changing it clears dependent choices.
            <Panel title="Choose your destination" sub="Where would you like to go on safari?">
              <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
                {destinations.map((d) => (
                  <button key={d.id} onClick={() => { setDestId(d.id); setPkgId(0); setVehicleId(0); }} className={`group relative h-48 overflow-hidden rounded-2xl text-left ring-2 transition ${destId === d.id ? "ring-gold" : "ring-transparent hover:ring-gold/40"}`}>
                    <img src={d.imageUrl} alt="" className="absolute inset-0 h-full w-full object-cover transition duration-700 group-hover:scale-110" />
                    <span className="absolute inset-0 bg-gradient-to-t from-jungle-950/90 to-transparent" />
                    {destId === d.id && <span className="absolute right-3 top-3 grid h-7 w-7 place-items-center rounded-full bg-gold text-sm text-jungle-950">✓</span>}
                    <span className="absolute bottom-4 left-4 right-4 text-white">
                      <span className="block font-display text-lg font-semibold">{d.name}</span>
                      <span className="text-xs text-white/70">{d.bestWildlife}</span>
                      <span className="mt-1 block text-xs font-semibold text-gold">from {money(d.startingPrice)}</span>
                    </span>
                  </button>
                ))}
              </div>
            </Panel>
          )}

          {step === 2 && (
            // Step 2: choose a package offered at the selected park.
            <Panel title="Choose a safari package" sub={`Experiences available at ${dest?.name ?? "this park"}`}>
              {destPackages.length === 0 && <p className="text-charcoal/60">No packages are currently offered here. Please choose another destination.</p>}
              <div className="space-y-4">
                {destPackages.map((p) => (
                  <button key={p.id} onClick={() => { setPkgId(p.id); setSlot(p.startTime); setGuests((g) => Math.min(g, p.maxGuests)); setVehicleId(0); }} className={`flex w-full flex-col overflow-hidden rounded-2xl bg-white text-left ring-2 transition sm:flex-row ${pkgId === p.id ? "ring-gold" : "ring-jungle-900/5 hover:ring-gold/40"}`}>
                    <img src={p.imageUrl} alt="" className="h-40 w-full object-cover sm:h-auto sm:w-48" />
                    <span className="flex flex-1 flex-col p-5">
                      <span className="text-[10px] font-bold uppercase tracking-widest text-gold-dark">{p.typeLabel}</span>
                      <span className="font-display text-xl font-semibold text-jungle-950">{p.name}</span>
                      <span className="mt-1 text-sm text-charcoal/65">{p.description}</span>
                      <span className="mt-3 text-xs text-charcoal/60">⏱ {p.durationHours} hrs · 🌅 {fmtTime(p.startTime)} · 👥 max {p.maxGuests}</span>
                    </span>
                    <span className="flex flex-col items-end justify-center border-t border-jungle-900/5 p-5 sm:border-l sm:border-t-0">
                      <span className="font-display text-2xl font-bold text-jungle-900">{money(p.basePrice)}</span>
                      <span className="text-[11px] text-charcoal/50">{p.pricingNote}</span>
                    </span>
                  </button>
                ))}
              </div>
            </Panel>
          )}

          {step === 3 && pkg && (
            // Step 3: choose the future departure and number of guests.
            <Panel title="Choose date & time" sub={`${pkg.name} · ${pkg.durationHours} hours`}>
              <div className="grid gap-6 md:grid-cols-2">
                <div>
                  <label htmlFor="safari-date" className="label">Safari date</label>
                  <input id="safari-date" type="date" min={tomorrow} className="input" value={date} onChange={(e) => { setDate(e.target.value); setVehicleId(0); }} />
                </div>
                <div>
                  <label className="label">Number of guests (max {pkg.maxGuests})</label>
                  <div className="flex items-center gap-3">
                    <button type="button" className="grid h-12 w-12 place-items-center rounded-xl bg-white text-xl ring-1 ring-jungle-900/15" onClick={() => setGuests(Math.max(1, guests - 1))}>−</button>
                    <span className="w-12 text-center font-display text-2xl">{guests}</span>
                    <button type="button" className="grid h-12 w-12 place-items-center rounded-xl bg-white text-xl ring-1 ring-jungle-900/15" onClick={() => setGuests(Math.min(pkg.maxGuests, guests + 1))}>+</button>
                  </div>
                </div>
              </div>
              <label className="label mt-6">Departure time</label>
              <div className="flex flex-wrap gap-3">
                {slotsFor(pkg).map((s) => (
                  <button key={s} onClick={() => { setSlot(s); setVehicleId(0); }} className={`rounded-xl px-5 py-3 text-sm font-semibold transition ${slot === s ? "bg-jungle-900 text-gold" : "bg-white ring-1 ring-jungle-900/15 hover:ring-gold"}`}>
                    {Number(s.slice(0, 2)) < 12 ? "🌅" : "🌇"} {fmtTime(s)}
                  </button>
                ))}
              </div>
              <p className="mt-6 rounded-xl bg-cream/60 p-4 text-sm text-charcoal/70">ℹ️ Pickup is about 30 minutes before departure from hotels within 15 km of the park gate.</p>
            </Panel>
          )}

          {step === 4 && pkg && (
            // Step 4: exclude unavailable or undersized vehicles.
            <Panel title="Choose your vehicle" sub={`Availability for ${fmtDate(date)} at ${fmtTime(slot)} · ${guests} guest(s)`}>
              {freeIds === null ? (
                <p className="py-10 text-center text-charcoal/50">Checking availability…</p>
              ) : (
                <div className="grid gap-4 sm:grid-cols-2">
                  {vehicles.map((v) => {
                    const booked = !freeIds.includes(v.id);
                    const tooSmall = v.capacity < guests;
                    const disabled = !v.available || booked || tooSmall;
                    return (
                      <button key={v.id} disabled={disabled} onClick={() => setVehicleId(v.id)} className={`overflow-hidden rounded-2xl bg-white text-left ring-2 transition disabled:cursor-not-allowed disabled:opacity-45 ${vehicleId === v.id ? "ring-gold" : "ring-jungle-900/5 hover:ring-gold/40"}`}>
                        <img src={v.imageUrl} alt="" className="h-36 w-full object-cover" />
                        <span className="block p-5">
                          <span className="flex items-start justify-between gap-2">
                            <span>
                              <span className="block text-[10px] font-bold uppercase tracking-widest text-gold-dark">{v.categoryLabel}</span>
                              <span className="font-display text-lg font-semibold text-jungle-950">{v.name}</span>
                            </span>
                            <span className="font-display text-xl font-bold text-jungle-900">{money(v.price)}</span>
                          </span>
                          <span className="mt-1 block text-xs text-charcoal/60">👥 {v.capacity} seats {v.fourByFour && "· 4x4"} · {v.allFeatures.slice(1, 4).join(" · ")}</span>
                          {disabled && <span className="mt-2 block text-xs font-semibold text-red-600">{!v.available ? "Out of service" : booked ? "Already booked for this slot" : `Seats only ${v.capacity}`}</span>}
                        </span>
                      </button>
                    );
                  })}
                </div>
              )}
            </Panel>
          )}

          {step === 5 && (
            // Step 5: collect contact details and optional requests.
            <Panel title="Your details" sub={user ? "Signed in — details pre-filled from your account." : "We'll send your confirmation here."}>
              {!user && <p className="mb-5 text-sm text-charcoal/60">Have an account? <Link className="font-semibold text-jungle-800 underline" href="/client/login?next=/client/booking">Sign in</Link> to see this booking under My Bookings.</p>}
              <div className="grid gap-4 sm:grid-cols-2">
                <div><label htmlFor="full-name" className="label">Full name *</label><input id="full-name" className="input" value={cust.fullName} onChange={(e) => setCust({ ...cust, fullName: e.target.value })} /></div>
                <div><label htmlFor="email" className="label">Email *</label><input id="email" type="email" className="input" value={cust.email} readOnly={!!user} onChange={(e) => setCust({ ...cust, email: e.target.value })} /></div>
                <div><label htmlFor="phone-whatsapp" className="label">Phone / WhatsApp *</label><input id="phone-whatsapp" className="input" value={cust.phone} onChange={(e) => setCust({ ...cust, phone: e.target.value })} placeholder="+94 77 123 4567" /></div>
                <div><label htmlFor="country" className="label">Country</label><input id="country" className="input" value={cust.country} onChange={(e) => setCust({ ...cust, country: e.target.value })} /></div>
              </div>
              <div className="mt-4"><label htmlFor="pickup-place" className="label">Pickup place *</label><input id="pickup-place" className="input" required maxLength={240} value={cust.pickupPlace} onChange={(e) => setCust({ ...cust, pickupPlace: e.target.value })} placeholder="Hotel name, park gate or meeting point" /></div>
              <div className="mt-4"><label htmlFor="hotel-special-requests" className="label">Special requests</label><textarea id="hotel-special-requests" className="input min-h-28" value={cust.specialRequests} onChange={(e) => setCust({ ...cust, specialRequests: e.target.value })} placeholder="Dietary needs, child seats…" /></div>
            </Panel>
          )}

          {step === 6 && (
            // Step 6: cash or a card-flow demonstration with no real charge.
            <Panel title="Payment method" sub="Choose how you'd like to pay">
              <div className="grid gap-4 sm:grid-cols-2">
                {[
                  
                  ["CASH", "💵", "Pay in cash", "Pay your driver on the day · booking pending"],
                  ["CARD", "💳", "Card payment · demo", "Campus demonstration · no real money is charged"],
                ].map(([m, i, t, d]) => (
                  <button key={m} onClick={() => setPay({ ...pay, paymentMethod: m })} className={`rounded-2xl bg-white p-5 text-left ring-2 transition ${pay.paymentMethod === m ? "ring-gold" : "ring-jungle-900/5 hover:ring-gold/40"}`}>
                    <span className="text-3xl">{i}</span>
                    <span className="mt-2 block font-display text-lg font-semibold text-jungle-950">{t}</span>
                    <span className="text-xs text-charcoal/60">{d}</span>
                  </button>
                ))}
              </div>
              <p className="mt-5 text-sm text-charcoal/60">{pay.paymentMethod === "CARD" ? "This demonstrates the card booking flow. Do not enter real card details. No charge is made; payment remains pending until staff collect payment." : "Payment is collected on arrival and recorded by staff."}</p>
            </Panel>
          )}

          {step === 7 && result && <Confirmation b={result} />}

          {step < 7 && (
            <div className="mt-8 flex justify-between">
              <button onClick={() => go(step - 1)} disabled={step === 1} className="rounded-full px-6 py-3 text-xs font-bold uppercase tracking-widest text-jungle-900 ring-1 ring-jungle-900/20 transition hover:bg-white disabled:opacity-30">← Back</button>
              {step < 6 ? (
                <button onClick={next} className="btn-gold">Continue →</button>
              ) : (
                <button onClick={confirm} disabled={busy || !quote} className="btn-gold">{busy ? "Processing…" : `Confirm & ${pay.paymentMethod === "CARD" ? "book demo" : "book"} ${quote ? money(quote.total) : ""}`}</button>
              )}
            </div>
          )}
        </div>

        {step < 7 && (
          <aside className="lg:sticky lg:top-28 lg:self-start">
            <div className="overflow-hidden rounded-2xl bg-jungle-950 text-white shadow-xl">
              {pkg ? <img src={pkg.imageUrl} alt="" className="h-36 w-full object-cover opacity-80" /> : dest && <img src={dest.imageUrl} alt="" className="h-36 w-full object-cover opacity-80" />}
              <div className="p-6">
                <p className="eyebrow">Booking summary</p>
                <dl className="mt-4 space-y-3 text-sm">
                  <Row k="Destination" v={dest?.name} />
                  <Row k="Package" v={pkg?.name} />
                  <Row k="Date" v={pkg ? `${fmtDate(date)}${slot ? " · " + fmtTime(slot) : ""}` : undefined} />
                  <Row k="Guests" v={pkg ? String(guests) : undefined} />
                  <Row k="Vehicle" v={vehicle?.name} />
                  <Row k="Vehicle number" v={vehicle ? vehicle.registrationNumber ?? "Pending assignment" : undefined} />
                  <Row k="Pickup place" v={cust.pickupPlace || undefined} />
                </dl>
                {quote && (
                  <div className="mt-5 space-y-1.5 border-t border-white/10 pt-4 text-sm">
                    <div className="flex justify-between text-white/70"><span>Safari ({pkg?.pricingNote})</span><span>{money(quote.packageCost)}</span></div>
                    <div className="flex justify-between text-white/70"><span>Vehicle hire</span><span>{money(quote.vehicleCost)}</span></div>
                    <div className="flex justify-between pt-2 font-display text-2xl text-gold"><span>Total</span><span>{money(quote.total)}</span></div>
                  </div>
                )}
              </div>
            </div>
          </aside>
        )}
      </div>
    </div>
  );
}

function Panel({ title, sub, children }: { title: string; sub: string; children: React.ReactNode }) {
  return (
    <section>
      <h2 className="font-display text-3xl font-semibold text-jungle-950">{title}</h2>
      <p className="mt-1 mb-6 text-sm text-charcoal/60">{sub}</p>
      {children}
    </section>
  );
}

function Row({ k, v }: { k: string; v?: string }) {
  return (
    <div className="flex justify-between gap-4">
      <dt className="text-white/50">{k}</dt>
      <dd className={`text-right ${v ? "text-white" : "text-white/25"}`}>{v ?? "—"}</dd>
    </div>
  );
}

function Confirmation({ b }: { b: BookingDetail }) {
  // The final screen displays the booking and payment state returned by the API.
  const paid = b.paymentStatus === "PAID";
  return (
    <div className="mx-auto max-w-3xl">
      <div className="text-center print:hidden">
        <div className="mx-auto grid h-20 w-20 animate-bob place-items-center rounded-full bg-gold text-4xl text-jungle-950 shadow-xl shadow-gold/30">✓</div>
        <h2 className="mt-6 font-display text-4xl font-semibold text-jungle-950">Your safari is booked!</h2>
        <p className="mt-2 text-charcoal/60">A confirmation has been recorded for {b.customerEmail}.</p>
      </div>
      <div className="mt-10 overflow-hidden rounded-3xl bg-white shadow-2xl ring-1 ring-jungle-900/5">
        <div className="flex flex-wrap items-center justify-between gap-4 bg-jungle-950 px-8 py-6 text-white">
          <div>
            <p className="eyebrow">Booking confirmation</p>
            <p className="mt-1 font-display text-3xl font-bold tracking-wider text-gold">{b.reference}</p>
          </div>
          <span className={`rounded-full px-4 py-1.5 text-xs font-bold uppercase tracking-widest ${b.status === "CONFIRMED" ? "bg-emerald-500 text-white" : "bg-amber-400 text-jungle-950"}`}>{b.status}</span>
        </div>
        <dl className="grid gap-x-8 gap-y-5 p-8 sm:grid-cols-2">
          {[
            ["Customer name", b.customerName],
            ["Destination", b.destinationName],
            ["Package", b.packageName],
            ["Vehicle", b.vehicleName],
            ["Vehicle number", b.vehicleNumber ?? "Pending assignment"],
            ["Pickup place", b.pickupPlace ?? "Contact staff to arrange pickup"],
            ["Date", `${fmtDate(b.safariDate)} · ${fmtTime(b.timeSlot)}`],
            ["Guests", String(b.guests)],
            ["Payment method", b.paymentMethod === "CARD" ? (b.transactionRef?.startsWith("DEMO-") ? "Card demo — no charge" : "Card") : "Cash on safari day"],
            ["Payment status", b.paymentStatus ?? "—"],
          ].map(([k, v]) => (
            <div key={k} className="border-b border-dashed border-jungle-900/10 pb-3">
              <dt className="label !mb-0.5">{k}</dt>
              <dd className={`font-medium ${k === "Payment status" ? (paid ? "text-emerald-700" : "text-amber-700") : "text-jungle-950"}`}>{v}</dd>
            </div>
          ))}
        </dl>
        <div className="flex items-center justify-between bg-cream/60 px-8 py-6">
          <span className="text-sm text-charcoal/60">Total amount</span>
          <span className="font-display text-4xl font-bold text-jungle-900">{money(b.totalAmount)}</span>
        </div>
        <div className="px-8 pb-5"><PaymentSummary booking={b} /></div>
        <p className="px-8 py-4 text-xs text-charcoal/50">Transaction ref: {b.transactionRef} · Free cancellation up to 24 hours before departure.</p>
      </div>
      <div className="mt-8 flex flex-wrap justify-center gap-3 print:hidden">
        <button onClick={() => window.print()} className="btn-dark">Print confirmation</button>
        <Link href="/client/my-bookings" className="btn-gold">My bookings</Link>
        <Link href="/client/home" className="rounded-full px-6 py-3 text-xs font-bold uppercase tracking-widest text-jungle-900 ring-1 ring-jungle-900/20">Home</Link>
      </div>
    </div>
  );
}

