import { PageHero, SectionHeading } from "@/components/client/ClientShell";
import { Stars } from "@/components/client/cards";
import { ReviewForm } from "@/components/client/forms";
import { ReviewCarousel } from "@/components/client/interactive";
import { loadPublicReviews } from "@/components/client/sections";
import { getCustomerSession } from "@/lib/session";
import { IMG, fmtDate } from "@/lib/content";
import Link from "next/link";
import MyReviews, { type MyReview } from "@/components/client/MyReviews";
import { backendApi } from "@/lib/backend-api";

export const metadata = { title: "Guest Reviews — WildTrail" };

export default async function ReviewsPage() {
  const [reviews, me] = await Promise.all([loadPublicReviews(), getCustomerSession()]);
  const realReviews = reviews.filter((r) => !r.sample);
  const avg = realReviews.length ? realReviews.reduce((s, r) => s + r.rating, 0) / realReviews.length : 0;
  const mine = me ? await backendApi<MyReview[]>("/api/reviews/mine") : [];
  return (
    <>
      <PageHero eyebrow="Guest reviews" title="Told by our travellers" image={IMG.elephantElla} />
      <section className="bg-jungle-900 py-20">
        <div className="mx-auto max-w-7xl px-5 lg:px-8">
          <div className="mb-12 text-center text-white">
            <p className="font-display text-6xl text-gold">{realReviews.length ? avg.toFixed(1) : "Sample stories"}</p>
            <p className="mt-1 text-sm text-white/60">{realReviews.length ? `average from ${realReviews.length} published reviews` : "Fictional examples for the campus project; no customer ratings yet."}</p>
          </div>
          <ReviewCarousel reviews={reviews} />
        </div>
      </section>
      <section className="mx-auto grid max-w-7xl gap-12 px-5 py-20 lg:grid-cols-[3fr_2fr] lg:px-8">
        <div>
          <SectionHeading center={false} eyebrow="All reviews" title="Recent stories" />
          <div className="space-y-4">
            {reviews.map((r) => (
              <article key={r.id} className="reveal card p-6">
                <div className="flex items-center gap-4">
                  <span className="grid h-12 w-12 place-items-center rounded-full bg-jungle-900 font-display text-gold">{r.authorName.split(" ").map((s) => s[0]).join("").slice(0, 2)}</span>
                  <div className="flex-1">
                    <p className="font-semibold text-jungle-950">{r.authorName}</p>
                    {r.sample && <p className="text-xs text-gold-dark">Fictional sample review</p>}
                    <p className="text-xs text-charcoal/50">{r.country ?? "Traveller"} · {fmtDate(r.createdAt)}</p>
                  </div>
                  <Stars n={r.rating} />
                </div>
                <p className="mt-4 text-sm leading-relaxed text-charcoal/75">{r.comment}</p>
              </article>
            ))}
          </div>
        </div>
        <aside className="lg:sticky lg:top-28 lg:self-start">
          <div className="card p-8">
            <p className="eyebrow">Share your story</p>
            <h3 className="mt-2 mb-6 font-display text-2xl font-semibold text-jungle-950">Write a review</h3>
            {me ? <ReviewForm defaultName={me.name} /> : <Link href="/client/login?next=/client/reviews" className="btn-dark">Sign in to write a review</Link>}
            {me && <MyReviews initial={mine} />}
          </div>
        </aside>
      </section>
    </>
  );
}

