package com.fansauchiwa.data

/**
 * 今回の生成で使ったもの（無料の1回か、回数券か）。計測の `save_ai_material` の `payment` に送る
 */
enum class AiMaterialPayment(val serverValue: String) {
    FREE("free"),
    TICKET("ticket");

    companion object {
        fun fromServerValue(value: String?): AiMaterialPayment? =
            entries.firstOrNull { it.serverValue == value }
    }
}
