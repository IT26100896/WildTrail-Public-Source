"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { api } from "@/lib/api";

export default function AdminLoginForm() {
  const router = useRouter();
  const [f, setF] = useState({ email: "", password: "" });
  const [err, setErr] = useState("");
  const [busy, setBusy] = useState(false);
  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setErr("");
    try {
      await api("/api/auth/admin-login", "POST", f);
      router.replace("/admin/dashboard");
      router.refresh();
    } catch (e) {
      setErr((e as Error).message);
      setBusy(false);
    }
  };
  return (
    <form onSubmit={submit} className="space-y-4">
      {err && <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700 ring-1 ring-red-200">{err}</p>}
      <div><label htmlFor="admin-email" className="a-label">Email</label><input id="admin-email" type="email" autoComplete="username" required maxLength={160} className="a-input" value={f.email} onChange={(e) => setF({ ...f, email: e.target.value })} /></div>
      <div><label htmlFor="admin-password" className="a-label">Password</label><input id="admin-password" type="password" autoComplete="current-password" required maxLength={72} className="a-input" value={f.password} onChange={(e) => setF({ ...f, password: e.target.value })} /></div>
      <button disabled={busy} className="a-btn w-full justify-center py-2.5">{busy ? "Signing in…" : "Sign in to dashboard"}</button>
    </form>
  );
}

