import { CustomersManager } from "@/components/admin/managers";
import { backendApi } from "@/lib/backend-api";

export default async function AdminCustomersPage() {
  const list = await backendApi<{
    id: number;
    fullName: string;
    email: string;
    phone: string | null;
    country: string | null;
    userId: number | null;
    createdAt: string;
    bookingCount: number;
    totalSpent: number;
  }[]>("/api/customers");
  return <CustomersManager initial={list} />;
}

