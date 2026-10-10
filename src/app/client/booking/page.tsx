import BookingWizard from "@/components/client/BookingWizard";
import { backendApi } from "@/lib/backend-api";
import type { PackageDTO } from "@/types/domain";
import type { VehicleDTO } from "@/types/domain";
import type { DestinationDTO } from "@/types/domain";
import { getCustomerSession } from "@/lib/session";
import { redirect } from "next/navigation";

export const metadata = { title: "Book a Safari — WildTrail" };

export default async function BookingPage({ searchParams }: { searchParams: Promise<Record<string, string | undefined>> }) {
  const sp = await searchParams;
  const session = await getCustomerSession();
  if (!session) {
    const query = new URLSearchParams(Object.entries(sp).filter((entry): entry is [string, string] => typeof entry[1] === "string")).toString();
    redirect(`/client/login?next=${encodeURIComponent(`/client/booking${query ? `?${query}` : ""}`)}`);
  }
  const [destinations, packages, vehicles, me] = await Promise.all([
    backendApi<DestinationDTO[]>("/api/destinations"),
    backendApi<PackageDTO[]>("/api/packages"),
    backendApi<VehicleDTO[]>("/api/vehicles"),
    getCustomerSession(),
  ]);
  return (
    <div className="bg-ivory pt-28 pb-24">
      <div className="mx-auto max-w-7xl px-5 lg:px-8">
        <div className="mb-10 text-center">
          <p className="eyebrow">Safari booking</p>
          <h1 className="mt-2 font-display text-4xl font-semibold text-jungle-950 md:text-5xl">Plan your adventure</h1>
        </div>
        <BookingWizard
          destinations={destinations}
          packages={packages}
          vehicles={vehicles}
          user={me ? { name: me.name, email: me.email } : null}
          initial={{
            destination: Number(sp.destination) || 0,
            package: Number(sp.package) || 0,
            date: sp.date ?? "",
            type: sp.type ?? "",
            guests: Number(sp.guests) || 2,
          }}
        />
      </div>
    </div>
  );
}

