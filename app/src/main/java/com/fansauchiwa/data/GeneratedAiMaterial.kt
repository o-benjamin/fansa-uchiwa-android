package com.fansauchiwa.data

/**
 * サーバーが作った素材。背景は単色なので、切り抜いてから置く。
 * [ticketsRemaining] はこの PR では使わず、購入画面（#348）で残りの回数を出すのに使う。
 * [model] は画像を作ったモデルで、報告するときにサーバーへ返す
 */
data class GeneratedAiMaterial(
    val imageBase64: String,
    val mimeType: String,
    val payment: AiMaterialPayment,
    val ticketsRemaining: Int,
    val model: String
)
