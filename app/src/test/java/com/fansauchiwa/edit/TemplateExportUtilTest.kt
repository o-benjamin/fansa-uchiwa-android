package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.ui.DecorationColors
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateExportUtilTest {

    @Test
    fun exportToKotlinCode_emptyDecorations_returnsValidCode() {
        val uchiwa = SavedUchiwa(
            decorations = emptyList(),
            uchiwaColor = Color(0xFFFF0000),
            backgroundColor = Color(0xFFFFFFFF)
        )

        val result = TemplateExportUtil.exportToKotlinCode(uchiwa)

        assertTrue(result.contains("SavedUchiwa("))
        assertTrue(result.contains("decorations = listOf("))
        assertTrue(result.contains("uchiwaColor = Color(0xFFFF0000)"))
        assertTrue(result.contains("backgroundColor = Color(0xFFFFFFFF)"))
    }

    @Test
    fun exportToKotlinCode_withTextDecoration_containsTextProperties() {
        val uchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "推し",
                    id = "text_1",
                    offset = Offset(0.3f, 0.4f),
                    scale = 1.5f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.MAGENTA.value,
                    strokeWidth = 30f,
                    font = FontFamilies.DELA_GOTHIC_ONE
                )
            ),
            uchiwaColor = Color(0xFFFF69B4),
            backgroundColor = Color(0xFFFFFFFF)
        )

        val result = TemplateExportUtil.exportToKotlinCode(uchiwa)

        assertTrue(result.contains("Decoration.Text("))
        assertTrue(result.contains("text = \"推し\""))
        assertTrue(result.contains("id = \"text_1\""))
        assertTrue(result.contains("offset = Offset(0.3f, 0.4f)"))
        assertTrue(result.contains("scale = 1.5f"))
        assertTrue(result.contains("color = DecorationColors.WHITE.value"))
        assertTrue(result.contains("strokeColor = DecorationColors.MAGENTA.value"))
        assertTrue(result.contains("strokeWidth = 30.0f"))
        assertTrue(result.contains("font = FontFamilies.DELA_GOTHIC_ONE"))
    }

    @Test
    fun exportToKotlinCode_withStickerDecoration_containsStickerProperties() {
        val uchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Sticker(
                    label = "heart",
                    id = "sticker_1",
                    offset = Offset(0.6f, 0.2f),
                    scale = 1.2f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0xFFFFFFFF)
        )

        val result = TemplateExportUtil.exportToKotlinCode(uchiwa)

        assertTrue(result.contains("Decoration.Sticker("))
        assertTrue(result.contains("label = \"heart\""))
        assertTrue(result.contains("id = \"sticker_1\""))
        assertTrue(result.contains("offset = Offset(0.6f, 0.2f)"))
        assertTrue(result.contains("scale = 1.2f"))
        assertTrue(result.contains("color = DecorationColors.RED.value"))
        assertTrue(result.contains("strokeColor = DecorationColors.WHITE.value"))
        assertTrue(result.contains("strokeWidth = 3.0f"))
    }

    @Test
    fun exportToKotlinCode_withImageDecoration_containsImageProperties() {
        val uchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Image(
                    id = "image_1",
                    imageId = "img_ref_1",
                    offset = Offset(0.5f, 0.5f),
                    scale = 1.0f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.CYAN.value,
                    strokeWidth = 30f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0xFFFFFFFF)
        )

        val result = TemplateExportUtil.exportToKotlinCode(uchiwa)

        assertTrue(result.contains("Decoration.Image("))
        assertTrue(result.contains("id = \"image_1\""))
        assertTrue(result.contains("imageId = \"img_ref_1\""))
        assertTrue(result.contains("offset = Offset(0.5f, 0.5f)"))
        assertTrue(result.contains("scale = 1.0f"))
    }

    @Test
    fun exportToKotlinCode_withMultipleDecorations_containsAllDecorations() {
        val uchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "テスト",
                    id = "text_1",
                    offset = Offset(0.1f, 0.1f),
                    scale = 1.0f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.RED.value,
                    strokeWidth = 20f,
                    font = FontFamilies.ROCKNROLL_ONE
                ),
                Decoration.Sticker(
                    label = "star",
                    id = "sticker_1",
                    offset = Offset(0.5f, 0.5f),
                    scale = 1.0f,
                    color = DecorationColors.YELLOW.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 3f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0xFFFFFFFF)
        )

        val result = TemplateExportUtil.exportToKotlinCode(uchiwa)

        assertTrue(result.contains("Decoration.Text("))
        assertTrue(result.contains("Decoration.Sticker("))
        assertTrue(result.contains("text = \"テスト\""))
        assertTrue(result.contains("label = \"star\""))
    }

    @Test
    fun exportToKotlinCode_customColor_usesColorHexFormat() {
        val customColor = Color(0xFF123456)
        val uchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Sticker(
                    label = "test",
                    id = "sticker_1",
                    offset = Offset.Zero,
                    scale = 1.0f,
                    color = customColor,
                    strokeColor = customColor,
                    strokeWidth = 1f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0xFFFFFFFF)
        )

        val result = TemplateExportUtil.exportToKotlinCode(uchiwa)

        assertTrue(result.contains("Color(0xFF123456)"))
        assertFalse(result.contains("DecorationColors."))
    }

    @Test
    fun exportToKotlinCode_knownDecorationColor_usesDecorationColorsEnum() {
        val uchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Sticker(
                    label = "test",
                    id = "sticker_1",
                    offset = Offset.Zero,
                    scale = 1.0f,
                    color = DecorationColors.RED.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 1f
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0xFFFFFFFF)
        )

        val result = TemplateExportUtil.exportToKotlinCode(uchiwa)

        assertTrue(result.contains("DecorationColors.RED.value"))
        assertTrue(result.contains("DecorationColors.WHITE.value"))
    }

    @Test
    fun exportToKotlinCode_textWithRotation_containsRotationProperty() {
        val uchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "回転",
                    id = "text_1",
                    offset = Offset.Zero,
                    rotation = 45f,
                    scale = 1.0f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.WHITE.value,
                    strokeWidth = 10f,
                    font = FontFamilies.NOTO_SANS_JP
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0xFFFFFFFF)
        )

        val result = TemplateExportUtil.exportToKotlinCode(uchiwa)

        assertTrue(result.contains("rotation = 45.0f"))
    }

    @Test
    fun exportToKotlinCode_textWithSecondBorder_containsSecondBorderProperties() {
        val uchiwa = SavedUchiwa(
            decorations = listOf(
                Decoration.Text(
                    text = "二重縁",
                    id = "text_1",
                    offset = Offset.Zero,
                    scale = 1.0f,
                    color = DecorationColors.WHITE.value,
                    strokeColor = DecorationColors.RED.value,
                    strokeWidth = 20f,
                    secondBorderColor = DecorationColors.BLUE.value,
                    secondBorderWidth = 10f,
                    font = FontFamilies.POTTA_ONE
                )
            ),
            uchiwaColor = Color(0xFF000000),
            backgroundColor = Color(0xFFFFFFFF)
        )

        val result = TemplateExportUtil.exportToKotlinCode(uchiwa)

        assertTrue(result.contains("secondBorderColor = DecorationColors.BLUE.value"))
        assertTrue(result.contains("secondBorderWidth = 10.0f"))
    }
}
