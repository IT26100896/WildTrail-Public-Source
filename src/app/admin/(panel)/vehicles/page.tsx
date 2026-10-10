import { VehiclesManager } from "@/components/admin/managers";
import { backendApi } from "@/lib/backend-api";
import type { VehicleDTO } from "@/types/domain";

export default async function AdminVehiclesPage() {
  return <VehiclesManager initial={await backendApi<VehicleDTO[]>("/api/vehicles?all=1")} />;
}

