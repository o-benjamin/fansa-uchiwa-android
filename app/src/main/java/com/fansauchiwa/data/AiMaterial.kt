package com.fansauchiwa.data

import android.net.Uri

/**
 * サーバーに送る元画像（#338）。[base64Data] は 4MB まで（`functions/src/generateMaterial.ts`）
 */
data class AiMaterialSourceImage(
    val mimeType: String,
    val base64Data: String
)

/**
 * 今回の生成で使ったもの。計測の `save_ai_material` と、購入画面へつなぐかの判断に使う
 */
enum class AiMaterialPayment(val serverValue: String) {
    FREE("free"),
    TICKET("ticket");

    companion object {
        fun fromServerValue(value: String?): AiMaterialPayment? =
            entries.firstOrNull { it.serverValue == value }
    }
}

/**
 * サーバーが作った素材。背景は単色なので、切り抜いてから置く
 */
data class GeneratedAiMaterial(
    val imageBase64: String,
    val mimeType: String,
    val payment: AiMaterialPayment,
    val ticketsRemaining: Int
)

/**
 * 端末に置いた生成結果。[uri] は切り抜き後の画像で、切り抜けなかったときは作ったままの画像
 * （[backgroundRemoved] が false）
 */
data class AiMaterialImage(
    val uri: Uri,
    val backgroundRemoved: Boolean,
    val payment: AiMaterialPayment,
    val ticketsRemaining: Int
)

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

/**
 * 理由つきで断ったときの例外。理由のない失敗（通信できない・時間切れなど）はこの例外にしない
 */
class AiMaterialRejectedException(val reason: AiMaterialRejectReason) :
    Exception("AI素材の生成を断られた: ${reason.serverValue}")
