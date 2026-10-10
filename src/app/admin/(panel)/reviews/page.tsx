import { ReviewsManager } from "@/components/admin/managers";
import { backendApi } from "@/lib/backend-api";

export default async function AdminReviewsPage() {
  const list = await backendApi<{
    id: number;
    authorName: string;
    country: string | null;
    rating: number;
    comment: string;
    approved: boolean;
    createdAt: string;
  }[]>("/api/reviews?all=1");
  return <ReviewsManager initial={list} />;
}

