package com.fansauchiwa.edit

import com.fansauchiwa.data.Decoration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PuffyStateTest {

    private fun text(id: String, isPuffy: Boolean) =
        Decoration.Text(id = id, text = "テスト", font = FontFamilies.NOTO_SANS_JP, isPuffyEnabled = isPuffy)

    private fun sticker(id: String, isPuffy: Boolean) =
        Decoration.Sticker(id = id, label = "star", isPukupuku = isPuffy)

    private fun image(id: String) = Decoration.Image(id = id, imageId = "image-$id")

    @Test
    fun of_allTextStickerAndBorderPuffy_returnsOn() {
        val decorations = listOf(text("t", true), sticker("s", true))

        assertEquals(PuffyState.ON, PuffyState.of(decorations, isOverallBorderPuffyEnabled = true))
    }

    @Test
    fun of_nothingPuffy_returnsOff() {
        val decorations = listOf(text("t", false), sticker("s", false))

        assertEquals(PuffyState.OFF, PuffyState.of(decorations, isOverallBorderPuffyEnabled = false))
    }

    @Test
    fun of_onlyTextPuffy_returnsMixed() {
        val decorations = listOf(text("t", true), sticker("s", false))

        assertEquals(PuffyState.MIXED, PuffyState.of(decorations, isOverallBorderPuffyEnabled = false))
    }

    @Test
    fun of_decorationsPuffyButBorderNot_returnsMixed() {
        val decorations = listOf(text("t", true), sticker("s", true))

        assertEquals(PuffyState.MIXED, PuffyState.of(decorations, isOverallBorderPuffyEnabled = false))
    }

    @Test
    fun of_onlyBorderPuffy_returnsMixed() {
        val decorations = listOf(text("t", false))

        assertEquals(PuffyState.MIXED, PuffyState.of(decorations, isOverallBorderPuffyEnabled = true))
    }

    @Test
    fun of_noDecorationsAndBorderPuffy_returnsOn() {
        assertEquals(PuffyState.ON, PuffyState.of(emptyList(), isOverallBorderPuffyEnabled = true))
    }

    @Test
    fun of_noDecorationsAndBorderNotPuffy_returnsOff() {
        assertEquals(PuffyState.OFF, PuffyState.of(emptyList(), isOverallBorderPuffyEnabled = false))
    }

    @Test
    fun of_imageIsIgnored_returnsOn() {
        val decorations = listOf(text("t", true), image("i"))

        assertEquals(PuffyState.ON, PuffyState.of(decorations, isOverallBorderPuffyEnabled = true))
    }

    @Test
    fun withPuffy_text_setsPuffyFlag() {
        val result = text("t", false).withPuffy(true)

        assertTrue((result as Decoration.Text).isPuffyEnabled)
    }

    @Test
    fun withPuffy_sticker_setsPukupukuFlag() {
        val result = sticker("s", true).withPuffy(false)

        assertFalse((result as Decoration.Sticker).isPukupuku)
    }

    @Test
    fun withPuffy_image_returnsSameImage() {
        val original = image("i")

        assertSame(original, original.withPuffy(true))
    }
}
