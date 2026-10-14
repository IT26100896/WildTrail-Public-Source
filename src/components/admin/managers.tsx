"use client";

/* eslint-disable @next/next/no-img-element */
import { useMemo, useState, type ReactNode } from "react";
import type { PackageDTO } from "@/types/domain";
import type { AdminVehicleDTO } from "@/types/domain";
import type { BookingDetail, PaymentDetail } from "@/types/domain";
import { api } from "@/lib/api";
import { fmtDate, fmtTime, money } from "@/lib/content";
import Link from "next/link";
import type { ReviewRow } from "@/types/domain";
import PaymentSummary, { paymentDate } from "@/components/client/PaymentSummary";
import ReviewReplies from "@/components/client/ReviewReplies";

// ---------------------------------------------------------------- shared UI
const BADGE: Record<string, string> = {
  PENDING: "bg-amber-100 text-amber-800",
  CONFIRMED: "bg-emerald-100 text-emerald-800",
  COMPLETED: "bg-sky-100 text-sky-800",
  CANCELLED: "bg-red-100 text-red-700",
  PAID: "bg-emerald-100 text-emerald-800",
  REFUNDED: "bg-violet-100 text-violet-800",
  FAILED: "bg-red-100 text-red-700",
  ACTIVE: "bg-emerald-100 text-emerald-800",
  APPROVED: "bg-emerald-100 text-emerald-800",
  INACTIVE: "bg-slate-200 text-slate-600",
};
export function Badge({ s }: { s: string | null }) {
  return <span className={`inline-block rounded-full px-2 py-0.5 text-[11px] font-semibold ${BADGE[s ?? ""] ?? "bg-slate-100 text-slate-600"}`}>{s ?? "—"}</span>;
}

export function PageHeader({ title, sub, action }: { title: string; sub?: string; action?: ReactNode }) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">{title}</h1>
        {sub && <p className="text-sm text-slate-500">{sub}</p>}
      </div>
      {action}
    </div>
  );
}

function Table({ head, children, empty }: { head: string[]; children: ReactNode; empty: boolean }) {
  return (
    <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
      <table className="w-full text-left text-sm">
        <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase tracking-wider text-slate-500">
          <tr>{head.map((h) => <th key={h} className="whitespace-nowrap px-4 py-3 font-medium">{h}</th>)}</tr>
        </thead>
        <tbody className="divide-y divide-slate-100">{children}</tbody>
      </table>
      {empty && <p className="p-10 text-center text-sm text-slate-400">No records found.</p>}
    </div>
  );
}

function Search({ value, onChange, placeholder = "Search…" }: { value: string; onChange: (v: string) => void; placeholder?: string }) {
  return <input className="a-input max-w-xs" placeholder={placeholder} value={value} onChange={(e) => onChange(e.target.value)} />;
}

function Toast({ msg, onClose }: { msg: { ok: boolean; text: string } | null; onClose: () => void }) {
  if (!msg) return null;
  return (
    <div className={`fixed bottom-5 right-5 z-[60] flex items-center gap-3 rounded-lg px-4 py-3 text-sm shadow-lg ${msg.ok ? "bg-jungle-900 text-white" : "bg-red-600 text-white"}`}>
      {msg.text}
      <button onClick={onClose} className="opacity-70 hover:opacity-100">✕</button>
    </div>
  );
}

function useToast() {
  const [msg, setMsg] = useState<{ ok: boolean; text: string } | null>(null);
  const show = (ok: boolean, text: string) => {
    setMsg({ ok, text });
    setTimeout(() => setMsg(null), 3500);
  };
  return { msg, show, clear: () => setMsg(null) };
}

// ---------------------------------------------------------------- generic form modal
type Field = {
  name: string;
  label: string;
  type?: "text" | "number" | "textarea" | "select" | "checkbox" | "time" | "email" | "tel";
  options?: [string | number, string][];
  full?: boolean;
  step?: string;
  maxLength?: number;
};
type FormValues = Record<string, string | number | boolean>;

