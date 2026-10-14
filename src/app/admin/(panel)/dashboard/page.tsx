import Link from "next/link";
import { Badge, PageHeader } from "@/components/admin/managers";
import { backendApi } from "@/lib/backend-api";
import type { DashboardStats } from "@/types/domain";
import { fmtDate, fmtTime, money } from "@/lib/content";

export default async function DashboardPage() {
  // Fetch one dashboard snapshot so the counts and recent rows match.
  const s = await backendApi<DashboardStats>("/api/admin/dashboard");
  const maxRev = Math.max(1, ...s.revenueByPackage.map((r) => r.total));
  // A minimum of one avoids division by zero when no records exist yet.
  const totalStatus = Math.max(1, Object.values(s.statusBreakdown).reduce((a, b) => a + b, 0));
  const statusColor: Record<string, string> = { PENDING: "bg-amber-400", CONFIRMED: "bg-emerald-500", COMPLETED: "bg-sky-500", CANCELLED: "bg-red-400" };

  const kpis = [
    { label: "Total bookings", value: String(s.totalBookings), note: `${s.pendingBookings} pending`, icon: "📅", href: "/admin/bookings" },
    { label: "Revenue collected", value: money(s.revenuePaid), note: `${money(s.revenuePending)} awaiting`, icon: "💰", href: "/admin/payments" },
    { label: "Customers", value: String(s.totalCustomers), note: "registered + guests", icon: "👥", href: "/admin/customers" },
    { label: "Active packages", value: String(s.activePackages), note: `${s.availableVehicles} vehicles available`, icon: "🦁", href: "/admin/packages" },
  ];

  return (
    <>
      <PageHeader title="Dashboard" sub={`Overview · ${new Date().toLocaleDateString("en-GB", { weekday: "long", day: "numeric", month: "long", year: "numeric" })}`} />
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {kpis.map((k) => (
          <Link key={k.label} href={k.href} className="group rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-jungle-700 hover:shadow-md">
            <div className="flex items-center justify-between"><p className="text-sm text-slate-500">{k.label}</p><span className="text-2xl">{k.icon}</span></div>
            <p className="mt-2 text-3xl font-semibold text-slate-900">{k.value}</p>
            <p className="mt-1 text-xs text-slate-500">{k.note}</p>
          </Link>
        ))}
      </div>

      <div className="mt-6 grid gap-6 xl:grid-cols-3">
        <section className="min-w-0 rounded-xl border border-slate-200 bg-white p-5 shadow-sm xl:col-span-2">
          <h2 className="font-semibold text-slate-900">Revenue by package</h2>
          <div className="mt-5 space-y-3">
            {s.revenueByPackage.map((r) => (
              <div key={r.name}>
                <div className="mb-1 flex justify-between text-sm"><span className="text-slate-600">{r.name}</span><span className="font-medium">{money(r.total)}</span></div>
                <div className="h-2.5 rounded-full bg-slate-100"><div className="h-full rounded-full bg-gradient-to-r from-jungle-800 to-gold" style={{ width: `${(r.total / maxRev) * 100}%` }} /></div>
              </div>
            ))}
            {!s.revenueByPackage.length && <p className="text-sm text-slate-400">No revenue yet.</p>}
          </div>
        </section>
        <section className="min-w-0 rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
          <h2 className="font-semibold text-slate-900">Booking status</h2>
          <div className="mt-5 flex h-3 overflow-hidden rounded-full bg-slate-100">
            {Object.entries(s.statusBreakdown).map(([k, v]) => <div key={k} className={statusColor[k]} style={{ width: `${(v / totalStatus) * 100}%` }} />)}
          </div>
          <ul className="mt-5 space-y-2.5 text-sm">
            {Object.entries(s.statusBreakdown).map(([k, v]) => (
              <li key={k} className="flex items-center justify-between"><span className="flex items-center gap-2"><span className={`h-2.5 w-2.5 rounded-full ${statusColor[k]}`} />{k}</span><b>{v}</b></li>
            ))}
          </ul>
          <Link href="/admin/reviews" className="mt-5 block rounded-lg bg-cream p-3 text-sm text-jungle-900 ring-1 ring-gold/30">⭐ {s.publishedReviews} published review(s) · View and reply →</Link>
        </section>
      </div>

      <div className="mt-6 grid gap-6 xl:grid-cols-2">
        <section className="min-w-0 rounded-xl border border-slate-200 bg-white shadow-sm">
          <h2 className="border-b border-slate-100 px-5 py-4 font-semibold text-slate-900">Upcoming safaris</h2>
          <ul className="divide-y divide-slate-100">
            {s.upcoming.map((b) => (
              <li key={b.id} className="flex items-center gap-4 px-5 py-3 text-sm">
                <div className="w-14 shrink-0 rounded-lg bg-jungle-950 py-1.5 text-center text-white"><p className="text-lg font-semibold leading-none text-gold">{new Date(`${b.safariDate}T00:00:00`).getDate()}</p><p className="text-[10px] uppercase">{new Date(`${b.safariDate}T00:00:00`).toLocaleString("en", { month: "short" })}</p></div>
                <div className="min-w-0 flex-1"><p className="truncate font-medium">{b.packageName}</p><p className="truncate text-xs text-slate-500">{b.customerName} · {b.guests} guests · {fmtTime(b.timeSlot)} · {b.vehicleName}</p></div>
                <Badge s={b.status} />
              </li>
            ))}
            {!s.upcoming.length && <li className="p-5 text-sm text-slate-400">No upcoming safaris.</li>}
          </ul>
        </section>
        <section className="min-w-0 rounded-xl border border-slate-200 bg-white shadow-sm">
          <h2 className="flex items-center justify-between border-b border-slate-100 px-5 py-4 font-semibold text-slate-900">Recent bookings <Link href="/admin/bookings" className="text-xs font-normal text-jungle-700 hover:underline">View all →</Link></h2>
          <ul className="divide-y divide-slate-100">
            {s.recent.map((b) => (
              <li key={b.id} className="flex items-center justify-between gap-3 px-5 py-3 text-sm">
                <div className="min-w-0"><p className="break-all font-mono text-xs text-slate-500">{b.reference} · {fmtDate(b.createdAt)}</p><p className="truncate font-medium">{b.customerName} — {b.packageName}</p></div>
                <div className="text-right"><p className="font-medium">{money(b.totalAmount)}</p><Badge s={b.paymentStatus} /></div>
              </li>
            ))}
          </ul>
        </section>
      </div>
    </>
  );
}

