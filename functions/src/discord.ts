/** Discord の Webhook に1行送る。URL が空なら何もしない（エミュレーターや、Secret を登録する前）。 */
export async function postToDiscord(webhookUrl: string, content: string, fetchImpl: typeof fetch = fetch): Promise<void> {
  if (!webhookUrl) return;
  const response = await fetchImpl(webhookUrl, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ content }),
    signal: AbortSignal.timeout(10_000),
  });
  if (!response.ok) throw new Error(`Discord が ${response.status} を返した`);
}