function FormModal({ title, fields, initial, onClose, onSubmit }: { title: string; fields: Field[]; initial: FormValues; onClose: () => void; onSubmit: (v: FormValues) => Promise<void> }) {
  const [v, setV] = useState<FormValues>(initial);
  const [err, setErr] = useState("");
  const [busy, setBusy] = useState(false);
  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr("");
    setBusy(true);
    try {
      await onSubmit(v);
    } catch (e) {
      setErr((e as Error).message);
      setBusy(false);
    }
  };
  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-black/40 p-4 pt-16" onClick={onClose}>
      <form onSubmit={submit} onClick={(e) => e.stopPropagation()} className="w-full max-w-2xl rounded-xl bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
          <h2 className="text-lg font-semibold">{title}</h2>
          <button type="button" onClick={onClose} className="text-slate-400 hover:text-slate-700">✕</button>
        </div>
        <div className="grid gap-4 p-6 sm:grid-cols-2">
          {err && <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700 sm:col-span-2">{err}</p>}
          {fields.map((f) => (
            <div key={f.name} className={f.full || f.type === "textarea" ? "sm:col-span-2" : ""}>
              {f.type === "checkbox" ? (
                <label className="mt-6 flex items-center gap-2 text-sm">
                  <input type="checkbox" checked={Boolean(v[f.name])} onChange={(e) => setV({ ...v, [f.name]: e.target.checked })} className="h-4 w-4 accent-jungle-800" />
                  {f.label}
                </label>
              ) : (
                <>
                  <label htmlFor={`field-${f.name}`} className="a-label">{f.label}</label>
                  {f.type === "textarea" ? (
                    <textarea id={`field-${f.name}`} className="a-input min-h-24" maxLength={f.maxLength} value={String(v[f.name] ?? "")} onChange={(e) => setV({ ...v, [f.name]: e.target.value })} />
                  ) : f.type === "select" ? (
                    <select id={`field-${f.name}`} className="a-input" value={String(v[f.name] ?? "")} onChange={(e) => setV({ ...v, [f.name]: e.target.value })}>
                      {f.options?.map(([val, l]) => <option key={val} value={val}>{l}</option>)}
                    </select>
                  ) : (
                    <input id={`field-${f.name}`} type={f.type ?? "text"} step={f.step} maxLength={f.maxLength} className="a-input" value={String(v[f.name] ?? "")} onChange={(e) => setV({ ...v, [f.name]: e.target.value })} />
                  )}
                </>
              )}
            </div>
          ))}
        </div>
        <div className="flex justify-end gap-2 border-t border-slate-200 px-6 py-4">
          <button type="button" onClick={onClose} className="a-btn-light">Cancel</button>
          <button disabled={busy} className="a-btn">{busy ? "Saving…" : "Save"}</button>
        </div>
      </form>
    </div>
  );
}

/** Generic CRUD state hook — re-fetches the canonical list after each mutation. */
function useCrud<T extends { id: number }>(initial: T[], listUrl: string, baseUrl: string) {
  const [list, setList] = useState(initial);
  const toast = useToast();
  const reload = async () => setList(await api<T[]>(listUrl));
  const save = async (id: number | null, body: FormValues) => {
    await api(id ? `${baseUrl}/${id}` : baseUrl, id ? "PUT" : "POST", body);
    await reload();
    toast.show(true, id ? "Record updated" : "Record created");
  };
  const remove = async (id: number, label: string) => {
    if (!confirm(`Delete ${label}? This cannot be undone.`)) return;
    try {
      await api(`${baseUrl}/${id}`, "DELETE");
      await reload();
      toast.show(true, "Record deleted");
    } catch (e) {
      toast.show(false, (e as Error).message);
    }
  };
  return { list, save, remove, toast, reload };
}

// ================================================================ Packages
const PKG_TYPES: [string, string][] = [
  ["MORNING", "Morning Safari"], ["EVENING", "Evening Safari"], ["HALF_DAY", "Half Day Safari"], ["FULL_DAY", "Full Day Safari"],
  ["PRIVATE", "Private Safari"], ["FAMILY", "Family Safari"], ["PHOTOGRAPHY", "Photography Safari"],
];

