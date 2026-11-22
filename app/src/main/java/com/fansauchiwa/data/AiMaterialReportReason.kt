package com.fansauchiwa.data

/**
 * 作った素材を報告する理由。[serverValue] はサーバーの `REPORT_REASONS`（`functions/src/reportMaterial.ts`）と同じ値で、
 * GA4 の `report_ai_material` の `reason` にもそのまま送る
 */
enum class AiMaterialReportReason(val serverValue: String) {
    INAPPROPRIATE("inappropriate"),
    REAL_PERSON("real_person"),
    RIGHTS("rights"),
    OTHER("other")
}
