const JST_OFFSET_MS = 9 * 60 * 60 * 1000;

/** 日本時間の日付（`2026-10-16` の形）。1日の回数を数える `dailyUsage/{日付}` の ID に使う。 */
export function jstDateKey(now: Date): string {
  return new Date(now.getTime() + JST_OFFSET_MS).toISOString().slice(0, 10);
}

/**
 * 日本時間の暦で `months` か月後の同じ時刻。その月に同じ日がなければ月末にする（8月31日の6か月後は2月28日）。
 * 回数券の期限（購入から6か月）に使う。月末に寄せるのは、期限を6か月より長くしないため（資金決済法の対象外にする条件）。
 */
export function addMonthsJst(date: Date, months: number): Date {
  const jst = new Date(date.getTime() + JST_OFFSET_MS);
  const day = jst.getUTCDate();
  jst.setUTCDate(1);
  jst.setUTCMonth(jst.getUTCMonth() + months);
  const lastDayOfMonth = new Date(Date.UTC(jst.getUTCFullYear(), jst.getUTCMonth() + 1, 0)).getUTCDate();
  jst.setUTCDate(Math.min(day, lastDayOfMonth));
  return new Date(jst.getTime() - JST_OFFSET_MS);
}
