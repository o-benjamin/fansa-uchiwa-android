package com.fansauchiwa.edit.decorationitem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.toSize
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.edit.TEXT_ITEM_PADDING

@Composable
fun TextItemContent(
    decoration: Decoration.Text,
    textSize: TextUnit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val textColor = decoration.color
    val strokeColor = decoration.strokeColor
    val secondBorderColor = decoration.secondBorderColor
    val secondBorderWidth = decoration.secondBorderWidth

    val layoutResult = remember(decoration.text, decoration.font, decoration.width, textSize) {
        measurer.measure(
            text = AnnotatedString(decoration.text),
            style = TextStyle(
                fontFamily = decoration.font.value,
                fontWeight = FontWeight(decoration.width),
                fontSize = textSize,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }

    val boxSize = with(density) { layoutResult.size.toSize().toDpSize() }

    Box(
        modifier = modifier
            .padding(TEXT_ITEM_PADDING)
            .size(boxSize)
            .drawBehind {
                // 二重枠線（最背面）: secondBorderColor で描画（太さ：borderWidth + secondBorderWidth）
                if (secondBorderWidth > 0f) {
                    drawText(
                        textLayoutResult = layoutResult,
                        drawStyle = Stroke(
                            width = decoration.strokeWidth + secondBorderWidth,
                            join = StrokeJoin.Round
                        ),
                        color = secondBorderColor,
                        blendMode = if (!isSelected) BlendMode.SrcIn else BlendMode.SrcOver
                    )
                }
                // 枠線（中間）: strokeColor で描画（太さ：borderWidth）
                drawText(
                    textLayoutResult = layoutResult,
                    drawStyle = Stroke(width = decoration.strokeWidth, join = StrokeJoin.Round),
                    color = strokeColor,
                    blendMode = if (!isSelected) BlendMode.SrcIn else BlendMode.SrcOver
                )
                // 塗りつぶし（最前面）: color で本体を描画
                drawText(
                    textLayoutResult = layoutResult,
                    drawStyle = Fill,
                    color = textColor,
                    blendMode = if (!isSelected) BlendMode.SrcIn else BlendMode.SrcOver
                )
            }
    )
}
