import { PaymentsManager } from "@/components/admin/managers";
import { backendApi } from "@/lib/backend-api";
import type { PaymentDetail } from "@/types/domain";

export default async function AdminPaymentsPage() {
  return <PaymentsManager initial={await backendApi<PaymentDetail[]>("/api/payments")} />;
}