export function PackagesManager({ initial, destinations }: { initial: PackageDTO[]; destinations: { id: number; name: string }[] }) {
  const { list, save, remove, toast } = useCrud(initial, "/api/packages?all=1", "/api/packages");
  const [edit, setEdit] = useState<PackageDTO | "new" | null>(null);
  const [q, setQ] = useState("");
  const rows = list.filter((p) => `${p.name} ${p.destinationName} ${p.typeLabel}`.toLowerCase().includes(q.toLowerCase()));
  const fields: Field[] = [
    { name: "name", label: "Package name", full: true },
    { name: "packageType", label: "Safari type", type: "select", options: PKG_TYPES },
    { name: "destinationId", label: "Destination", type: "select", options: destinations.map((d) => [d.id, d.name]) },
    { name: "durationHours", label: "Duration (hours)", type: "number" },
    { name: "startTime", label: "Start time", type: "time" },
    { name: "maxGuests", label: "Maximum guests", type: "number" },
    { name: "basePrice", label: "Base price (USD)", type: "number", step: "0.01" },
    { name: "imageUrl", label: "Image URL", full: true },
    { name: "description", label: "Description", type: "textarea" },
    { name: "active", label: "Active (visible on client website)", type: "checkbox" },
  ];
  const init = (p?: PackageDTO): FormValues =>
    p
      ? { name: p.name, packageType: p.type, destinationId: p.destinationId, durationHours: p.durationHours, startTime: p.startTime, maxGuests: p.maxGuests, basePrice: p.basePrice, imageUrl: p.imageUrl, description: p.description, active: p.active }
      : { name: "", packageType: "MORNING", destinationId: destinations[0]?.id ?? "", durationHours: 4, startTime: "05:30", maxGuests: 6, basePrice: 50, imageUrl: "", description: "", active: true };

  return (
    <>
      <PageHeader title="Safari Packages" sub={`${list.length} packages`} action={<div className="flex gap-2"><Search value={q} onChange={setQ} /><button className="a-btn" onClick={() => setEdit("new")}>+ New package</button></div>} />
      <Table head={["Package", "Type", "Destination", "Duration", "Start", "Max", "Price", "Status", ""]} empty={!rows.length}>
        {rows.map((p) => (
          <tr key={p.id} className="hover:bg-slate-50">
            <td className="px-4 py-3"><div className="flex items-center gap-3"><img src={p.imageUrl} alt="" className="h-10 w-14 rounded object-cover" /><span className="font-medium text-slate-900">{p.name}</span></div></td>
            <td className="px-4 py-3 text-slate-600">{p.typeLabel}<div className="text-[11px] text-slate-400">{p.pricingNote}</div></td>
            <td className="px-4 py-3">{p.destinationName}</td>
            <td className="px-4 py-3">{p.durationHours}h</td>
            <td className="px-4 py-3">{fmtTime(p.startTime)}</td>
            <td className="px-4 py-3">{p.maxGuests}</td>
            <td className="px-4 py-3 font-medium">{money(p.basePrice)}</td>
            <td className="px-4 py-3"><Badge s={p.active ? "ACTIVE" : "INACTIVE"} /></td>
            <td className="whitespace-nowrap px-4 py-3 text-right"><button className="a-btn-light mr-2" onClick={() => setEdit(p)}>Edit</button><button className="a-btn-danger" onClick={() => remove(p.id, p.name)}>Delete</button></td>
          </tr>
        ))}
      </Table>
      {edit && (
        <FormModal title={edit === "new" ? "New safari package" : `Edit · ${edit.name}`} fields={fields} initial={init(edit === "new" ? undefined : edit)} onClose={() => setEdit(null)} onSubmit={async (v) => { await save(edit === "new" ? null : edit.id, v); setEdit(null); }} />
      )}
      <Toast msg={toast.msg} onClose={toast.clear} />
    </>
  );
}

// ================================================================ Vehicles
export function VehiclesManager({ initial }: { initial: AdminVehicleDTO[] }) {
  const { list, save, remove, toast } = useCrud(initial, "/api/vehicles?all=1", "/api/vehicles");
  const [edit, setEdit] = useState<AdminVehicleDTO | "new" | null>(null);
  const fields: Field[] = [
    { name: "name", label: "Vehicle name", full: true },
    { name: "registrationNumber", label: "Vehicle number (registration plate)", full: true, maxLength: 32 },
    { name: "ownerName", label: "Owner full name (admin only)", full: true, maxLength: 120 },
    { name: "ownerPhone", label: "Owner telephone number", type: "tel", maxLength: 40 },
    { name: "ownerIdNumber", label: "Owner ID / NIC", maxLength: 40 },
    { name: "ownerAddress", label: "Owner address", type: "textarea", maxLength: 300 },
    { name: "category", label: "Category", type: "select", options: [["STANDARD", "Standard Safari Jeep"], ["HILUX", "Toyota Hilux Safari Jeep"], ["LUXURY", "Luxury Jeep"], ["FAMILY", "Family Safari Jeep"]] },
    { name: "capacity", label: "Capacity (seats)", type: "number" },
    { name: "price", label: "Hire price per safari (USD)", type: "number", step: "0.01" },
    { name: "imageUrl", label: "Image URL" },
    { name: "features", label: "Features (comma separated)", type: "textarea" },
    { name: "fourByFour", label: "4x4 drive", type: "checkbox" },
    { name: "available", label: "Available for booking", type: "checkbox" },
  ];
  const init = (v?: AdminVehicleDTO): FormValues =>
    v
      ? { name: v.name, registrationNumber: v.registrationNumber ?? "",
          ownerName: v.ownerName ?? "", ownerPhone: v.ownerPhone ?? "",
          ownerAddress: v.ownerAddress ?? "", ownerIdNumber: v.ownerIdNumber ?? "", category: v.category, capacity: v.capacity, price: v.price, imageUrl: v.imageUrl, features: v.features.join(", "), fourByFour: v.fourByFour, available: v.available }
      : { name: "", registrationNumber: "", ownerName: "", ownerPhone: "", ownerAddress: "", ownerIdNumber: "", category: "STANDARD", capacity: 6, price: 40, imageUrl: "", features: "", fourByFour: true, available: true };
  return (
    <>
      <PageHeader title="Vehicles" sub={`${list.length} vehicles in the fleet · owner details are visible to administrators only`} action={<button className="a-btn" onClick={() => setEdit("new")}>+ New vehicle</button>} />
      <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {list.map((v) => (
          <div key={v.id} className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
            <img src={v.imageUrl} alt="" className="h-36 w-full object-cover" />
            <div className="p-4">
              <div className="flex items-start justify-between gap-2">
                <div><p className="text-[11px] font-semibold uppercase text-slate-400">{v.categoryLabel}</p><p className="font-semibold text-slate-900">{v.name}</p></div>
                <Badge s={v.available ? "ACTIVE" : "INACTIVE"} />
              </div>
              <p className="mt-1 text-sm text-slate-500">Vehicle number: {v.registrationNumber ?? "Pending assignment"}<br />{v.capacity} seats · {v.fourByFour ? "4x4" : "2WD"} · <b className="text-slate-800">{money(v.price)}</b></p>
              <p className="mt-2 line-clamp-2 text-xs text-slate-500">{v.allFeatures.join(" · ")}</p>
              <details className="mt-3 rounded-lg bg-slate-50 p-3 text-xs">
                <summary className="cursor-pointer font-semibold text-slate-700">Owner details (admin only)</summary>
                <dl className="mt-3 space-y-2">
                  {[["Owner name",v.ownerName],["Telephone",v.ownerPhone],["Owner ID / NIC",v.ownerIdNumber],["Address",v.ownerAddress]].map(([label,value]) => <div key={label}>
                    <dt className="text-slate-500">{label}</dt><dd className="whitespace-pre-wrap break-words text-slate-800">{value || "Not recorded"}</dd>
                  </div>)}
                </dl>
              </details>
              <div className="mt-4 flex gap-2"><button className="a-btn-light flex-1 justify-center" onClick={() => setEdit(v)}>Edit</button><button className="a-btn-danger" onClick={() => remove(v.id, v.name)}>Delete</button></div>
            </div>
          </div>
        ))}
      </div>
      {edit && <FormModal title={edit === "new" ? "New vehicle" : `Edit · ${edit.name}`} fields={fields} initial={init(edit === "new" ? undefined : edit)} onClose={() => setEdit(null)} onSubmit={async (v) => { await save(edit === "new" ? null : edit.id, v); setEdit(null); }} />}
      <Toast msg={toast.msg} onClose={toast.clear} />
    </>
  );
}

