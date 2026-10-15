package com.fansauchiwa.edit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FontFamiliesTest {

    @Test
    fun selectableFontFamilies_retiredFont_isExcluded() {
        assertFalse(selectableFontFamilies.contains(FontFamilies.WDXL_LUBRIFONT_JP_N))
        assertTrue(selectableFontFamilies.none { it.isRetired })
    }

    @Test
    fun selectableFontFamilies_activeFonts_keepDeclarationOrder() {
        assertEquals(FontFamilies.entries.filterNot { it.isRetired }, selectableFontFamilies)
    }

    // 選択画面に出さないフォントが途中にあると、その後ろのフォントの順位（バッジ・final_font_rank_bucket）が1つずれる
    @Test
    fun fontFamilies_retiredFonts_areDeclaredAfterAllActiveFonts() {
        val firstRetiredIndex = FontFamilies.entries.indexOfFirst { it.isRetired }
        val lastActiveIndex = FontFamilies.entries.indexOfLast { !it.isRetired }

        assertTrue(firstRetiredIndex > lastActiveIndex)
    }

    @Test
    fun fontRankIndexMap_activeFonts_rankIgnoresRetiredFonts() {
        val rankedActiveFonts = selectableFontFamilies.filterNot { it.isNew }

        rankedActiveFonts.forEachIndexed { index, font ->
            assertEquals(index, fontRankIndexMap[font])
        }
    }
}
