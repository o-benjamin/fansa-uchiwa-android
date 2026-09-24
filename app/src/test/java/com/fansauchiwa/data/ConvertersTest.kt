package com.fansauchiwa.data

import com.fansauchiwa.edit.FontFamilies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 保存済みのうちわ（Room に JSON で保存）のフォントが、[FontFamilies] の宣言順を
 * 並べ替えても変わらないことを確かめる（#241）。
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
}
