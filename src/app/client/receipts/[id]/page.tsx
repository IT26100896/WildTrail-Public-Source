import { redirect } from "next/navigation";
import { getCustomerSession, getAdminSession } from "@/lib/session";
import { backendApi } from "@/lib/backend-api";
import type { BookingDetail } from "@/types/domain";
import { money, fmtDate, fmtTime } from "@/lib/content";
import PrintButton from "@/components/client/PrintButton";
export default async function ReceiptPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  if (!await getCustomerSession() && !await getAdminSession()) redirect("/client/login");
  const b = await backendApi<BookingDetail>(`/api/bookings/${encodeURIComponent(id)}/receipt`);
  return <main className="mx-auto max-w-2xl px-6 py-32 print:py-8"><p className="eyebrow">WildTrail Safaris</p><h1 className="my-5 font-display text-3xl">{b.paymentStatus === "PAID" ? "Payment receipt" : "Booking statement"}</h1><p className="font-mono">{b.reference}</p><dl className="my-8 grid gap-5 sm:grid-cols-2">{[["Customer", b.customerName], ["Email", b.customerEmail], ["Package", b.packageName], ["Destination", b.destinationName], ["Date", `${fmtDate(b.safariDate)} ${fmtTime(b.timeSlot)}`], ["Vehicle", b.vehicleName], ["Guests", b.guests], ["Booking status", b.status], ["Payment status", b.paymentStatus ?? "No payment record"], ["Transaction", b.transactionRef ?? "—"]].map(([label, value]) => <div key={label}><dt className="text-xs uppercase text-charcoal/60">{label}</dt><dd className="break-words font-medium">{value}</dd></div>)}</dl><p className="mb-8 text-2xl">Total: <strong>{money(b.totalAmount)}</strong></p>{b.paymentStatus !== "PAID" && <p className="mb-5 text-sm">This statement is not proof of payment.</p>}<PrintButton /></main>;
}

