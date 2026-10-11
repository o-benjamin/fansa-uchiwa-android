package com.fansauchiwa.edit.pager

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class FontButtonGridCellsTest {

    // Pixel 8a と同じ密度
    private val density = Density(density = 2.625f)
    private val spacing = with(density) { 8.dp.roundToPx() }

    private fun GridCells.cellSizes(availableSize: Int): List<Int> =
        with(this) { density.calculateCrossAxisCellSizes(availableSize, spacing) }

    @Test
    fun uchiwaBackgroundPage_pixel8aWidth_threeColumnsLikeTextPage() {
        // 画面の幅 411dp から、「全体」タブの左右の余白 16dp を引いた幅
        val availableSize = with(density) { (411.dp - 32.dp).roundToPx() }

        val sizes = FontButtonGridCells(pageHorizontalPadding = 16.dp).cellSizes(availableSize)

        assertEquals(3, sizes.size)
        assertEquals(availableSize, sizes.sum() + spacing * (sizes.size - 1))
    }

    @Test
    fun uchiwaBackgroundPage_anyScreenWidth_sameColumnCountAsTextPage() {
        for (screenWidthDp in 280..1280 step 7) {
            val screenWidth = with(density) { screenWidthDp.dp.roundToPx() }
            val textPageAvailableSize = screenWidth - with(density) { (TEXT_PAGE_HORIZONTAL_PADDING * 2).roundToPx() }
            val uchiwaBackgroundAvailableSize = screenWidth - with(density) { 32.dp.roundToPx() }

            val textPageColumns = GridCells.Adaptive(minSize = FONT_BUTTON_MIN_WIDTH)
                .cellSizes(textPageAvailableSize).size
            val uchiwaBackgroundColumns = FontButtonGridCells(pageHorizontalPadding = 16.dp)
                .cellSizes(uchiwaBackgroundAvailableSize).size

            assertEquals("screenWidthDp=$screenWidthDp", textPageColumns, uchiwaBackgroundColumns)
        }
    }

    @Test
    fun samePaddingAsTextPage_sameSizesAsAdaptive() {
        val availableSize = with(density) { (411.dp - TEXT_PAGE_HORIZONTAL_PADDING * 2).roundToPx() }

        assertEquals(
            GridCells.Adaptive(minSize = FONT_BUTTON_MIN_WIDTH).cellSizes(availableSize),
            FontButtonGridCells(pageHorizontalPadding = TEXT_PAGE_HORIZONTAL_PADDING).cellSizes(availableSize)
        )
    }
}
