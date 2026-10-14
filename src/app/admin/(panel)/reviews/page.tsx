import { ReviewsManager } from "@/components/admin/managers";
import { backendApi } from "@/lib/backend-api";
import type { ReviewRow } from "@/types/domain";

export default async function AdminReviewsPage() {
  return <ReviewsManager initial={await backendApi<ReviewRow[]>("/api/reviews?all=1")} />;
}
