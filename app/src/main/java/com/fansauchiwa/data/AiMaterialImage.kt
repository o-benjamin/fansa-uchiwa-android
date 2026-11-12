package com.fansauchiwa.data

import android.net.Uri

/**
 * 端末に置いた生成結果。[uri] は切り抜き後の画像で、切り抜けなかったときは作ったままの画像
 * （[backgroundRemoved] が false）。
 * [ticketsRemaining] は回数券の残りで、AI 素材の画面に出す。
 * [model] は画像を作ったモデルで、報告するときにサーバーへ返す
 */
data class AiMaterialImage(
    val uri: Uri,
    val backgroundRemoved: Boolean,
    val payment: AiMaterialPayment,
    val ticketsRemaining: Int,
    val model: String
)
