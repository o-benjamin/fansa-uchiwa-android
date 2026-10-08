package com.fansauchiwa.edit

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.Decoration

/**
 * 「全体」タブの「すべての文字」の欄に出す、うちわの中の文字すべての見た目（#308）
 *
 * 項目ごとに、すべての文字で値がそろっていればその値、文字ごとにちがえば null。
 * null の項目は、欄で何も選ばれていない表示にする。
 * 縁は1つめ（枠線）だけを扱う。2つめの縁（secondBorder）は文字ごとに変える。
 */
@Immutable
data class AllTextStyle(
    val font: FontFamilies?,
    val color: Color?,
    val strokeColor: Color?,
    val strokeWidth: Float?
) {
    companion object {
        /** うちわに文字が1つもなければ null（欄を出さない） */
        fun of(decorations: List<Decoration>): AllTextStyle? {
            val texts = decorations.filterIsInstance<Decoration.Text>()
            if (texts.isEmpty()) return null
            return AllTextStyle(
                font = texts.commonValueOrNull { it.font },
                color = texts.commonValueOrNull { it.color },
                strokeColor = texts.commonValueOrNull { it.strokeColor },
                strokeWidth = texts.commonValueOrNull { it.strokeWidth }
            )
        }
    }
}

/** すべての文字で [selector] の値が同じならその値、ちがえば null */
private fun <T> List<Decoration.Text>.commonValueOrNull(selector: (Decoration.Text) -> T): T? =
    map(selector).distinct().singleOrNull()
