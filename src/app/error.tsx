"use client";
export default function ErrorPage({ reset }: { reset: () => void }) {
  return <main className="mx-auto max-w-xl px-6 py-36 text-center"><h1 className="text-3xl font-semibold">Service temporarily unavailable</h1><p className="my-5">We could not load the requested information. Please try again shortly.</p><button className="btn-dark" onClick={reset}>Try again</button></main>;
}

