import { randomInt, randomUUID } from "node:crypto";
import { getApps, initializeApp } from "firebase-admin/app";
import { getFirestore, Timestamp } from "firebase-admin/firestore";
import type { Firestore } from "firebase-admin/firestore";

/**
 * Firestore エミュレーターにつないだ Firestore。`npm test`（firebase emulators:exec）の中で動かす。
 * テストのファイルは並んで走るので、データは消さずに、利用者の ID と日付をテストごとに変えて分ける。
 */
export function emulatorDb(): Firestore {
  if (!process.env.FIRESTORE_EMULATOR_HOST) {
    throw new Error("Firestore エミュレーターの中で動かす（npm test）");
  }
  if (getApps().length === 0) initializeApp({ projectId: "demo-fansa-uchiwa" });
  return getFirestore();
}

export function uniqueUid(): string {
  return `test-${randomUUID()}`;
}

const DAY_MS = 24 * 60 * 60 * 1000;

/** ほかのテストと重ならない日（日本時間の正午）。全体の回数 `spend/{日付}` を分けるため。 */
export function uniqueDay(): Date {
  return new Date(Date.UTC(2030, 0, 1, 3) + randomInt(0, 1_000_000) * DAY_MS);
}

export function daysLater(base: Date, days: number): Timestamp {
  return Timestamp.fromMillis(base.getTime() + days * DAY_MS);
}

export const LIMITS = { freeDailyLimit: 30, totalDailyLimit: 300 };
