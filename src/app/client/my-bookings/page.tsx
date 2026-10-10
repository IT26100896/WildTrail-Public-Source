import Link from "next/link";
import { redirect } from "next/navigation";
import { PageHero } from "@/components/client/ClientShell";
import { MyBookingsList } from "@/components/client/forms";
import { backendApi } from "@/lib/backend-api";
import type { BookingDetail } from "@/types/domain";
import { getCustomerSession } from "@/lib/session";
import { IMG } from "@/lib/content";

export const metadata = { title: "My Bookings — WildTrail" };

export default async function MyBookingsPage() {
  const me = await getCustomerSession();
  if (!me) redirect("/client/login?next=/client/my-bookings");
  const bookings = await backendApi<BookingDetail[]>("/api/bookings/mine");
  return (
    <>
      <PageHero eyebrow={`Welcome, ${me.name}`} title="My safari bookings" image={IMG.jeepGroup} />
      <section className="mx-auto max-w-5xl px-5 py-16">
        <div className="mb-6 flex items-center justify-between">
          <p className="text-sm text-charcoal/60">{bookings.length} booking(s)</p>
          <Link href="/client/booking" className="btn-dark !py-2.5">+ New booking</Link>
        </div>
        <MyBookingsList initial={bookings} />
      </section>
    </>
  );
}

