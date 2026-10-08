package com.fansauchiwa.edit.decorationitem

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.edit.FontFamilies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextItemContentTest {

    private fun textDecoration(strokeWidth: Float = 0f, secondBorderWidth: Float = 0f, width: Int = 900) =
        Decoration.Text(
            id = "t",
            font = FontFamilies.NOTO_SANS_JP,
            strokeWidth = strokeWidth,
            secondBorderWidth = secondBorderWidth,
            width = width
        )

    @Test
    fun supportsPukuPukuEffect_whenSdkIsBelowAndroid13_returnsFalse() {
        assertFalse(supportsPukuPukuEffect(32))
    }

    @Test
    fun supportsPukuPukuEffect_whenSdkIsAndroid13OrAbove_returnsTrue() {
        assertTrue(supportsPukuPukuEffect(33))
    }

    @Test
    fun decorationTextFrameSize_NoStroke_ReturnsMeasuredSize() {
        assertEquals(Size(120f, 40f), decorationTextFrameSize(IntSize(120, 40), maxStroke = 0f))
    }

    @Test
    fun decorationTextFrameSize_WithStroke_AddsStrokeToWidthAndHeight() {
        assertEquals(Size(150f, 70f), decorationTextFrameSize(IntSize(120, 40), maxStroke = 30f))
    }

    @Test
    fun decorationTextFrameSize_EmptyMeasuredSize_ReturnsStrokeOnly() {
        assertEquals(Size(30f, 30f), decorationTextFrameSize(IntSize.Zero, maxStroke = 30f))
    }

    @Test
    fun maxStroke_StrokeAndSecondBorder_ReturnsSum() {
        assertEquals(40f, textDecoration(strokeWidth = 30f, secondBorderWidth = 10f).maxStroke)
    }

    @Test
    fun maxStroke_NoSecondBorder_ReturnsStrokeWidth() {
        assertEquals(30f, textDecoration(strokeWidth = 30f).maxStroke)
    }

    @Test
    fun maxStroke_NoBorders_ReturnsZero() {
        assertEquals(0f, textDecoration().maxStroke)
    }

    @Test
    fun fontWeight_Width900_ReturnsW900() {
        assertEquals(FontWeight.W900, textDecoration(width = 900).fontWeight)
    }

    @Test
    fun fontWeight_Width400_ReturnsNormal() {
        assertEquals(FontWeight.Normal, textDecoration(width = 400).fontWeight)
    }
}
