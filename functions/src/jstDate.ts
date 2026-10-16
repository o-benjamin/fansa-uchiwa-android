const JST_OFFSET_MS = 9 * 60 * 60 * 1000;

/** 日本時間の日付（`2026-10-16` の形）。1日の回数を数える `dailyUsage/{日付}` の ID に使う。 */
export function jstDateKey(now: Date): string {
  return new Date(now.getTime() + JST_OFFSET_MS).toISOString().slice(0, 10);
}
