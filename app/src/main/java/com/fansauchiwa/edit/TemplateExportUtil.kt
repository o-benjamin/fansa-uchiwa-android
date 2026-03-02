package com.fansauchiwa.edit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.ui.DecorationColors

object TemplateExportUtil {

    fun exportToKotlinCode(uchiwa: SavedUchiwa): String {
        val decorationsCode = uchiwa.decorations.joinToString(
            separator = ",\n"
        ) { decoration ->
            exportDecoration(decoration)
        }

        return buildString {
            appendLine("SavedUchiwa(")
            appendLine("    decorations = listOf(")
            appendLine(decorationsCode.prependIndent("        "))
            appendLine("    ),")
            appendLine("    uchiwaColor = ${colorToCode(uchiwa.uchiwaColor)},")
            appendLine("    backgroundColor = ${colorToCode(uchiwa.backgroundColor)}")
            append(")")
        }
    }

    private fun exportDecoration(decoration: Decoration): String {
        return when (decoration) {
            is Decoration.Text -> exportTextDecoration(decoration)
            is Decoration.Sticker -> exportStickerDecoration(decoration)
            is Decoration.Image -> exportImageDecoration(decoration)
        }
    }

    private fun exportTextDecoration(text: Decoration.Text): String {
        return buildString {
            appendLine("Decoration.Text(")
            appendLine("    text = \"${text.text}\",")
            appendLine("    id = \"${text.id}\",")
            appendLine("    offset = Offset(${text.offset.x}f, ${text.offset.y}f),")
            appendLine("    rotation = ${text.rotation}f,")
            appendLine("    scale = ${text.scale}f,")
            appendLine("    color = ${colorToDecorationColorCode(text.color)},")
            appendLine("    strokeColor = ${colorToDecorationColorCode(text.strokeColor)},")
            appendLine("    strokeWidth = ${text.strokeWidth}f,")
            appendLine("    secondBorderColor = ${colorToDecorationColorCode(text.secondBorderColor)},")
            appendLine("    secondBorderWidth = ${text.secondBorderWidth}f,")
            appendLine("    width = ${text.width},")
            appendLine("    font = FontFamilies.${text.font.name}")
            append(")")
        }
    }

    private fun exportStickerDecoration(sticker: Decoration.Sticker): String {
        return buildString {
            appendLine("Decoration.Sticker(")
            appendLine("    label = \"${sticker.label}\",")
            appendLine("    id = \"${sticker.id}\",")
            appendLine("    offset = Offset(${sticker.offset.x}f, ${sticker.offset.y}f),")
            appendLine("    rotation = ${sticker.rotation}f,")
            appendLine("    scale = ${sticker.scale}f,")
            appendLine("    color = ${colorToDecorationColorCode(sticker.color)},")
            appendLine("    strokeColor = ${colorToDecorationColorCode(sticker.strokeColor)},")
            appendLine("    strokeWidth = ${sticker.strokeWidth}f,")
            appendLine("    secondStrokeColor = ${colorToDecorationColorCode(sticker.secondStrokeColor)},")
            appendLine("    secondStrokeWidth = ${sticker.secondStrokeWidth}f")
            append(")")
        }
    }

    private fun exportImageDecoration(image: Decoration.Image): String {
        return buildString {
            appendLine("Decoration.Image(")
            appendLine("    id = \"${image.id}\",")
            appendLine("    imageId = \"${image.imageId}\",")
            appendLine("    offset = Offset(${image.offset.x}f, ${image.offset.y}f),")
            appendLine("    rotation = ${image.rotation}f,")
            appendLine("    scale = ${image.scale}f,")
            appendLine("    color = ${colorToDecorationColorCode(image.color)},")
            appendLine("    strokeColor = ${colorToDecorationColorCode(image.strokeColor)},")
            appendLine("    strokeWidth = ${image.strokeWidth}f")
            append(")")
        }
    }

    private fun colorToCode(color: Color): String {
        val argb = color.toArgb()
        return "Color(0x${"%08X".format(argb)})"
    }

    private fun colorToDecorationColorCode(color: Color): String {
        val matched = DecorationColors.entries.find { it.value == color }
        return if (matched != null) {
            "DecorationColors.${matched.name}.value"
        } else {
            colorToCode(color)
        }
    }
}