// ================================================================ Customers
export interface CustomerItem { id: number; fullName: string; email: string; phone: string | null; country: string | null; userId: number | null; createdAt: string; bookingCount: number; totalSpent: number }

export interface GuideItem { id: number; fullName: string; phone: string; licenseNumber: string; specialty: string; available: boolean; assignmentRole: string }
export function GuidesManager({ initial, bookings, initialAssignments }: { initial: GuideItem[]; bookings: BookingDetail[]; initialAssignments: { bookingId: number; guideId: number }[] }) {
  const { list, save, remove, toast } = useCrud(initial, "/api/guides", "/api/guides");
  const [edit, setEdit] = useState<GuideItem | "new" | null>(null);
  const [assignments, setAssignments] = useState(initialAssignments);
  const [bookingId, setBookingId] = useState(0);
  const [guideId, setGuideId] = useState(0);
  const [assigning, setAssigning] = useState(false);
  const assign = async (e: React.FormEvent) => { e.preventDefault(); if (assigning) return; setAssigning(true); try { await api("/api/guides/assignments", "POST", { bookingId, guideId }); setAssignments(await api("/api/guides/assignments")); toast.show(true, "Guide assigned"); } catch(e) { toast.show(false, (e as Error).message); } finally { setAssigning(false); } };
  const fields: Field[] = [{ name: "fullName", label: "Guide name" }, { name: "phone", label: "Phone" }, { name: "licenseNumber", label: "Licence number" }, { name: "specialty", label: "Specialty", type: "select", options: [["DRIVER", "Driver"], ["NATURALIST", "Naturalist"]] }, { name: "available", label: "Available for assignments", type: "checkbox" }];
  return <><PageHeader title="Guides & resource schedules" sub="Register licensed staff and review vehicle departures." action={<button className="a-btn" onClick={() => setEdit("new")}>+ New guide</button>} /><Table head={["Name", "Phone", "Licence", "Role", "Status", "Actions"]} empty={!list.length}>{list.map(g => <tr key={g.id}><td className="p-4">{g.fullName}</td><td>{g.phone}</td><td>{g.licenseNumber}</td><td>{g.assignmentRole}</td><td><Badge s={g.available ? "ACTIVE" : "INACTIVE"} /></td><td><button className="a-btn-light mr-2" onClick={() => setEdit(g)}>Edit</button><button className="a-btn-danger" onClick={() => remove(g.id, g.fullName)}>Delete</button></td></tr>)}</Table>
  <h2 className="my-6 text-xl font-semibold">Assign or reassign a guide</h2><form onSubmit={assign} className="mb-6 flex flex-wrap items-end gap-3"><div className="min-w-0 max-w-full"><label htmlFor="assignment-booking" className="a-label">Active booking</label><select id="assignment-booking" className="a-input max-w-full" required value={bookingId} onChange={e => setBookingId(Number(e.target.value))}><option value={0} disabled>Select booking</option>{bookings.filter(b => ["PENDING", "CONFIRMED"].includes(b.status)).map(b => <option key={b.id} value={b.id}>{b.reference} · {fmtDate(b.safariDate)}</option>)}</select></div><div><label htmlFor="assignment-guide" className="a-label">Guide</label><select id="assignment-guide" className="a-input" required value={guideId} onChange={e => setGuideId(Number(e.target.value))}><option value={0} disabled>Select guide</option>{list.filter(g => g.available).map(g => <option key={g.id} value={g.id}>{g.fullName}</option>)}</select></div><button disabled={assigning || !bookingId || !guideId} className="a-btn">Assign guide</button></form>
  <h2 className="my-6 text-xl font-semibold">Vehicle departure schedule</h2><p className="mb-4 text-sm text-slate-500">Each guide can lead one safari per day.</p><Table head={["Reference", "Date", "Time", "Vehicle", "Guide", "Status"]} empty={!bookings.filter(b => b.status !== "CANCELLED").length}>{bookings.filter(b => b.status !== "CANCELLED").map(b => <tr key={b.id}><td className="p-4">{b.reference}</td><td>{fmtDate(b.safariDate)}</td><td>{fmtTime(b.timeSlot)}</td><td>{b.vehicleName}<p className="text-xs text-slate-500">{b.vehicleNumber ?? "Pending assignment"}</p></td><td>{list.find(g => g.id === assignments.find(a => a.bookingId === b.id)?.guideId)?.fullName ?? "Unassigned"}</td><td><Badge s={b.status} /></td></tr>)}</Table>
  {edit && <FormModal title={edit === "new" ? "Register guide" : "Edit guide"} fields={fields} initial={edit === "new" ? { fullName: "", phone: "", licenseNumber: "", specialty: "DRIVER", available: true } : { fullName: edit.fullName, phone: edit.phone, licenseNumber: edit.licenseNumber, specialty: edit.specialty, available: edit.available }} onClose={() => setEdit(null)} onSubmit={async v => { await save(edit === "new" ? null : edit.id, v); setEdit(null); }} />}<Toast msg={toast.msg} onClose={toast.clear} /></>;
}

