import { cookies } from "next/headers";
import { backendApi } from "./backend-api";
import type { SessionUser } from "@/types/domain";
/** The Java API verifies both the cookie signature and the current account. */
export async function getAdminSession(): Promise<SessionUser | null> {
  if (!(await cookies()).get("wt_admin_session")) return null;
  const user = await backendApi<Partial<SessionUser>>("/api/auth/me?area=admin");
  return user.role === "ROLE_ADMIN" ? user as SessionUser : null;
}
export async function getCustomerSession(): Promise<SessionUser | null> {
  if (!(await cookies()).get("wt_client_session")) return null;
  const user = await backendApi<Partial<SessionUser>>("/api/auth/me?area=client");
  return user.role === "ROLE_CUSTOMER" ? user as SessionUser : null;
}
