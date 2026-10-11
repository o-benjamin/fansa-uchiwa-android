package com.fansauchiwa.edit.pager

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

/**
 * 文字タブより左右の余白が狭いページで、フォントのボタンを文字タブと同じ列数で並べるための列（#331）。
 *
 * 列数の求め方は文字タブの [GridCells.Adaptive]（列の最小幅 [FONT_BUTTON_MIN_WIDTH]）と同じで、
 * 余白の差を引いた幅（＝文字タブで使える幅）で数える。列の幅は、ページで使える幅を等しく分ける。
 * 「全体」タブ（余白 16dp）で Adaptive をそのまま使うと文字タブより1列多くなり、ボタンの中の「あA!」が折り返していた
 *
 * @param pageHorizontalPadding グリッドを置くページの左右の余白（片側）
 */
internal data class FontButtonGridCells(private val pageHorizontalPadding: Dp) : GridCells {
    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): List<Int> {
        // LazyVerticalGrid は左右の余白をそれぞれ px に丸めるので、差も片側ずつ丸めてから2倍にする（1px のずれで列数が変わらないように）
        val narrowerOnTextPage = (TEXT_PAGE_HORIZONTAL_PADDING.roundToPx() - pageHorizontalPadding.roundToPx()) * 2
        val textPageAvailableSize = availableSize - narrowerOnTextPage
        val count = ((textPageAvailableSize + spacing) / (FONT_BUTTON_MIN_WIDTH.roundToPx() + spacing))
            .coerceAtLeast(1)
        val sizeWithoutSpacing = availableSize - spacing * (count - 1)
        val cellSize = sizeWithoutSpacing / count
        val remainingPixels = sizeWithoutSpacing % count
        return List(count) { index -> cellSize + if (index < remainingPixels) 1 else 0 }
    }
}
