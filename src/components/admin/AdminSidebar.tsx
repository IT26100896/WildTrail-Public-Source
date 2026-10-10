"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";

const NAV = [
  { href: "/admin/accounts", label: "Administrators", icon: "🔐" },
  { href: "/admin/messages", label: "Contact inbox", icon: "✉️" },
  { href: "/admin/dashboard", label: "Dashboard", icon: "📊" },
  { href: "/admin/bookings", label: "Bookings", icon: "📅" },
  { href: "/admin/payments", label: "Payments", icon: "💳" },
  { href: "/admin/customers", label: "Customers", icon: "👥" },
  { href: "/admin/packages", label: "Safari Packages", icon: "🦁" },
  { href: "/admin/vehicles", label: "Vehicles", icon: "🚙" },
  { href: "/admin/guides", label: "Guides & schedules", icon: "🧭" },
  { href: "/admin/files", label: "File reports", icon: "📁" },
  { href: "/admin/reviews", label: "Reviews", icon: "⭐" },
];

export default function AdminSidebar({ name, email }: { name: string; email: string }) {
  const pathname = usePathname();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [openPath, setOpenPath] = useState<string | null>(null);
  const open = openPath === pathname;


  const logout = async () => {
    setBusy(true); setError("");
    try {
      const response = await fetch("/api/auth/logout", { method: "POST" });
      if (!response.ok) throw new Error("Unable to sign out. Please try again.");
      window.location.replace("/client/login");
    } catch (e) { setError((e as Error).message); setBusy(false); }
  };

  return (
    <>
      <header className="fixed inset-x-0 top-0 z-30 flex h-14 items-center justify-between border-b border-slate-200 bg-white px-4 lg:hidden">
        <button onClick={() => setOpenPath(pathname)} className="text-2xl" aria-label="Open menu">☰</button>
        <span className="font-bold tracking-widest text-jungle-900">WILD<span className="text-gold">TRAIL</span> Admin</span>
        <span />
      </header>
      {open && <div className="fixed inset-0 z-40 bg-black/40 lg:hidden" onClick={() => setOpenPath(null)} />}
      <aside className={`fixed inset-y-0 left-0 z-50 flex w-64 flex-col bg-jungle-950 text-white transition-transform lg:translate-x-0 ${open ? "translate-x-0" : "-translate-x-full"}`}>
        <div className="shrink-0 border-b border-white/10 px-6 py-5">
          <p className="font-display text-xl font-bold tracking-[0.18em]">WILD<span className="text-gold">TRAIL</span></p>
          <p className="text-[10px] uppercase tracking-[0.3em] text-white/40">Admin console</p>
        </div>
        <nav className="min-h-0 flex-1 space-y-1 overflow-y-auto p-3">
          {NAV.map((n) => {
            const active = pathname.startsWith(n.href);
            return (
              <Link key={n.href} href={n.href} className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm transition ${active ? "bg-gold text-jungle-950 font-semibold" : "text-white/70 hover:bg-white/5 hover:text-white"}`}>
                <span>{n.icon}</span>{n.label}
              </Link>
            );
          })}
        </nav>
        <div className="shrink-0 border-t border-white/10 p-4">
          <div className="flex items-center gap-3">
            <span className="grid h-9 w-9 place-items-center rounded-full bg-jungle-800 text-sm font-bold text-gold">{name.split(" ").map((s) => s[0]).join("").slice(0, 2)}</span>
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium">{name}</p>
              <p className="truncate text-xs text-white/40">{email}</p>
            </div>
          </div>
          <p role="alert" className="text-xs text-red-300">{error}</p>
          <button disabled={busy} onClick={logout} className="mt-3 w-full rounded-lg border border-white/15 py-2 text-xs text-white/70 hover:bg-white/5">{busy ? "Signing out…" : "Sign out"}</button>
        </div>
      </aside>
    </>
  );
}



