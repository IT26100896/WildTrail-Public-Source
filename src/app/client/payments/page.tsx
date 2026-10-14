import Link from "next/link";
import { redirect } from "next/navigation";
import { getWebsiteSession } from "@/lib/session";
import { backendApi } from "@/lib/backend-api";
import type { BookingDetail } from "@/types/domain";
import PaymentSummary from "@/components/client/PaymentSummary";
import { money } from "@/lib/content";

export const metadata = { title: "My payments — WildTrail" };
export default async function PaymentsPage() {
  if (!await getWebsiteSession()) redirect("/client/login?next=/client/payments");
  const bookings = await backendApi<BookingDetail[]>("/api/bookings/mine");
  const total = (status: string) => bookings.filter(booking => booking.paymentStatus === status)
    .reduce((sum,booking) => sum + (booking.paymentAmount ?? 0),0);
  return <div className="mx-auto max-w-5xl px-5 pb-20 pt-28">
    <h1 className="font-display text-3xl">My payments</h1>
    <div className="my-6 grid gap-4 sm:grid-cols-3">
      {[["Collected",total("PAID")],["Pending",total("PENDING")],["Refunded",total("REFUNDED")]].map(([label,amount]) => <div key={label} className="card p-5"><p>{label}</p><p className="mt-2 text-2xl font-semibold">{money(Number(amount))}</p></div>)}
    </div>
    {bookings.map(booking => <article key={booking.id} className="card mb-5 p-5">
      <h2 className="font-semibold">{booking.reference} · {booking.packageName}</h2>
      <p className="mt-2 text-sm">Vehicle number: {booking.vehicleNumber ?? "Pending assignment"}</p>
      <p className="text-sm">Pickup place: {booking.pickupPlace ?? "Contact staff to arrange pickup"}</p>
      <PaymentSummary booking={booking} />
      <Link className="mt-4 inline-block text-sm underline" href={`/client/receipts/${booking.id}`}>View statement / receipt</Link>
    </article>)}
    {!bookings.length && <p className="card p-8">No payments yet. Your payment details appear after booking a safari.</p>}
  </div>;
}
