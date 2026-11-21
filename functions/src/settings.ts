import type { Firestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";

/**
 * Firestore の `config/aiMaterial` に置く設定。サーバーを出し直さずに変えられる。
 * ドキュメントや項目がなければ、下の初期値を使う。
 */
export interface AiMaterialSettings {
  /** 無料の非常ブレーキ：無料で作れる回数の全体の1日の上限（日本時間） */
  freeDailyLimit: number;
  /** 全体の安全上限：無料と回数券を合わせた1日の上限。当たったら止めて Discord に知らせる */
  totalDailyLimit: number;
  /** 画像を作るモデル */
  imageModel: string;
  /** 文章の審査に使うモデル */
  judgeModel: string;
  /** 頼む文章に入っていたら断る実在の人物・グループ名（公開リポジトリには置かない） */
  blockedNames: string[];
}

export const DEFAULT_SETTINGS: AiMaterialSettings = {
  freeDailyLimit: 30,
  totalDailyLimit: 300,
  imageModel: "gemini-3.1-flash-lite-image",
  judgeModel: "gemini-3.1-flash-lite",
  blockedNames: [],
};

export const SETTINGS_DOC_PATH = "config/aiMaterial";

/** 項目ごとに型を確かめ、誤った値は初期値に戻す（設定の打ち間違いで止まらないように）。 */
export function parseSettings(data: Record<string, unknown> | undefined): AiMaterialSettings {
  const nonNegativeInt = (value: unknown, fallback: number) =>
    Number.isInteger(value) && (value as number) >= 0 ? (value as number) : fallback;
  const nonEmptyString = (value: unknown, fallback: string) =>
    typeof value === "string" && value.trim() !== "" ? value.trim() : fallback;
  const blockedNames = Array.isArray(data?.blockedNames)
    ? data.blockedNames.filter((name): name is string => typeof name === "string" && name.trim() !== "")
    : DEFAULT_SETTINGS.blockedNames;
  return {
    freeDailyLimit: nonNegativeInt(data?.freeDailyLimit, DEFAULT_SETTINGS.freeDailyLimit),
    totalDailyLimit: nonNegativeInt(data?.totalDailyLimit, DEFAULT_SETTINGS.totalDailyLimit),
    imageModel: nonEmptyString(data?.imageModel, DEFAULT_SETTINGS.imageModel),
    judgeModel: nonEmptyString(data?.judgeModel, DEFAULT_SETTINGS.judgeModel),
    blockedNames,
  };
}

/**
 * 設定を読む。インスタンスごとに1分だけ覚えておき、呼ばれるたびに Firestore を読まない。
 * 読めなかったときは前回の値を使う（1分は読み直さない）。一度も読めていなければ例外を投げる。
 * 初期値で動かすと断る名前の一覧が空になるので、入口スイッチと同じく「作らない」側に倒す。
 */
export function createSettingsLoader(db: Firestore, ttlMs = 60_000, now = () => Date.now()) {
  let cached: { settings: AiMaterialSettings; loadedAt: number } | undefined;
  return async (): Promise<AiMaterialSettings> => {
    if (cached && now() - cached.loadedAt < ttlMs) return cached.settings;
    try {
      const snapshot = await db.doc(SETTINGS_DOC_PATH).get();
      cached = { settings: parseSettings(snapshot.data()), loadedAt: now() };
    } catch (error) {
      if (!cached) throw error;
      logger.warn("設定を読めなかった。前回の値を使う", { error: String(error) });
      cached = { settings: cached.settings, loadedAt: now() };
    }
    return cached.settings;
  };
}
