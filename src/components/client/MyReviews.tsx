"use client";
import { useState } from "react";
import { api } from "@/lib/api";
export interface MyReview { id: number; rating: number; comment: string; approved: boolean; createdAt: string }
export default function MyReviews({ initial }: { initial: MyReview[] }) {
  const [rows, setRows] = useState(initial); const [edit, setEdit] = useState<MyReview | null>(null); const [error, setError] = useState(""); const [busy, setBusy] = useState(false);
  const reload = async () => setRows(await api<MyReview[]>("/api/reviews/mine"));
  const save = async (e: React.FormEvent) => { e.preventDefault(); if (!edit || busy) return; setBusy(true); try { await api(`/api/reviews/mine/${edit.id}`, "PUT", edit); await reload(); setEdit(null); } catch(e) { setError((e as Error).message); } finally { setBusy(false); } };
  const remove = async (id: number) => { if (!confirm("Delete your review?")) return; try { await api(`/api/reviews/mine/${id}`, "DELETE"); await reload(); } catch(e) { setError((e as Error).message); } };
  return <section className="mt-10"><h2 className="mb-4 font-display text-2xl">My reviews</h2>{error && <p role="alert" className="my-3 text-red-700">{error}</p>}{rows.map(row => <article className="card my-4 p-4" key={row.id}><p>{"★".repeat(row.rating)} · {row.approved ? "Published" : "Awaiting moderation"}</p><p className="my-3 break-words">{row.comment}</p><button className="mr-4 text-sm underline" onClick={() => setEdit(row)}>Edit</button><button className="text-sm text-red-700" onClick={() => remove(row.id)}>Delete</button></article>)}{!rows.length && <p>No reviews submitted yet.</p>}{edit && <form className="card space-y-4 p-5" onSubmit={save}><h3>Edit review</h3><label htmlFor="review-rating">Rating</label><select id="review-rating" className="input" value={edit.rating} onChange={e => setEdit({ ...edit, rating: Number(e.target.value) })}>{[1,2,3,4,5].map(n => <option key={n}>{n}</option>)}</select><label htmlFor="review-comment">Comment</label><textarea id="review-comment" className="input" required maxLength={1000} value={edit.comment} onChange={e => setEdit({ ...edit, comment: e.target.value })} /><p className="text-xs">Edited reviews are submitted for moderation again.</p><button disabled={busy} className="btn-dark">Save review</button><button type="button" className="ml-4 text-sm" onClick={() => setEdit(null)}>Cancel</button></form>}</section>;
}

