import { redirect } from "next/navigation";
import { getAdminSession } from "@/lib/session";

export default async function AdminIndex() {
  redirect((await getAdminSession()) ? "/admin/dashboard" : "/client/login");
}

