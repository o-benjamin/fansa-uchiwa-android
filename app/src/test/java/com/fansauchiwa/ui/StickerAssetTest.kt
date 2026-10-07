package com.fansauchiwa.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 保存済みのうちわは `Decoration.Sticker.label`（= `StickerAsset.type`）から drawable を引き直す（`Decoration.Sticker.resId`）。
 * `type` が重なると、片方のステッカーがもう片方の形で描かれるため、重なりがないことを確かめる。
 * drawable の重なりは、ステッカーを足すときに、別の enum と同じ drawable を指す書き写し間違いを見つけるために確かめる（#287）。
 */
class StickerAssetTest {

    @Test
    fun stickerAsset_allEntries_haveUniqueType() {
        val duplicated = StickerAsset.entries.groupBy { it.type }.filterValues { it.size > 1 }.values.flatten()

        assertEquals(emptyList<StickerAsset>(), duplicated)
    }

    @Test
    fun stickerAsset_allEntries_haveUniqueDrawable() {
        val duplicated = StickerAsset.entries.groupBy { it.resId }.filterValues { it.size > 1 }.values.flatten()

        assertEquals(emptyList<StickerAsset>(), duplicated)
    }
}
