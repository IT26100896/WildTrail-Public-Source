import { redirect } from "next/navigation";
import { AuthPanel } from "@/components/client/AuthPanel";
import { AuthForm } from "@/components/client/forms";
import { getCustomerSession } from "@/lib/session";

export const metadata = { title: "Create account — WildTrail" };

export default async function RegisterPage() {
  if (await getCustomerSession()) redirect("/client/my-bookings");
  return (
    <AuthPanel title="Join the trail" subtitle="Create an account to track bookings and share reviews.">
      <AuthForm mode="register" />
    </AuthPanel>
  );
}