export function FileReportsManager() {
  const [preview, setPreview] = useState("");
  const [busy, setBusy] = useState(false);
  const toast = useToast();
  const run = async (module: string, method: "GET" | "POST") => {
    if (busy) return; setBusy(true);
    try { const data = await api(`/api/admin/files/${module}`, method); setPreview(JSON.stringify(data, null, 2)); toast.show(true, method === "POST" ? "Report saved" : "Report loaded from file"); } catch (e) { toast.show(false, (e as Error).message); } finally { setBusy(false); }
  };
  return <><PageHeader title="File reports" sub="Save current records as readable text snapshots, then load them from disk. Password hashes are excluded." /><div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">{["users", "packages", "bookings", "vehicles", "payments", "reviews"].map(module => <section key={module} className="rounded-xl bg-white p-5 ring-1 ring-slate-200"><h2 className="mb-4 capitalize font-semibold">{module}</h2><button disabled={busy} className="a-btn mr-2" onClick={() => run(module, "POST")}>Save snapshot</button><button disabled={busy} className="a-btn-light" onClick={() => run(module, "GET")}>Read file</button></section>)}</div><pre className="mt-6 max-h-96 overflow-auto whitespace-pre-wrap break-all rounded-xl bg-slate-900 p-5 text-xs text-white">{preview || "Select a report to preview."}</pre><Toast msg={toast.msg} onClose={toast.clear} /></>;
}

export function CustomersManager({ initial }: { initial: CustomerItem[] }) {
  const { list, save, remove, toast } = useCrud(initial, "/api/customers", "/api/customers");
  const [edit, setEdit] = useState<CustomerItem | null>(null);
  const [q, setQ] = useState("");
  const rows = list.filter((c) => `${c.fullName} ${c.email} ${c.country ?? ""}`.toLowerCase().includes(q.toLowerCase()));
  const fields: Field[] = [
    { name: "fullName", label: "Full name" },
    { name: "email", label: "Email", type: "email" },
    { name: "phone", label: "Phone" },
    { name: "country", label: "Country" },
  ];
  return (
    <>
      <PageHeader title="Customers" sub={`${list.length} customers`} action={<Search value={q} onChange={setQ} placeholder="Search name, email, country…" />} />
      <Table head={["Customer", "Phone", "Country", "Account", "Bookings", "Total spent", "Since", ""]} empty={!rows.length}>
        {rows.map((c) => (
          <tr key={c.id} className="hover:bg-slate-50">
            <td className="px-4 py-3"><p className="font-medium text-slate-900">{c.fullName}</p><p className="text-xs text-slate-500">{c.email}</p></td>
            <td className="px-4 py-3">{c.phone ?? "—"}</td>
            <td className="px-4 py-3">{c.country ?? "—"}</td>
            <td className="px-4 py-3">{c.userId ? <Badge s="ACTIVE" /> : <span className="text-xs text-slate-400">Guest</span>}</td>
            <td className="px-4 py-3">{c.bookingCount}</td>
            <td className="px-4 py-3 font-medium">{money(c.totalSpent)}</td>
            <td className="px-4 py-3 text-slate-500">{fmtDate(c.createdAt)}</td>
            <td className="whitespace-nowrap px-4 py-3 text-right"><button className="a-btn-light mr-2" onClick={() => setEdit(c)}>Edit</button><button className="a-btn-danger" onClick={() => remove(c.id, `${c.fullName} and all their bookings`)}>Delete</button></td>
          </tr>
        ))}
      </Table>
      {edit && <FormModal title={`Edit · ${edit.fullName}`} fields={fields} initial={{ fullName: edit.fullName, email: edit.email, phone: edit.phone ?? "", country: edit.country ?? "" }} onClose={() => setEdit(null)} onSubmit={async (v) => { await save(edit.id, v); setEdit(null); }} />}
      <Toast msg={toast.msg} onClose={toast.clear} />
    </>
  );
}

