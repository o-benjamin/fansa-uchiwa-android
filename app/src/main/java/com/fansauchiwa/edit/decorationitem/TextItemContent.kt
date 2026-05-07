package com.fansauchiwa.edit.decorationitem

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

internal fun supportsPukuPukuTextEffect(sdkInt: Int = Build.VERSION.SDK_INT): Boolean {
    return sdkInt >= Build.VERSION_CODES.TIRAMISU
}

@Composable
fun TextItemContent(
    decoration: Decoration.Text,
    textSize: TextUnit,
    modifier: Modifier = Modifier,
    isPuffyEnabled: Boolean = decoration.isPuffyEnabled
) {
    val shouldRenderPuffyText = isPuffyEnabled && supportsPukuPukuTextEffect()
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

    val maxStroke = decoration.strokeWidth + decoration.secondBorderWidth
    val boxSize = with(density) {
        Size(layoutResult.size.width + maxStroke, layoutResult.size.height + maxStroke).toDpSize()
    }

    var fillSdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var strokeSdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var secondBorderSdfBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // ▼ チューニング: 【描画の解像度（綺麗さ）】 ▼
    // scaleFactor: 画像を何倍のサイズで内部生成して縮小表示するか（スーパーサンプリング）。
    // - 大きくする(例: 3.0f): 文字の輪郭や影のギザギザがより滑らかになりますが、処理は重くなります。
    // - 小さくする(例: 1.0f): 処理は軽いですが、画質が荒くなります。
    val scaleFactor = 3.0f

    val strokeDrawStyle = remember(decoration.strokeWidth) {
        Stroke(width = decoration.strokeWidth, join = StrokeJoin.Round)
    }

    val secondBorderDrawStyle = remember(decoration.strokeWidth, decoration.secondBorderWidth) {
        Stroke(
            width = decoration.strokeWidth + decoration.secondBorderWidth,
            join = StrokeJoin.Round
        )
    }

    LaunchedEffect(
        layoutResult,
        decoration.strokeWidth,
        decoration.secondBorderWidth,
        shouldRenderPuffyText,
        maxStroke
    ) {
        if (shouldRenderPuffyText) {
            val fillMaskBitmap =
                createTextMaskBitmap(layoutResult, density, Fill, scaleFactor, maxStroke)
            fillSdfBitmap = generateSdfTexture(fillMaskBitmap)

            if (decoration.strokeWidth > 0f) {
                val strokeMaskBitmap =
                    createTextMaskBitmap(
                        layoutResult,
                        density,
                        strokeDrawStyle,
                        scaleFactor,
                        maxStroke,
                        clearInner = true
                    )
                strokeSdfBitmap = generateSdfTexture(strokeMaskBitmap)
            } else {
                strokeSdfBitmap = null
            }

            if (decoration.secondBorderWidth > 0f) {
                val secondBorderMaskBitmap =
                    createTextMaskBitmap(
                        layoutResult,
                        density,
                        secondBorderDrawStyle,
                        scaleFactor,
                        maxStroke,
                        clearInner = true,
                        clearStroke = strokeDrawStyle
                    )
                secondBorderSdfBitmap = generateSdfTexture(secondBorderMaskBitmap)
            } else {
                secondBorderSdfBitmap = null
            }
        } else {
            fillSdfBitmap = null
            strokeSdfBitmap = null
            secondBorderSdfBitmap = null
        }
    }

    Box(
        modifier = modifier.size(boxSize)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            translate(maxStroke / 2f, maxStroke / 2f) {
                if (secondBorderWidth > 0f) {
                    if (!shouldRenderPuffyText || secondBorderSdfBitmap == null) {
                        drawText(
                            textLayoutResult = layoutResult,
                            drawStyle = Stroke(
                                width = decoration.strokeWidth + secondBorderWidth,
                                join = StrokeJoin.Round
                            ),
                            color = secondBorderColor,
                        )
                    }
                }

                if (!shouldRenderPuffyText || strokeSdfBitmap == null) {
                    drawText(
                        textLayoutResult = layoutResult,
                        drawStyle = Stroke(width = decoration.strokeWidth, join = StrokeJoin.Round),
                        color = strokeColor,
                    )
                }

                if (!shouldRenderPuffyText || fillSdfBitmap == null) {
                    drawText(
                        textLayoutResult = layoutResult,
                        drawStyle = Fill,
                        color = textColor,
                    )
                }
            }
        }

        if (shouldRenderPuffyText) {
            if (secondBorderSdfBitmap != null) {
                PuffyTextRenderer(
                    sdfTextureBitmap = secondBorderSdfBitmap!!,
                    baseColor = secondBorderColor,
                    scaleFactor = scaleFactor,
                    modifier = Modifier.matchParentSize()
                )
            }
            if (strokeSdfBitmap != null) {
                PuffyTextRenderer(
                    sdfTextureBitmap = strokeSdfBitmap!!,
                    baseColor = strokeColor,
                    scaleFactor = scaleFactor,
                    modifier = Modifier.matchParentSize()
                )
            }
            if (fillSdfBitmap != null) {
                PuffyTextRenderer(
                    sdfTextureBitmap = fillSdfBitmap!!,
                    baseColor = textColor,
                    scaleFactor = scaleFactor,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

// region TextItemContent Previews

private val previewTextDecoration = Decoration.Text(
    id = "preview-text-1",
    text = "推し活最高！",
    font = FontFamilies.HACHI_MARU_POP,
    color = Color.White,
    strokeColor = Color.Magenta,
    strokeWidth = 30f,
    secondBorderColor = Color.White,
    secondBorderWidth = 0f,
    width = FontWeight.W900.weight,
)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TextItemContentPreview() {
    FansaUchiwaTheme {
        TextItemContent(
            decoration = previewTextDecoration,
            textSize = 48.sp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TextItemContentWithSecondBorderPreview() {
    FansaUchiwaTheme {
        TextItemContent(
            decoration = previewTextDecoration.copy(
                secondBorderWidth = 10f,
                secondBorderColor = Color.Cyan
            ),
            textSize = 48.sp,
        )
    }
}

// endregion
