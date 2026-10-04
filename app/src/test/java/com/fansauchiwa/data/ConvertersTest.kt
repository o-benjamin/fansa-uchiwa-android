package com.fansauchiwa.data

import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.ui.StickerAsset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 保存済みのうちわ（Room に JSON で保存）のフォントが、[FontFamilies] の宣言順を
 * 並べ替えても変わらないことを確かめる（#241）。ステッカーも、[StickerAsset] を足したり
 * 並べ替えたりしても変わらないことを確かめる（#287）。
 */
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun decorationsToJson_textDecoration_writesFontByName() {
        val text = Decoration.Text(id = "text-1", font = FontFamilies.KLEE_ONE)

        val json = converters.decorationsToJson(listOf(text))

        assertTrue(json.contains("\"font\":\"KLEE_ONE\""))
    }

    @Test
    fun decorationsFromJson_everyFont_restoresSameFont() {
        FontFamilies.entries.forEach { font ->
            val json = converters.decorationsToJson(listOf(Decoration.Text(id = "text-1", font = font)))

            val restored = converters.decorationsFromJson(json).single() as Decoration.Text

            assertEquals(font, restored.font)
        }
    }

    // ステッカーは `label`（= StickerAsset.type の文字列）で保存し、描画する drawable は読み込むたびに label から引き直す。
    // R.drawable の値はステッカーを足すとずれるため、resId が JSON に書かれていないことを確かめる。
    // resId は本体で label から計算するプロパティで、値が初期化式と同じなので、Json の既定（encodeDefaults = false）では書かれない（#287）。
    @Test
    fun decorationsToJson_stickerDecoration_writesLabelWithoutResId() {
        val sticker = Decoration.Sticker(label = StickerAsset.HEART.type, id = "sticker-1")

        val json = converters.decorationsToJson(listOf(sticker))

        assertTrue(json.contains("\"label\":\"heart\""))
        assertFalse(json.contains("resId"))
    }

    @Test
    fun decorationsFromJson_everySticker_restoresSameDrawable() {
        StickerAsset.entries.forEach { asset ->
            val json = converters.decorationsToJson(listOf(Decoration.Sticker(label = asset.type, id = "sticker-1")))

            val restored = converters.decorationsFromJson(json).single() as Decoration.Sticker

            assertEquals(asset.type, restored.label)
            assertEquals(asset.resId, restored.resId)
        }
    }
}