// Staff may offer only the status changes allowed by the booking lifecycle.
const NEXT_STATUS: Record<string, string[]> = {
  PENDING: ["CONFIRMED", "CANCELLED"],
  CONFIRMED: ["COMPLETED", "CANCELLED"],
  COMPLETED: [],
  CANCELLED: [],
};

export function BookingsManager({ initial }: { initial: BookingDetail[] }) {
  const [list, setList] = useState(initial);
  const [q, setQ] = useState("");
  const [status, setStatus] = useState("");
  const [view, setView] = useState<BookingDetail | null>(null);
  const toast = useToast();
  // Filter the current list locally; mutations reload the authoritative server rows.
  const rows = useMemo(
    () => list.filter((b) => (!status || b.status === status) && `${b.reference} ${b.customerName} ${b.packageName} ${b.destinationName}`.toLowerCase().includes(q.toLowerCase())),
    [list, q, status],
  );
  const reload = async () => setList(await api<BookingDetail[]>("/api/bookings"));
  const change = async (b: BookingDetail, s: string) => {
    // The backend also validates transitions and payment requirements.
    try {
      await api(`/api/bookings/${b.id}`, "PATCH", { status: s });
      await reload();
      toast.show(true, `${b.reference} → ${s}`);
    } catch (e) {
      toast.show(false, (e as Error).message);
    }
  };
  const del = async (b: BookingDetail) => {
    // Deletion succeeds only after the backend's cancellation and refund checks.
    if (!confirm(`Delete booking ${b.reference}?`)) return;
    try {
      await api(`/api/bookings/${b.id}`, "DELETE");
      await reload();
      toast.show(true, "Booking deleted");
    } catch (e) {
      toast.show(false, (e as Error).message);
    }
  };
  return (
    <>
      <PageHeader
        title="Bookings"
        sub={`${list.length} bookings`}
        action={
          <div className="flex gap-2">
            <select className="a-input w-40" value={status} onChange={(e) => setStatus(e.target.value)}>
              <option value="">All statuses</option>
              {Object.keys(NEXT_STATUS).map((s) => <option key={s}>{s}</option>)}
            </select>
            <Search value={q} onChange={setQ} placeholder="Ref, customer, package…" />
          </div>
        }
      />
      <Table head={["Ref", "Customer", "Package", "Safari date", "Vehicle", "Guests", "Total", "Payment", "Status", ""]} empty={!rows.length}>
        {rows.map((b) => (
          <tr key={b.id} className="hover:bg-slate-50">
            <td className="px-4 py-3 font-mono text-xs"><button className="text-jungle-800 underline" onClick={() => setView(b)}>{b.reference}</button></td>
            <td className="px-4 py-3"><p className="font-medium">{b.customerName}</p><p className="text-xs text-slate-500">{b.customerEmail}</p><p className="text-xs text-gold-dark">{b.customerRole === "ROLE_ADMIN" ? "Administrator" : "Customer"}</p></td>
            <td className="px-4 py-3"><p>{b.packageName}</p><p className="text-xs text-slate-500">{b.destinationName}</p></td>
            <td className="whitespace-nowrap px-4 py-3">{fmtDate(b.safariDate)}<p className="text-xs text-slate-500">{fmtTime(b.timeSlot)}</p></td>
            <td className="px-4 py-3">{b.vehicleName}<p className="text-xs text-slate-500">{b.vehicleNumber ?? "Pending assignment"}</p></td>
            <td className="px-4 py-3">{b.guests}</td>
            <td className="px-4 py-3 font-medium">{money(b.totalAmount)}</td>
            <td className="px-4 py-3"><span className="text-xs text-slate-500">{b.paymentMethod}</span> <Badge s={b.paymentStatus} /></td>
            <td className="px-4 py-3"><Badge s={b.status} /></td>
            <td className="whitespace-nowrap px-4 py-3 text-right">
              {NEXT_STATUS[b.status].map((s) => (
                <button key={s} className={`mr-1.5 ${s === "CANCELLED" ? "a-btn-danger" : "a-btn-light"}`} onClick={() => change(b, s)}>{s === "CONFIRMED" ? "Confirm" : s === "COMPLETED" ? "Complete" : "Cancel"}</button>
              ))}
              <button className="a-btn-light" onClick={() => del(b)} title="Delete">🗑</button>
            </td>
          </tr>
        ))}
      </Table>
      {view && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={() => setView(null)}>
          <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-xl bg-white p-6 shadow-2xl" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between"><h2 className="font-mono text-lg font-semibold">{view.reference}</h2><Badge s={view.status} /></div>
            <dl className="mt-4 grid grid-cols-2 gap-3 text-sm">
              {[["Customer", view.customerName], ["Email", view.customerEmail], ["Phone", view.customerPhone ?? "—"], ["Destination", view.destinationName], ["Package", view.packageName], ["Vehicle", view.vehicleName], ["Vehicle number", view.vehicleNumber ?? "Pending assignment"], ["Pickup place", view.pickupPlace ?? "Not specified"], ["Account type", view.customerRole === "ROLE_ADMIN" ? "Administrator" : "Customer"], ["Date", `${fmtDate(view.safariDate)} ${fmtTime(view.timeSlot)}`], ["Guests", String(view.guests)], ["Total", money(view.totalAmount)], ["Payment", `${view.paymentMethod} · ${view.paymentStatus}`], ["Transaction", view.transactionRef ?? "—"], ["Booked", fmtDate(view.createdAt)]].map(([k, v]) => (
                <div key={k}><dt className="text-xs text-slate-500">{k}</dt><dd className="font-medium break-words">{v}</dd></div>
              ))}
            </dl>
            <PaymentSummary booking={view} />
            {view.specialRequests && <p className="mt-4 rounded-lg bg-slate-50 p-3 text-sm"><b>Requests:</b> {view.specialRequests}</p>}
            <button className="a-btn mt-5 w-full justify-center" onClick={() => setView(null)}>Close</button>
          </div>
        </div>
      )}
      <Toast msg={toast.msg} onClose={toast.clear} />
    </>
  );
}

