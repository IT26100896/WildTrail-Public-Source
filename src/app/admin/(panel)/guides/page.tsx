import { GuidesManager, type GuideItem } from "@/components/admin/managers";
import { backendApi } from "@/lib/backend-api";
import type { BookingDetail } from "@/types/domain";
export default async function GuidesPage() {
  const [guides, bookings, assignments] = await Promise.all([backendApi<GuideItem[]>("/api/guides"), backendApi<BookingDetail[]>("/api/bookings"), backendApi<{ bookingId: number; guideId: number }[]>("/api/guides/assignments")]);
  return <GuidesManager initial={guides} bookings={bookings} initialAssignments={assignments} />;
}

