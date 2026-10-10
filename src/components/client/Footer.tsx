import Link from "next/link";

import { Logo } from "./Navbar";

export default function Footer() {
  return (
    <footer className="relative overflow-hidden bg-jungle-950 text-white/70">
      <div className="pointer-events-none absolute -right-40 -top-40 h-96 w-96 rounded-full bg-jungle-800/40 blur-3xl" />
      <div className="mx-auto grid max-w-7xl gap-12 px-5 py-16 md:grid-cols-2 lg:grid-cols-4 lg:px-8">
        <div>
          <Logo />
          <p className="mt-5 text-sm leading-relaxed">
            Handcrafted safari experiences across Sri Lanka&apos;s wildest national parks — guided by people who love the jungle.
          </p>

        </div>
        <div>
          <h4 className="eyebrow mb-5">Explore</h4>
          <ul className="space-y-2.5 text-sm">
            {[
              ["Safari Packages", "/client/packages"],
              ["Destinations", "/client/destinations"],
              ["Safari Vehicles", "/client/vehicles"],
              ["Gallery", "/client/gallery"],
              ["Reviews", "/client/reviews"],
            ].map(([l, h]) => (
              <li key={h}><Link href={h} className="hover:text-gold">{l}</Link></li>
            ))}
          </ul>
        </div>
        <div>
          <h4 className="eyebrow mb-5">Parks</h4>
          <ul className="space-y-2.5 text-sm">
            {["Yala", "Udawalawe", "Minneriya", "Wilpattu", "Bundala", "Kumana"].map((p) => (
              <li key={p}><Link href="/client/destinations" className="hover:text-gold">{p} National Park</Link></li>
            ))}
          </ul>
        </div>
        <div>
          <h4 className="eyebrow mb-5">Contact</h4>
          <ul className="space-y-3 text-sm">
            <li><Link href="/client/contact" className="hover:text-gold">Contact our safari team</Link></li>
          </ul>
        </div>
      </div>
      <div className="border-t border-white/10">
        <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-2 px-5 py-6 text-xs text-white/40 sm:flex-row lg:px-8">
          <p>© {new Date().getFullYear()} WildTrail Safaris. All rights reserved.</p>
          <p>Photography via Pexels</p>
        </div>
      </div>
    </footer>
  );
}


