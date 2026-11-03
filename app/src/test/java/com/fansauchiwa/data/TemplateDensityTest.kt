package com.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.fansauchiwa.edit.FontFamilies
import org.junit.Assert.assertEquals
import org.junit.Test

class TemplateDensityTest {

    private val text = Decoration.Text(
        id = "text-1",
        text = "名",
        offset = Offset(-21f, 150f),
        rotation = 15f,
        scale = 6f,
        strokeWidth = 10f,
        secondBorderWidth = 15f,
        font = FontFamilies.M_PLUS_ROUNDED_1C
    )
    private val sticker = Decoration.Sticker(
        label = "heart",
        id = "sticker-1",
        offset = Offset(200f, -160f),
        rotation = 28f,
        scale = 0.7f,
        strokeWidth = 3f,
        secondStrokeWidth = 3f
    )
    private val image = Decoration.Image(
        id = "image-1",
        imageId = "image-file-1",
        offset = Offset(30f, -60f),
        strokeWidth = 30f
    )
    private val template = Template(
        id = "template_1",
        savedUchiwa = SavedUchiwa(
            decorations = listOf(text, sticker, image),
            uchiwaColor = Color.Black,
            backgroundColor = Color.White,
            overallBorderColor = Color.Red,
            overallBorderWidth = 4f
        ),
        isNameInputPlaceholderEnabled = true
    )

    @Test
    fun scaledToDensity_baseDensity_returnsEqualTemplate() {
        assertEquals(template, template.scaledToDensity(TEMPLATE_BASE_DENSITY))
    }

    @Test
    fun scaledToDensity_higherDensity_scalesTextOffsetAndBorders() {
        // 3.5 / 2.625 = 4/3
        val scaled = template.scaledToDensity(3.5f).savedUchiwa.decorations[0] as Decoration.Text

        assertEquals(-28f, scaled.offset.x, DELTA)
        assertEquals(200f, scaled.offset.y, DELTA)
        assertEquals(13.333333f, scaled.strokeWidth, DELTA)
        assertEquals(20f, scaled.secondBorderWidth, DELTA)
    }

    @Test
    fun scaledToDensity_lowerDensity_shrinksStickerOffset() {
        // 2.0 / 2.625 = 0.7619...
        val scaled = template.scaledToDensity(2.0f).savedUchiwa.decorations[1] as Decoration.Sticker

        assertEquals(152.38095f, scaled.offset.x, DELTA)
        assertEquals(-121.90476f, scaled.offset.y, DELTA)
    }

    @Test
    fun scaledToDensity_higherDensity_scalesOverallBorderWidth() {
        assertEquals(5.3333335f, template.scaledToDensity(3.5f).savedUchiwa.overallBorderWidth, DELTA)
    }

    @Test
    fun scaledToDensity_higherDensity_keepsStickerBordersInViewportUnits() {
        val scaled = template.scaledToDensity(3.5f).savedUchiwa.decorations[1] as Decoration.Sticker

        assertEquals(sticker.copy(offset = scaled.offset), scaled)
    }

    @Test
    fun scaledToDensity_higherDensity_scalesImageOffsetOnly() {
        val scaled = template.scaledToDensity(3.5f).savedUchiwa.decorations[2] as Decoration.Image

        assertEquals(40f, scaled.offset.x, DELTA)
        assertEquals(-80f, scaled.offset.y, DELTA)
        assertEquals(image.copy(offset = scaled.offset), scaled)
    }

    @Test
    fun scaledToDensity_higherDensity_keepsUnitlessValuesAndColors() {
        val scaled = template.scaledToDensity(3.5f)
        val scaledText = scaled.savedUchiwa.decorations[0] as Decoration.Text

        assertEquals(
            text.copy(offset = scaledText.offset, strokeWidth = scaledText.strokeWidth, secondBorderWidth = scaledText.secondBorderWidth),
            scaledText
        )
        assertEquals(
            template.savedUchiwa.copy(decorations = scaled.savedUchiwa.decorations, overallBorderWidth = scaled.savedUchiwa.overallBorderWidth),
            scaled.savedUchiwa
        )
        assertEquals(template.id, scaled.id)
        assertEquals(template.isNameInputPlaceholderEnabled, scaled.isNameInputPlaceholderEnabled)
    }

    @Test
    fun scaledToDensity_zeroOffsetAndBorders_staysZero() {
        val flat = template.copy(
            savedUchiwa = template.savedUchiwa.copy(
                decorations = listOf(text.copy(offset = Offset.Zero, strokeWidth = 0f, secondBorderWidth = 0f)),
                overallBorderWidth = 0f
            )
        )

        assertEquals(flat, flat.scaledToDensity(3.5f))
    }

    @Test
    fun scaledToDensity_emptyDecorations_scalesOverallBorderOnly() {
        val empty = template.copy(savedUchiwa = template.savedUchiwa.copy(decorations = emptyList()))

        val scaled = empty.scaledToDensity(2.0f)

        assertEquals(emptyList<Decoration>(), scaled.savedUchiwa.decorations)
        assertEquals(3.047619f, scaled.savedUchiwa.overallBorderWidth, DELTA)
    }

    private companion object {
        const val DELTA = 0.0001f
    }
}
