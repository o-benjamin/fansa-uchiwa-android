package com.fansauchiwa.edit.pager

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

/**
 * 文字タブより左右の余白が狭いページで、フォントのボタンを文字タブと同じ列数で並べるための列（#331）。
 *
 * 列数は、余白の差を引いた幅（＝文字タブで使える幅）に対する [GridCells.Adaptive] の列数。
 * 列の幅は、ページで使える幅を [GridCells.Fixed] で等しく分ける。
 * [UCHIWA_BACKGROUND_PAGE_HORIZONTAL_PADDING] のページで Adaptive をそのまま使うと文字タブより1列多くなり、ボタンの中の「あA!」が折り返していた。
 * data class にして、再コンポーズで作り直しても列の計算をやり直さないようにする（[GridCells.Adaptive] と同じく equals で比べられる）
 *
 * @param pageHorizontalPadding グリッドを置くページの左右の余白（片側）
 */
internal data class FontButtonGridCells(private val pageHorizontalPadding: Dp) : GridCells {
    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): List<Int> {
        // LazyVerticalGrid は左右の余白をそれぞれ px に丸めるので、差も片側ずつ丸めてから2倍にする（1px のずれで列数が変わらないように）
        val paddingDifference = (TEXT_PAGE_HORIZONTAL_PADDING.roundToPx() - pageHorizontalPadding.roundToPx()) * 2
        val textPageColumnCount = with(GridCells.Adaptive(minSize = FONT_BUTTON_MIN_WIDTH)) {
            calculateCrossAxisCellSizes(availableSize - paddingDifference, spacing)
        }.size
        return with(GridCells.Fixed(textPageColumnCount)) {
            calculateCrossAxisCellSizes(availableSize, spacing)
        }
    }
}
