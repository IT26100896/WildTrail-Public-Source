import { BookingsManager } from "@/components/admin/managers";
import { backendApi } from "@/lib/backend-api";
import type { BookingDetail } from "@/types/domain";

export default async function AdminBookingsPage() {
  // Staff receives the complete list; BookingsManager handles filters and actions.
  return <BookingsManager initial={await backendApi<BookingDetail[]>("/api/bookings")} />;
}

