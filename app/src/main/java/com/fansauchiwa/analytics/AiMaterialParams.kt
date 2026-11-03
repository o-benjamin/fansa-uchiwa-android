package com.fansauchiwa.analytics

/**
 * AI 素材のイベント（`generate_ai_material`・`save_ai_material`）のパラメータ
 */
object AiMaterialParams {
    const val PARAM_RESULT = "result"
    const val PARAM_REASON = "reason"
    const val PARAM_PAYMENT = "payment"

    // 成功したときだけ付ける。切り抜けたかを "true" / "false" の文字列で送る
    const val PARAM_BACKGROUND_REMOVED = "background_removed"

    const val RESULT_SUCCESS = "success"
    const val RESULT_REJECTED = "rejected"
    const val RESULT_FAILED = "failed"
}
