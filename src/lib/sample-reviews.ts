import type { ReviewItem } from "@/components/client/interactive";

// Fictional campus examples. Never inserted into customer review records.
export const SAMPLE_REVIEWS: ReviewItem[] = [
  { id: -1, authorName: "Sample Traveller A", country: "Sample review", rating: 5, comment: "The early morning safari was peaceful, and the guide helped us spot wildlife along the trail.", createdAt: "2026-09-01T00:00:00Z", sample: true },
  { id: -2, authorName: "Sample Traveller B", country: "Sample review", rating: 4, comment: "Choosing our park, jeep and time slot was straightforward. The family enjoyed the trip.", createdAt: "2026-09-02T00:00:00Z", sample: true },
  { id: -3, authorName: "Sample Traveller C", country: "Sample review", rating: 5, comment: "A lovely day exploring the national park, with plenty of time to observe the animals.", createdAt: "2026-09-03T00:00:00Z", sample: true },
];
