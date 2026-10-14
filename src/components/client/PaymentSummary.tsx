import type { BookingDetail } from "@/types/domain";
import { money } from "@/lib/content";

/** Database timestamps are interpreted in the same Sri Lankan timezone as departures. */
export function paymentDate(value: string | null) {
  if (!value) return "Not paid yet";
  const timestamp = value.replace(" ", "T");
  const zoned = /(?:Z|[+-]\d{2}:?\d{2})$/i.test(timestamp) ? timestamp : `${timestamp}+05:30`;
  return new Intl.DateTimeFormat("en-GB", {
    timeZone: "Asia/Colombo", dateStyle: "medium", timeStyle: "short",
  }).format(new Date(zoned));
}

export default function PaymentSummary({ booking }: { booking: BookingDetail }) {
  const rows = [
    ["Safari cost", booking.packageAmount == null ? "Included in total" : money(booking.packageAmount)],
    ["Vehicle hire", booking.vehicleAmount == null ? "Included in total" : money(booking.vehicleAmount)],
    ["Total booking amount", money(booking.totalAmount)],
    ["Recorded payment amount", booking.paymentAmount == null ? "No payment record" : money(booking.paymentAmount)],
    ["Payment method", booking.paymentMethod === "CARD" && booking.transactionRef?.startsWith("DEMO-") ? "Card demo (no charge)" : booking.paymentMethod ?? "—"],
    ["Payment status", booking.paymentStatus ?? "No payment record"],
    ["Payment date", paymentDate(booking.paidAt)],
    ["Transaction reference", booking.transactionRef ?? "—"],
  ];
  return <section className="mt-6 rounded-xl border border-jungle-900/10 bg-cream/40 p-5">
    <h2 className="font-display text-2xl text-jungle-950">Payment summary</h2>
    <h3 className="mt-3 text-sm font-semibold text-jungle-950">Payment details</h3>
    <dl className="mt-3 grid gap-x-6 gap-y-3 sm:grid-cols-2">
      {rows.map(([label,value]) => <div key={label} className="min-w-0">
        <dt className="text-xs text-charcoal/60">{label}</dt>
        <dd className="break-words text-sm font-medium text-jungle-950">{value}</dd>
      </div>)}
    </dl>
    {booking.paymentStatus !== "PAID" && <p className="mt-4 text-xs text-charcoal/70">This statement is not proof of payment. A payment date appears when staff record collected payment.</p>}
  </section>;
}
