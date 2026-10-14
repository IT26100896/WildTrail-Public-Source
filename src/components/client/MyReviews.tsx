"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import type { ReviewRow } from "@/types/domain";
import ReviewReplies from "./ReviewReplies";

export type MyReview = ReviewRow;
export default function MyReviews({ initial }: { initial: MyReview[] }) {
  const router = useRouter();
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  async function remove(id: number) {
    if (busy || !confirm("Delete your review?")) return;
    setBusy(true); setError("");
    try { await api(`/api/reviews/mine/${id}`, "DELETE"); router.refresh(); }
    catch (error) { setError((error as Error).message); }
    finally { setBusy(false); }
  }
  return <section className="mt-10">
    <h2 className="mb-4 font-display text-2xl">My reviews</h2>
    {error && <p role="alert" className="my-3 text-red-700">{error}</p>}
    {initial.map(row => <article className="card my-4 p-4" key={row.id}>
      <p>{"★".repeat(row.rating)} · Published</p>
      <p className="my-3 whitespace-pre-wrap break-words">{row.comment}</p>
      <ReviewReplies reviewId={row.id} replies={row.replies} />
      <button disabled={busy} className="mt-3 text-sm text-red-700 underline" onClick={() => remove(row.id)}>Delete review</button>
    </article>)}
    {!initial.length && <p>No reviews submitted yet.</p>}
  </section>;
}
