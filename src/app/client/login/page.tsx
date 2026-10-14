import { redirect } from "next/navigation";
import { AuthPanel } from "@/components/client/AuthPanel";
import { AuthForm } from "@/components/client/forms";
import { getWebsiteSession } from "@/lib/session";

export const metadata = { title: "Sign in — WildTrail" };

export default async function LoginPage() {
  if (await getWebsiteSession()) redirect("/client/home");
  return (
    <AuthPanel title="Sign in to WildTrail" subtitle="Access your bookings or staff dashboard.">
      <AuthForm mode="login" />
    </AuthPanel>
  );
}

