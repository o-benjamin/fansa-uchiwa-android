package com.fansauchiwa.edit.decorationitem

import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

@Composable
fun TextItemContent(
    decoration: Decoration.Text,
    textSize: TextUnit,
    modifier: Modifier = Modifier,
    isPuffyEnabled: Boolean = decoration.isPuffyEnabled
) {
    val shouldRenderPuffyText = isPuffyEnabled && supportsPukuPukuEffect()
    val density = LocalDensity.current
    val textColor = decoration.color
    val strokeColor = decoration.strokeColor
    val secondBorderColor = decoration.secondBorderColor
    val secondBorderWidth = decoration.secondBorderWidth

    // layoutResult は、文字・書式が変わったときと、フォントの取得が終わったときだけ別の値になる（measureDecorationText の KDoc）
    val layoutResult = measureDecorationText(
        text = decoration.text,
        fontFamily = decoration.font.value,
        fontWeight = FontWeight(decoration.width),
        fontSize = textSize
    )

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
            val isHardware = drawContext.canvas.nativeCanvas.isHardwareAccelerated
            translate(maxStroke / 2f, maxStroke / 2f) {
                if (secondBorderWidth > 0f) {
                    if (!shouldRenderPuffyText || secondBorderSdfBitmap == null || !isHardware) {
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

                if (!shouldRenderPuffyText || strokeSdfBitmap == null || !isHardware) {
                    drawText(
                        textLayoutResult = layoutResult,
                        drawStyle = Stroke(width = decoration.strokeWidth, join = StrokeJoin.Round),
                        color = strokeColor,
                    )
                }

                if (!shouldRenderPuffyText || fillSdfBitmap == null || !isHardware) {
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

/**
 * 文字の装飾を、描画（[TextItemContent]）とつかめる範囲（`EditScreen`）で共通の書式で測る。
 *
 * ダウンロード式フォント（`GoogleFont`）は、取得が終わるまで標準の書体で測られる。
 * 取得が終わると、測ったときに読んだフォントの状態が変わって呼び出し元が再コンポーズされ、
 * [TextMeasurer][androidx.compose.ui.text.TextMeasurer] は古くなったキャッシュを捨てて測り直す。
 * そのため結果を `remember` に入れないこと。フォントの取得が終わったことはキーにできず、標準の書体のまま固まる（#305）。
 *
 * 文字・書式・フォントの取得の状態が変わらないあいだは、キャッシュから等しい（`equals`）結果が返る。
 * そのため結果を `LaunchedEffect` のキーにしても、再コンポーズのたびに動き直すことはない。キャッシュを無効にしないこと。
 */
@Composable
internal fun measureDecorationText(
    text: String,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    fontSize: TextUnit
): TextLayoutResult {
    val measurer = rememberTextMeasurer()
    return measurer.measure(
        text = AnnotatedString(text),
        style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            fontSize = fontSize,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )
    )
}

/**
 * 文字の装飾それぞれが、いま描かれている書体。
 *
 * ダウンロード式フォントの取得が終わると別の値になる。文字の形を写し取って作るもの（全体の縁取り）は、
 * これをキーにして作り直す。キーにしないと、文字は新しい書体なのに、縁取りは標準の書体の形のまま残る（#305）。
 */
@Composable
internal fun resolveTextDecorationTypefaces(decorations: List<Decoration>): List<Any> =
    decorations.filterIsInstance<Decoration.Text>().map { decoration ->
        resolveDecorationTypeface(
            fontFamily = decoration.font.value,
            fontWeight = FontWeight(decoration.width)
        )
    }

/** [fontFamily] の [fontWeight] で、いま描かれている書体。取得中のダウンロード式フォントは標準の書体になる。 */
@Composable
internal fun resolveDecorationTypeface(fontFamily: FontFamily, fontWeight: FontWeight): Any =
    LocalFontFamilyResolver.current.resolve(fontFamily, fontWeight).value

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
