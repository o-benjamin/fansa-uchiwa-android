package com.fansauchiwa.analytics

/**
 * AI 素材のイベント（`generate_ai_material`・`save_ai_material`・`report_ai_material`・
 * `view_ai_material_paywall`・`purchase_ai_material`）のパラメータ。
 * `report_ai_material` は [PARAM_REASON] に報告の理由、[PARAM_RESULT] に送れたか
 * （[RESULT_SUCCESS] / 1人1日の上限で断られた [RESULT_REJECTED] / [RESULT_FAILED]）を送る。
 * `view_ai_material_paywall` は購入の案内に値段を出したか（[RESULT_SUCCESS]）、値段を取れず「今は買えない」と出したか
 * （[RESULT_FAILED]）が決まったときに送り、[PARAM_REASON] に案内を開いた生成の断りの理由、[PARAM_RESULT] にそのどちらかを送る。
 * 足せていない購入を足して案内を閉じたときは送らない。
 * `purchase_ai_material` は [PARAM_RESULT] に [RESULT_SUCCESS] / [RESULT_PENDING] / [RESULT_CANCELLED] /
 * サーバーが断った [RESULT_REJECTED]（[PARAM_REASON] に理由）/ [RESULT_FAILED] を送る。
 * 前の購入が足し済みのまま残っていて買えなかったときも [RESULT_FAILED]。
 * あとで足した購入（コンビニ払いが済んだ・足す前に通信が切れた）は、足したときに [RESULT_SUCCESS] だけを送る
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
    const val RESULT_PENDING = "pending"
    const val RESULT_CANCELLED = "cancelled"
}
