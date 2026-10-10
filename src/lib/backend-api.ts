import { headers } from "next/headers";

const backendUrl = (process.env.WILDTRAIL_API_URL ?? "http://127.0.0.1:8080").replace(/\/$/, "");

/** Fetches backend data for server-rendered pages while forwarding the signed session cookie. */
export async function backendApi<T>(path: string): Promise<T> {
  const cookie = (await headers()).get("cookie") ?? "";
  const response = await fetch(`${backendUrl}${path}`, {
    headers: cookie ? { cookie } : undefined,
    cache: "no-store",
  });
  const data: unknown = await response.json().catch(() => ({}));
  if (!response.ok) {
    const message = data && typeof data === "object" && "error" in data
      ? String(data.error)
      : `Backend request failed (${response.status})`;
    throw new Error(message);
  }
  return data as T;
}
