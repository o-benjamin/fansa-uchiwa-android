package com.fansauchiwa.data

/**
 * テンプレートの位置と縁の太さ（px）を決めた端末の密度（Pixel_10）。
 * [templateList] と debug ビルドの候補は、この密度の px で持つ。
 */
const val TEMPLATE_BASE_DENSITY = 2.625f

/**
 * テンプレートを、密度 [density] の端末でも基準の密度（[TEMPLATE_BASE_DENSITY]）と同じ組み方に見えるよう直す（#381）。
 *
 * 文字とステッカーの大きさは dp なので密度に比例して大きくなるが、位置（`translationX` / `translationY`）・
 * 文字の縁の太さ・全体の縁の太さは px のまま描かれる。直さないと、密度が高い端末では文字とステッカーが重なり、
 * 低い端末ではうちわの外へ広がる。そこで px の値も密度の比で伸ばす。
 * ステッカーの縁（`strokeWidth`・`secondStrokeWidth`）はベクター画像の viewport（24×24）の座標で描かれ、
 * ステッカーと一緒に伸びるので変えない。
 *
 * 伸ばした縁の太さは、編集画面のスライダーの目盛り（文字の縁は 5 刻み、全体の縁は 1 刻み）から外れることがある。
 * つまみは近い目盛りに寄って見え、触ると目盛りの値になる（差は1目盛り未満）。丸めると密度の比からずれるので丸めない。
 *
 * 開いたあとの飾りは端末の px として保存される。保存済みのうちわには使わないこと（その端末で作ったときの見た目が変わる）。
 */
fun Template.scaledToDensity(density: Float): Template {
    val ratio = density / TEMPLATE_BASE_DENSITY
    return copy(
        savedUchiwa = savedUchiwa.copy(
            decorations = savedUchiwa.decorations.map { it.scaledPxBy(ratio) },
            overallBorderWidth = savedUchiwa.overallBorderWidth * ratio
        )
    )
}

private fun Decoration.scaledPxBy(ratio: Float): Decoration = when (this) {
    is Decoration.Text -> copy(
        offset = offset * ratio,
        strokeWidth = strokeWidth * ratio,
        secondBorderWidth = secondBorderWidth * ratio
    )

    // ステッカーの縁は viewport（24×24）の座標で描かれ、ステッカーと一緒に伸びる
    is Decoration.Sticker -> copy(offset = offset * ratio)
    // 写真の strokeWidth は描画に使っていない
    is Decoration.Image -> copy(offset = offset * ratio)
}
