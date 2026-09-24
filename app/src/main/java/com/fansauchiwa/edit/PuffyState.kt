package com.fansauchiwa.edit

import com.fansauchiwa.data.Decoration

/**
 * うちわ全体のぷくぷくの状態（#268）
 *
 * ぷくぷくは編集画面のトグル1つでうちわ全体を切り替えるが、保存データは今までどおり
 * 文字・ステッカーごとのフラグ（[Decoration.Text.isPuffyEnabled]・[Decoration.Sticker.isPukupuku]）と
 * フチのフラグ（isOverallBorderPuffyEnabled）で持つ。前のバージョンで部品ごとに切り替えて保存した
 * うちわの見た目を変えないため、フラグをそろえ直さず、ここで全体の状態として読み取る。
 */
enum class PuffyState {
    /** 文字・ステッカー・フチがすべてぷくぷく。トグルはオン表示 */
    ON,

    /** ぷくぷくが1つもない */
    OFF,

    /** 一部だけぷくぷく（前のバージョンで部品ごとに切り替えたうちわ）。トグルはオフ表示 */
    MIXED;

    companion object {
        /**
         * @param decorations うちわの装飾。画像はぷくぷくにならないので数えない
         * @param isOverallBorderPuffyEnabled フチがぷくぷくかどうか
         */
        fun of(decorations: List<Decoration>, isOverallBorderPuffyEnabled: Boolean): PuffyState {
            val flags = decorations.mapNotNull { it.puffyFlagOrNull() } + isOverallBorderPuffyEnabled
            return when {
                flags.all { it } -> ON
                flags.none { it } -> OFF
                else -> MIXED
            }
        }
    }
}

/** 文字・ステッカーならぷくぷくのフラグ、ぷくぷくにできない装飾（画像）なら null */
private fun Decoration.puffyFlagOrNull(): Boolean? = when (this) {
    is Decoration.Text -> isPuffyEnabled
    is Decoration.Sticker -> isPukupuku
    is Decoration.Image -> null
}

/** 文字・ステッカーのぷくぷくを [isEnabled] にそろえた装飾を返す。画像はそのまま */
fun Decoration.withPuffy(isEnabled: Boolean): Decoration = when (this) {
    is Decoration.Text -> copy(isPuffyEnabled = isEnabled)
    is Decoration.Sticker -> copy(isPukupuku = isEnabled)
    is Decoration.Image -> this
}
