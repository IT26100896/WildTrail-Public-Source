import { backendApi } from "@/lib/backend-api";
import AdminAccountsManager, { AdminAccount } from "@/components/admin/AdminAccountsManager";
export default async function AccountsPage() {
 return <AdminAccountsManager initial={await backendApi<AdminAccount[]>("/api/admin/accounts")} />;
}
