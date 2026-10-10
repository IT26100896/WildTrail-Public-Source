import { PackagesManager } from "@/components/admin/managers";
import { backendApi } from "@/lib/backend-api";
import type { PackageDTO } from "@/types/domain";
import type { DestinationDTO } from "@/types/domain";

export default async function AdminPackagesPage() {
  const [packages, destinations] = await Promise.all([
    backendApi<PackageDTO[]>("/api/packages?all=1"),
    backendApi<DestinationDTO[]>("/api/destinations"),
  ]);
  return <PackagesManager initial={packages} destinations={destinations.map((d) => ({ id: d.id, name: d.name }))} />;
}

