import { PageHero } from "@/components/client/ClientShell";
import { Gallery } from "@/components/client/interactive";
import { GALLERY, IMG } from "@/lib/content";

export const metadata = { title: "Gallery — WildTrail" };

export default function GalleryPage() {
  return (
    <>
      <PageHero eyebrow="Gallery" title="Through the lens" text="Leopards, elephants, jeeps at dawn and sunsets over the plains. Click any image to view it full-screen." image={IMG.sunsetBirds} />
      <section className="mx-auto max-w-7xl px-5 py-20 lg:px-8">
        <Gallery items={GALLERY} />
      </section>
    </>
  );
}

