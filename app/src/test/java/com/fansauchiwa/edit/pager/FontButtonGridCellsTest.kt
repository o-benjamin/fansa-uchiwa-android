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
        // 画面の幅 411dp から、「全体」タブの左右の余白を引いた幅
        val availableSize = with(density) { (411.dp - UCHIWA_BACKGROUND_PAGE_HORIZONTAL_PADDING * 2).roundToPx() }

        val sizes = FontButtonGridCells(UCHIWA_BACKGROUND_PAGE_HORIZONTAL_PADDING).cellSizes(availableSize)

        assertEquals(3, sizes.size)
        assertEquals(availableSize, sizes.sum() + spacing * (sizes.size - 1))
    }

    @Test
    fun uchiwaBackgroundPage_anyScreenWidthAndDensity_sameColumnCountAsTextPage() {
        // 16dp・32dp が px にちょうどならない密度（tvdpi の 1.33 など）も含める
        for (densityValue in listOf(1.33f, 2f, 2.625f, 2.75f, 3f, 3.5f)) {
            val density = Density(density = densityValue)
            val spacing = with(density) { 8.dp.roundToPx() }
            for (screenWidthPx in 600..3000) {
                val textPageAvailableSize = screenWidthPx - with(density) { TEXT_PAGE_HORIZONTAL_PADDING.roundToPx() } * 2
                val pageAvailableSize =
                    screenWidthPx - with(density) { UCHIWA_BACKGROUND_PAGE_HORIZONTAL_PADDING.roundToPx() } * 2

                val textPageColumns = with(GridCells.Adaptive(minSize = FONT_BUTTON_MIN_WIDTH)) {
                    density.calculateCrossAxisCellSizes(textPageAvailableSize, spacing)
                }.size
                val pageColumns = with(FontButtonGridCells(UCHIWA_BACKGROUND_PAGE_HORIZONTAL_PADDING)) {
                    density.calculateCrossAxisCellSizes(pageAvailableSize, spacing)
                }.size

                assertEquals("density=$densityValue screenWidthPx=$screenWidthPx", textPageColumns, pageColumns)
            }
        }
    }

    @Test
    fun samePaddingAsTextPage_sameSizesAsAdaptive() {
        val availableSize = with(density) { (411.dp - TEXT_PAGE_HORIZONTAL_PADDING * 2).roundToPx() }

        assertEquals(
            GridCells.Adaptive(minSize = FONT_BUTTON_MIN_WIDTH).cellSizes(availableSize),
            FontButtonGridCells(TEXT_PAGE_HORIZONTAL_PADDING).cellSizes(availableSize)
        )
    }
}