// ================================================================ Payments
export function PaymentsManager({ initial }: { initial: PaymentDetail[] }) {
  const [list, setList] = useState(initial);
  const [filter, setFilter] = useState("");
  const toast = useToast();
  const rows = list.filter((p) => !filter || p.status === filter || p.method === filter);
  const totals = {
    paid: list.filter((p) => p.status === "PAID").reduce((s, p) => s + p.amount, 0),
    pending: list.filter((p) => p.status === "PENDING").reduce((s, p) => s + p.amount, 0),
    refunded: list.filter((p) => p.status === "REFUNDED").reduce((s, p) => s + p.amount, 0),
  };
  const change = async (p: PaymentDetail, status: string) => {
    try {
      await api(`/api/payments/${p.id}`, "PATCH", { status });
      setList(await api<PaymentDetail[]>("/api/payments"));
      toast.show(true, `Payment ${p.transactionRef} marked ${status}`);
    } catch (e) {
      toast.show(false, (e as Error).message);
    }
  };
  return (
    <>
      <PageHeader title="Payments" sub="Record cash collected on arrival. Refunds must be returned to the customer before marking them refunded." action={
        <select className="a-input w-44" value={filter} onChange={(e) => setFilter(e.target.value)}>
          <option value="">All payments</option>
          <option value="PAID">Paid</option><option value="PENDING">Pending</option><option value="REFUNDED">Refunded</option><option value="FAILED">Failed</option>
          <option value="CARD">Card only</option><option value="CASH">Cash only</option>
        </select>
      } />
      <h2 className="mb-3 text-lg font-semibold">Payment summary</h2>
      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        {[["Collected", totals.paid, "text-emerald-700"], ["Awaiting cash", totals.pending, "text-amber-700"], ["Refunded", totals.refunded, "text-violet-700"]].map(([l, v, c]) => (
          <div key={l as string} className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm"><p className="text-xs uppercase text-slate-500">{l}</p><p className={`mt-1 text-2xl font-semibold ${c}`}>{money(v as number)}</p></div>
        ))}
      </div>
      <h2 className="mb-3 text-lg font-semibold">Payment details</h2>
      <Table head={["Transaction", "Booking / pickup", "Customer", "Method", "Amount", "Status", "Payment date", ""]} empty={!rows.length}>
        {rows.map((p) => (
          <tr key={p.id} className="hover:bg-slate-50">
            <td className="px-4 py-3 font-mono text-xs">{p.transactionRef}</td>
            <td className="px-4 py-3 font-mono text-xs">{p.bookingReference}<p className="mt-1 font-sans text-xs text-slate-500">{p.vehicleNumber ?? "Vehicle number pending"}<br />{p.pickupPlace ?? "Pickup not specified"}</p></td>
            <td className="px-4 py-3">{p.customerName}<p className="text-xs text-slate-500">{p.customerEmail}</p><p className="text-xs text-gold-dark">{p.customerRole === "ROLE_ADMIN" ? "Administrator" : "Customer"}</p></td>
            <td className="px-4 py-3">{p.method === "CARD" ? `💳 •••• ${p.cardLast4 ?? ""}` : "💵 Cash"}</td>
            <td className="px-4 py-3 font-medium">{money(p.amount)}</td>
            <td className="px-4 py-3"><Badge s={p.status} /></td>
            <td className="px-4 py-3 text-slate-500">{paymentDate(p.paidAt)}</td>
            <td className="whitespace-nowrap px-4 py-3 text-right">
              {p.status === "PENDING" && <button className="a-btn-light mr-1.5" onClick={() => change(p, "PAID")}>Mark paid</button>}
              {p.status === "PAID" && <button className="a-btn-danger" onClick={() => change(p, "REFUNDED")}>Refund</button>}
              <Link href={`/client/receipts/${p.bookingId}`} className="a-btn-light ml-2">Receipt</Link>
              {p.status === "FAILED" && <button className="a-btn-danger ml-2" onClick={async () => { if (!confirm("Remove this failed payment record?")) return; try { await api(`/api/payments/${p.id}`, "DELETE"); setList(await api<PaymentDetail[]>("/api/payments")); } catch(e) { toast.show(false, (e as Error).message); } }}>Delete failed record</button>}
            </td>
          </tr>
        ))}
      </Table>
      <Toast msg={toast.msg} onClose={toast.clear} />
    </>
  );
}

