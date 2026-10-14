import { redirect } from "next/navigation";
import { getWebsiteSession } from "@/lib/session";
import { backendApi } from "@/lib/backend-api";
import ProfileForm, { type Profile } from "@/components/client/ProfileForm";
export default async function ProfilePage() {
  if (!await getWebsiteSession()) redirect("/client/login?next=/client/profile");
  return <main className="mx-auto max-w-xl px-5 py-32"><h1 className="mb-8 font-display text-3xl">My profile</h1><ProfileForm initial={await backendApi<Profile>("/api/profile")} /></main>;
}

