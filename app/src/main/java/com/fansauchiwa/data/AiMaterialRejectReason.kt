package com.fansauchiwa.data

/**
 * 断った理由。[FACE_IN_SOURCE] は送る前にアプリが断ったもので、それ以外はサーバーの理由
 * （`functions/src/generateMaterial.ts` の `RejectReason`）。
 * [serverValue] は GA4 の `generate_ai_material` の「断った理由」にもそのまま送る
 */
enum class AiMaterialRejectReason(val serverValue: String) {
    UNAUTHENTICATED("unauthenticated"),
    INVALID_PROMPT("invalid_prompt"),
    INVALID_IMAGE("invalid_image"),
    DISABLED("disabled"),
    BLOCKED_NAME("blocked_name"),
    MODERATION("moderation"),
    NO_CREDITS("no_credits"),
    FREE_DAILY_LIMIT("free_daily_limit"),
    TOTAL_DAILY_LIMIT("total_daily_limit"),
    SAFETY_BLOCKED("safety_blocked"),
    GENERATION_FAILED("generation_failed"),
    FACE_IN_SOURCE("face_in_source");

    companion object {
        fun fromServerValue(value: String?): AiMaterialRejectReason? =
            entries.firstOrNull { it.serverValue == value }
    }
}
