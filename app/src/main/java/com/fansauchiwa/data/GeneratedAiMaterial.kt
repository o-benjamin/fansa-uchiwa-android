package com.fansauchiwa.data

/**
 * サーバーが作った素材。背景は単色なので、切り抜いてから置く。
 * [ticketsRemaining] はこの PR では使わず、購入画面（#348）で残りの回数を出すのに使う
 */
data class GeneratedAiMaterial(
    val imageBase64: String,
    val mimeType: String,
    val payment: AiMaterialPayment,
    val ticketsRemaining: Int
)
