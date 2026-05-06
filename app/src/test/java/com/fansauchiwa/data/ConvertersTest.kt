package com.fansauchiwa.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toColorLong
import com.fansauchiwa.edit.FontFamilies
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun decorationsToJson_andDecorationsFromJson_roundTripsAllDecorationTypes() {
        val decorations = listOf(
            Decoration.Text(
                text = "推し活",
                id = "text-1",
                offset = Offset(12f, 24f),
                rotation = 15f,
                scale = 1.2f,
                color = Color(0xFFFFEB3B),
                strokeColor = Color(0xFFE91E63),
                strokeWidth = 28f,
                secondBorderColor = Color(0xFFFFFFFF),
                secondBorderWidth = 8f,
                isPuffyEnabled = true,
                width = 700,
                font = FontFamilies.KEI_FONT
            ),
            Decoration.Sticker(
                label = "star",
                id = "sticker-1",
                offset = Offset(40f, 56f),
                rotation = 45f,
                scale = 0.9f,
                color = Color(0xFFFF9800),
                strokeColor = Color(0xFF212121),
                strokeWidth = 4f,
                secondStrokeColor = Color(0xFFFFFFFF),
                secondStrokeWidth = 2f
            ),
            Decoration.Image(
                id = "image-1",
                imageId = "image-resource-1",
                offset = Offset(80f, 96f),
                rotation = 90f,
                scale = 1.5f,
                color = Color(0xFFFFFFFF),
                strokeColor = Color(0xFF03A9F4),
                strokeWidth = 18f
            )
        )

        val restored = converters.decorationsFromJson(converters.decorationsToJson(decorations))

        assertEquals(decorations, restored)
    }

    @Test
    fun decorationsFromJson_legacyDecorationPayload_restoresDomainModels() {
        val legacyJson = """
            [
              {
                "type":"com.fansauchiwa.data.Decoration.Text",
                "text":"テスト",
                "id":"text-legacy",
                "offset":{"x":16.0,"y":32.0},
                "rotation":10.0,
                "scale":1.1,
                "color":${Color(0xFFFFFFFF).toColorLong()},
                "strokeColor":${Color(0xFFE91E63).toColorLong()},
                "strokeWidth":24.0,
                "secondBorderColor":${Color(0xFFFFEB3B).toColorLong()},
                "secondBorderWidth":4.0,
                "isPuffyEnabled":true,
                "width":800,
                "font":"KEI_FONT"
              },
              {
                "type":"com.fansauchiwa.data.Decoration.Image",
                "id":"image-legacy",
                "imageId":"image-123",
                "offset":{"x":48.0,"y":64.0},
                "rotation":0.0,
                "scale":1.0,
                "color":${Color(0xFFFFFFFF).toColorLong()},
                "strokeColor":${Color(0xFF03A9F4).toColorLong()},
                "strokeWidth":30.0
              }
            ]
        """.trimIndent()

        val restored = converters.decorationsFromJson(legacyJson)

        assertEquals(
            listOf(
                Decoration.Text(
                    text = "テスト",
                    id = "text-legacy",
                    offset = Offset(16f, 32f),
                    rotation = 10f,
                    scale = 1.1f,
                    color = Color(0xFFFFFFFF),
                    strokeColor = Color(0xFFE91E63),
                    strokeWidth = 24f,
                    secondBorderColor = Color(0xFFFFEB3B),
                    secondBorderWidth = 4f,
                    isPuffyEnabled = true,
                    width = 800,
                    font = FontFamilies.KEI_FONT
                ),
                Decoration.Image(
                    id = "image-legacy",
                    imageId = "image-123",
                    offset = Offset(48f, 64f),
                    rotation = 0f,
                    scale = 1f,
                    color = Color(0xFFFFFFFF),
                    strokeColor = Color(0xFF03A9F4),
                    strokeWidth = 30f
                )
            ),
            restored
        )
    }
}
