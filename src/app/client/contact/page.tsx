import { PageHero } from "@/components/client/ClientShell";
import { ContactForm } from "@/components/client/forms";
import { FaqSection } from "@/components/client/sections";
import { getWebsiteSession } from "@/lib/session";
import { IMG } from "@/lib/content";

export const metadata = { title: "Contact — WildTrail" };

export default async function ContactPage() {
  const user = await getWebsiteSession();
  return (
    <>
      <PageHero eyebrow="Contact" title="Let's plan your safari" text="Questions about parks, timings or private tours? Our team answers every message personally." image={IMG.lagoon} />
      <section className="mx-auto grid max-w-7xl gap-10 px-5 py-20 lg:grid-cols-[2fr_3fr] lg:px-8">
        <div className="space-y-4">
          <div className="reveal card p-8"><h2 className="font-display text-2xl">How can we help?</h2><p className="mt-4 text-charcoal/70">Ask about safari packages, departure times, vehicle availability or an existing reservation.</p><p className="mt-4 text-charcoal/70">Include your booking reference if your message concerns a reservation. Our team will use the email you provide to contact you.</p></div>
        </div>
        <div className="reveal card p-8 md:p-10">
          <p className="eyebrow">Send a message</p>
          <h2 className="mt-2 mb-6 font-display text-3xl font-semibold text-jungle-950">Contact our safari team</h2>
          <ContactForm user={user ? { name: user.name, email: user.email } : undefined} />
        </div>
      </section>
      <div className="bg-cream/50"><FaqSection /></div>
    </>
  );
}

