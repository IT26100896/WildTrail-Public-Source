import { redirect } from "next/navigation";
import Link from "next/link";
import MyReviews, { type MyReview } from "@/components/client/MyReviews";
import { getCustomerSession } from "@/lib/session";
import { backendApi } from "@/lib/backend-api";

export const metadata = { title: "My reviews — WildTrail" };
export default async function MyReviewsPage() {
  if (!await getCustomerSession()) redirect("/client/login?next=/client/my-reviews");
  const rows = await backendApi<MyReview[]>("/api/reviews/mine");
  return <div className="mx-auto min-h-screen max-w-4xl px-5 pb-20 pt-28"><p className="eyebrow">Your stories</p><MyReviews initial={rows} /><Link href="/client/reviews" className="btn-dark mt-6">Write a new review</Link></div>;
}