// ================================================================ Reviews
export type ReviewAdminItem = ReviewRow;

export function ReviewsManager({ initial }: { initial: ReviewAdminItem[] }) {
  const [list, setList] = useState(initial);
  const [tab, setTab] = useState<"all" | "customers" | "admins" | "samples">("all");
  const [query, setQuery] = useState("");
  const [busy, setBusy] = useState(false);
  const toast = useToast();
  const rows = list.filter(review => {
    const category = tab === "all" || (tab === "samples" ? review.sample
      : !review.sample && review.authorRole === (tab === "admins" ? "ROLE_ADMIN" : "ROLE_CUSTOMER"));
    return category && `${review.authorName} ${review.authorEmail ?? ""} ${review.comment}`.toLowerCase().includes(query.toLowerCase());
  });
  const reload = async () => setList(await api<ReviewAdminItem[]>("/api/reviews?all=1"));
  async function remove(id: number) {
    if (busy || !confirm("Delete this review from the website? Its replies will also be removed.")) return;
    setBusy(true);
    try { await api(`/api/reviews/${id}`, "DELETE"); await reload(); toast.show(true, "Review deleted"); }
    catch (error) { toast.show(false, (error as Error).message); }
    finally { setBusy(false); }
  }
  return <>
    <PageHeader title="Reviews" sub={`${list.filter(review => !review.sample).length} published reviews · ${list.filter(review => review.sample).length} samples`} action={
      <div className="flex flex-wrap gap-2">
        <Search value={query} onChange={setQuery} placeholder="Name, email or review…" />
        <div className="flex flex-wrap rounded-lg border border-slate-300 bg-white p-0.5 text-sm">
          {(["all", "customers", "admins", "samples"] as const).map(value => <button key={value} onClick={() => setTab(value)} className={`rounded-md px-3 py-1.5 capitalize ${tab === value ? "bg-jungle-800 text-white" : "text-slate-600"}`}>{value}</button>)}
        </div>
      </div>
    } />
    <p className="mb-5 text-sm text-slate-600">Reviews appear immediately. Administrators can reply or delete a review.</p>
    <div className="grid gap-4 md:grid-cols-2">
      {rows.map(review => <article key={review.id} className="min-w-0 rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0"><p className="break-words font-semibold">{review.authorName}</p>
            <p className="break-all text-xs text-slate-500">{review.authorEmail ?? "Fictional sample"}</p>
            <p className="text-xs text-slate-500">{review.sample ? "Sample" : review.authorRole === "ROLE_ADMIN" ? "Administrator" : "Customer"} · {fmtDate(review.createdAt)}</p>
          </div>
          <span className="shrink-0 text-amber-500">{"★".repeat(review.rating)}</span>
        </div>
        <p className="mt-3 whitespace-pre-wrap break-words text-sm text-slate-600">{review.comment}</p>
        <ReviewReplies reviewId={review.id} replies={review.replies} canReply onChange={reload} />
        <button disabled={busy} className="a-btn-danger mt-4" onClick={() => remove(review.id)}>Delete review</button>
      </article>)}
      {!rows.length && <p className="col-span-full rounded-xl bg-white p-10 text-center text-sm text-slate-400">No reviews here.</p>}
    </div>
    <Toast msg={toast.msg} onClose={toast.clear} />
  </>;
}
