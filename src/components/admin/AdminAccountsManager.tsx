"use client";
import { useState } from "react";
import { api } from "@/lib/api";
export type AdminAccount={id:number;email:string;fullName:string;staffTitle:string};
export default function AdminAccountsManager({initial}:{initial:AdminAccount[]}) {
 const [accounts,setAccounts]=useState(initial);
 const [busy,setBusy]=useState(false);
 const [error,setError]=useState("");const [success,setSuccess]=useState("");
 async function submit(event:React.FormEvent<HTMLFormElement>){
  event.preventDefault();const form=event.currentTarget;setError("");setSuccess("");setBusy(true);
  try {const body=Object.fromEntries(new FormData(form));await api("/api/admin/accounts","POST",body);setAccounts(await api<AdminAccount[]>("/api/admin/accounts"));form.reset();setSuccess("Administrator account created. They can sign in with their email and password.");}
  catch(e){setError((e as Error).message);}finally{setBusy(false);}
 }
 return <div className="mx-auto max-w-5xl space-y-7">
  <div><h1 className="text-2xl font-semibold">Administrators</h1><p className="mt-2 text-sm text-slate-500">Manage staff access to the operations dashboard.</p></div>
  <section className="rounded-xl border border-slate-200 bg-white p-5"><h2 className="mb-4 text-lg font-semibold">Add administrator</h2>
   <form className="grid gap-4 sm:grid-cols-2" onSubmit={submit}>
    <div><label className="label" htmlFor="admin-name">Full name</label><input id="admin-name" name="fullName" className="input" required maxLength={120} autoComplete="off"/></div>
    <div><label className="label" htmlFor="admin-email">Email</label><input id="admin-email" name="email" type="email" className="input" required maxLength={160} autoComplete="off"/></div>
    <div><label className="label" htmlFor="admin-title">Staff title</label><input id="admin-title" name="staffTitle" className="input" required maxLength={80}/></div>
    <div><label className="label" htmlFor="admin-password">New administrator password</label><input id="admin-password" name="password" type="password" className="input" required minLength={12} maxLength={72} autoComplete="new-password"/><p className="mt-1 text-xs text-slate-500">At least 12 characters.</p></div>
    <div><label className="label" htmlFor="current-password">Your current password</label><input id="current-password" name="currentPassword" type="password" className="input" required autoComplete="current-password"/><p className="mt-1 text-xs text-slate-500">Confirm your identity before granting administrator access.</p></div>
    <div className="flex items-end"><button className="btn-gold" disabled={busy}>{busy?"Creating…":"Create administrator"}</button></div>
    {error&&<p role="alert" className="text-sm text-red-700 sm:col-span-2">{error}</p>}
    {success&&<p role="status" className="text-sm text-emerald-700 sm:col-span-2">{success}</p>}
   </form>
  </section>
  <section className="rounded-xl border border-slate-200 bg-white p-5"><h2 className="mb-4 text-lg font-semibold">Staff accounts</h2><ul className="divide-y divide-slate-100">{accounts.map(a=><li key={a.id} className="break-words py-3"><p className="font-semibold">{a.fullName}</p><p className="text-sm text-slate-600">{a.email}</p><p className="text-xs text-slate-500">{a.staffTitle}</p></li>)}</ul></section>
 </div>;
}
