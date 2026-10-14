"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { fmtDate } from "@/lib/content";
import type { ReviewReply } from "@/types/domain";

export default function ReviewReplies({ reviewId, replies = [], canReply = false, onChange }:
  { reviewId: number; replies?: ReviewReply[]; canReply?: boolean; onChange?: () => Promise<void> }) {
  const router = useRouter();
  const [comment, setComment] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  async function refresh() {
    if (onChange) await onChange();
    router.refresh();
  }
  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (busy) return;
    setBusy(true); setError("");
    try {
      await api(`/api/reviews/${reviewId}/replies`, "POST", { comment });
      setComment(""); await refresh();
    } catch (error) { setError((error as Error).message); }
    finally { setBusy(false); }
  }
  async function remove(id: number) {
    if (busy || !confirm("Delete this reply?")) return;
    setBusy(true); setError("");
    try { await api(`/api/reviews/${reviewId}/replies/${id}`, "DELETE"); await refresh(); }
    catch (error) { setError((error as Error).message); }
    finally { setBusy(false); }
  }
  return <div className="mt-4 space-y-3">
    {replies.map(reply => <div key={reply.id} className="rounded-lg border-l-4 border-gold bg-cream/60 p-4 text-sm">
      <p className="font-semibold">{reply.authorName} <span className="text-xs font-normal text-gold-dark">· Administrator reply</span></p>
      {reply.authorEmail && <p className="break-all text-xs text-charcoal/60">{reply.authorEmail}</p>}
      <p className="text-xs text-charcoal/50">{fmtDate(reply.createdAt)}</p>
      <p className="mt-2 whitespace-pre-wrap break-words">{reply.comment}</p>
      {canReply && <button disabled={busy} className="mt-2 text-xs text-red-700 underline" onClick={() => remove(reply.id)}>Delete reply</button>}
    </div>)}
    {error && <p role="alert" className="text-sm text-red-700">{error}</p>}
    {canReply && <form onSubmit={submit} className="space-y-2">
      <label htmlFor={`reply-${reviewId}`} className="block text-sm font-medium">Reply as administrator</label>
      <textarea id={`reply-${reviewId}`} className="input" required maxLength={1000} value={comment} onChange={event => setComment(event.target.value)} />
      <button disabled={busy} className="btn-dark !px-4 !py-2">{busy ? "Saving…" : "Post reply"}</button>
    </form>}
  </div>;
}
