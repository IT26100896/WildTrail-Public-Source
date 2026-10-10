import { BookingsManager } from "@/components/admin/managers";
import { backendApi } from "@/lib/backend-api";
import type { BookingDetail } from "@/types/domain";

export default async function AdminBookingsPage() {
  return <BookingsManager initial={await backendApi<BookingDetail[]>("/api/bookings")} />;
}

