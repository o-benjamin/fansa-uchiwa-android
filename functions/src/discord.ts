/** Discord の Webhook に1行送る。 */
export async function postToDiscord(webhookUrl: string, content: string, fetchImpl: typeof fetch = fetch): Promise<void> {
  const response = await fetchImpl(webhookUrl, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ content }),
    signal: AbortSignal.timeout(10_000),
  });
  if (!response.ok) throw new Error(`Discord が ${response.status} を返した`);
}
