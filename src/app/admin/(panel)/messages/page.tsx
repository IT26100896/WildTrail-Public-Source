import { backendApi } from "@/lib/backend-api";
import { PageHeader } from "@/components/admin/managers";
import { fmtDate } from "@/lib/content";
interface Message { senderRole: string | null; id: number; name: string; email: string; phone: string | null; message: string; createdAt: string }
export default async function InboxPage() {
  const messages = await backendApi<Message[]>("/api/admin/messages");
  return <><PageHeader title="Contact inbox" sub={`${messages.length} messages received`} /><div className="space-y-4">{messages.map(m => <article key={m.id} className="rounded-xl bg-white p-6 ring-1 ring-slate-200"><div className="flex flex-wrap justify-between gap-3"><h2 className="font-semibold">{m.name}</h2><p className="text-xs text-slate-500">{fmtDate(m.createdAt)}</p></div><a className="text-sm text-jungle-800 underline" href={`mailto:${m.email}`}>{m.email}</a><p className="text-xs text-gold-dark">{m.senderRole === "ROLE_ADMIN" ? "Administrator" : m.senderRole === "ROLE_CUSTOMER" ? "Customer" : "Visitor"}</p>{m.phone && <p className="text-sm text-slate-500">{m.phone}</p>}<p className="mt-4 whitespace-pre-wrap break-words text-sm">{m.message}</p></article>)}{!messages.length && <p className="rounded-xl bg-white p-8 text-slate-500">No contact messages yet.</p>}</div></>;
}
