import { redirect } from "next/navigation";
import type { ReactNode } from "react";
import AdminSidebar from "@/components/admin/AdminSidebar";
import { getAdminSession } from "@/lib/session";

/**
 * Server-side authorization guard for every /admin/* page (except /admin/login).
 * Unauthenticated users — including logged-in customers — are redirected.
 */
export default async function AdminPanelLayout({ children }: { children: ReactNode }) {
  const admin = await getAdminSession();
  if (!admin) redirect("/client/login");
  return (
    <div className="lg:pl-64">
      <AdminSidebar name={admin.name} email={admin.email} />
      <main className="p-5 pt-20 lg:p-8">{children}</main>
    </div>
  );
}

