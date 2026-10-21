package com.fansauchiwa.edit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FontFamiliesTest {

    @Test
    fun selectableFontFamilies_retiredFont_isExcluded() {
        assertFalse(selectableFontFamilies.contains(FontFamilies.WDXL_LUBRIFONT_JP_N))
        assertTrue(selectableFontFamilies.none { it.isRetired })
    }

    @Test
    fun fontRankIndexMap_selectableFonts_areNotShiftedByRetiredFonts() {
        val rankedSelectableFonts = selectableFontFamilies.filterNot { it.isNew }

        rankedSelectableFonts.forEachIndexed { index, font ->
            assertEquals(index, fontRankIndexMap[font])
        }
    }

    @Test
    fun fontRankIndexMap_retiredFonts_haveNoRank() {
        FontFamilies.entries.filter { it.isRetired }.forEach { font ->
            assertNull(fontRankIndexMap[font])
        }
    }
}
