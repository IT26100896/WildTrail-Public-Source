import { VehiclesManager } from "@/components/admin/managers";
import { backendApi } from "@/lib/backend-api";
import type { AdminVehicleDTO } from "@/types/domain";

export default async function AdminVehiclesPage() {
  return <VehiclesManager initial={await backendApi<AdminVehicleDTO[]>("/api/vehicles?all=1")} />;
}

