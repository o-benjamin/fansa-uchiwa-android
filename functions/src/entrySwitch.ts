import { logger } from "firebase-functions";
import type { RemoteConfigParameterValue, RemoteConfigTemplate } from "firebase-admin/remote-config";

/** アプリと同じ Remote Config のパラメータ。アプリの入口もこの値で出し分ける。 */
export const ENTRY_SWITCH_PARAMETER = "ai_material_enabled";

function explicitValue(value: RemoteConfigParameterValue | undefined): string | undefined {
  return value && "value" in value ? value.value : undefined;
}

/**
 * 入口スイッチが開いているか。初期値か、どれか1つの条件の値が `true` なら開いているとみなす。
 * サーバーは端末の条件（ユーザーの属性など）を評価できないので、ここは「すべて false にすれば
 * 古いアプリからの呼び出しも止まる」緊急停止として使う。パラメータがなければ閉じている。
 */
export function isEntryOpen(template: RemoteConfigTemplate): boolean {
  const parameter = template.parameters[ENTRY_SWITCH_PARAMETER];
  if (!parameter) return false;
  const values = [parameter.defaultValue, ...Object.values(parameter.conditionalValues ?? {})];
  return values.some((value) => explicitValue(value) === "true");
}

/**
 * Remote Config のテンプレートを1分だけ覚えておく読み手を作る。
 * 読めなかったときは前回の値を使い、一度も読めていなければ閉じているとみなす（費用が出ない側に倒す）。
 */
export function createEntrySwitch(
  loadTemplate: () => Promise<RemoteConfigTemplate>,
  ttlMs = 60_000,
  now = () => Date.now(),
) {
  let cached: { open: boolean; loadedAt: number } | undefined;
  return async (): Promise<boolean> => {
    if (cached && now() - cached.loadedAt < ttlMs) return cached.open;
    try {
      cached = { open: isEntryOpen(await loadTemplate()), loadedAt: now() };
    } catch (error) {
      logger.warn("Remote Config を読めなかった。前回の値を使う", { error: String(error) });
      return cached?.open ?? false;
    }
    return cached.open;
  };
}
