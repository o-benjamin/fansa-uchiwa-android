package com.fansauchiwa.data

/**
 * 1人1日の報告の上限（サーバーの `too_many_reports`。`functions/src/reportMaterial.ts` の `MAX_REPORTS_PER_DAY`）に当たった。
 * 通信の失敗と違い、押し直しても送れないので、画面では別の文言を出して Crashlytics にも残さない
 */
class AiMaterialReportLimitException : Exception("too_many_reports")
