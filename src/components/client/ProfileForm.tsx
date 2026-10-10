"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
export interface Profile { id: number; fullName: string; email: string; phone: string | null; country: string | null }
export default function ProfileForm({ initial }: { initial: Profile }) {
  const router = useRouter();
  const [form, setForm] = useState({ fullName: initial.fullName, email: initial.email, phone: initial.phone ?? "", country: initial.country ?? "", password: "", newPassword: "" });
  const [message, setMessage] = useState(""); const [busy, setBusy] = useState(false);
  const save = async (e: React.FormEvent) => { e.preventDefault(); if (busy) return; setBusy(true); setMessage(""); try { await api("/api/profile", "PUT", form); setForm(f => ({ ...f, password: "", newPassword: "" })); setMessage("Profile updated."); router.refresh(); } catch(e) { setMessage((e as Error).message); } finally { setBusy(false); } };
  const remove = async () => { if (!confirm("Delete your account? Accounts with bookings must be retained.")) return; setBusy(true); try { await api("/api/profile", "DELETE"); router.replace("/client/home"); router.refresh(); } catch(e) { setMessage((e as Error).message); setBusy(false); } };
  return <form onSubmit={save} className="space-y-5">{message && <p role="status" className="rounded-lg bg-cream p-3">{message}</p>}{([['fullName', 'Full name', 'text'], ['email', 'Email', 'email'], ['phone', 'Phone', 'tel'], ['country', 'Country', 'text'], ['password', 'Current password (for password change)', 'password'], ['newPassword', 'New password (optional)', 'password']] as const).map(([key, label, type]) => <div key={key}><label htmlFor={`profile-${key}`} className="label">{label}</label><input id={`profile-${key}`} type={type} className="input" required={key === "email" || key === "fullName"} value={form[key]} maxLength={key.includes("assword") ? 72 : 120} onChange={e => setForm({ ...form, [key]: e.target.value })} autoComplete={key === "password" ? "current-password" : key === "newPassword" ? "new-password" : undefined} /></div>)}<button disabled={busy} className="btn-dark">{busy ? "Saving…" : "Save profile"}</button><button type="button" disabled={busy} className="ml-5 text-sm text-red-700" onClick={remove}>Delete account</button></form>;
}

