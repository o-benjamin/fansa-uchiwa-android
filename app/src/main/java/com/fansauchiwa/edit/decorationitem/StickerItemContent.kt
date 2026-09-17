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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.ui.StickerAsset
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

/**
 * Stickerのコンテンツを描画するComposable
 * ぷくぷく(SDF)対応版
 */
@Composable
fun StickerItemContent(
    decoration: Decoration.Sticker,
    modifier: Modifier
) {
    if (decoration.resId == 0) return

    val imageVector = ImageVector.vectorResource(id = decoration.resId)
    val painter = rememberVectorPainter(image = imageVector)
    val density = LocalDensity.current

    val fillColor = decoration.color
    val strokeColor = decoration.strokeColor
    val strokeWidth = decoration.strokeWidth
    val secondStrokeColor = decoration.secondStrokeColor
    val secondStrokeWidth = decoration.secondStrokeWidth

    // intrinsicSizeを基準に、strokeWidth分を考慮した内部スケールを計算
    val intrinsicSize = painter.intrinsicSize
    val totalStrokeWidth = strokeWidth + secondStrokeWidth
    val innerScaleX = if (intrinsicSize.width > totalStrokeWidth * 2) {
        (intrinsicSize.width - totalStrokeWidth * 10) / intrinsicSize.width
    } else {
        0.1f
    }
    val innerScaleY = if (intrinsicSize.height > totalStrokeWidth * 2) {
        (intrinsicSize.height - totalStrokeWidth * 10) / intrinsicSize.height
    } else {
        0.1f
    }
    val innerScale = minOf(innerScaleX, innerScaleY)
    val boxSize = Size(intrinsicSize.width, intrinsicSize.height)

    val shouldRenderPukupuku = decoration.isPukupuku && supportsPukuPukuEffect()

    // SDF生成用のBitmapステート（TextItemContentと同一の仕組み）
    var fillSdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var strokeSdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var secondBorderSdfBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // TextItemContentと合わせた解像度スケール
    val scaleFactor = 3.0f

    val stickerShaderParams = remember {
        PuffyShaderParams(
            edgeWidthMulti = 30.0f,
            normalZ = 0.4f,
            lightDirZ = 3.0f,
            shininess = 450f,
            specularIntensity = 0.8f,
            blurWeightCenter = 0.3f,
        )
    }

    LaunchedEffect(
        decoration.resId,
        decoration.strokeWidth,
        decoration.secondStrokeWidth,
        shouldRenderPukupuku
    ) {
        if (shouldRenderPukupuku) {
            val fillMaskBitmap = createStickerMaskBitmap(
                imageVector = imageVector,
                density = density,
                boxSize = boxSize,
                scaleFactor = scaleFactor,
                innerScale = innerScale,
                strokeWidth = strokeWidth,
                secondStrokeWidth = secondStrokeWidth,
                targetLayer = StickerLayer.FILL
            )
            fillSdfBitmap = generateSdfTexture(fillMaskBitmap)

            if (decoration.strokeWidth > 0f) {
                val strokeMaskBitmap = createStickerMaskBitmap(
                    imageVector = imageVector,
                    density = density,
                    boxSize = boxSize,
                    scaleFactor = scaleFactor,
                    innerScale = innerScale,
                    strokeWidth = strokeWidth,
                    secondStrokeWidth = secondStrokeWidth,
                    targetLayer = StickerLayer.STROKE
                )
                strokeSdfBitmap = generateSdfTexture(strokeMaskBitmap)
            } else {
                strokeSdfBitmap = null
            }

            if (decoration.secondStrokeWidth > 0f) {
                val secondBorderMaskBitmap = createStickerMaskBitmap(
                    imageVector = imageVector,
                    density = density,
                    boxSize = boxSize,
                    scaleFactor = scaleFactor,
                    innerScale = innerScale,
                    strokeWidth = strokeWidth,
                    secondStrokeWidth = secondStrokeWidth,
                    targetLayer = StickerLayer.SECOND_STROKE
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
        modifier = modifier.size(
            with(density) { intrinsicSize.width.toDp() },
            with(density) { intrinsicSize.height.toDp() }
        )
    ) {
        // 通常描画（ぷくぷくOFF時、またはSDF生成完了までのフォールバック）
        Canvas(modifier = Modifier.matchParentSize()) {
            val isHardware = drawContext.canvas.nativeCanvas.isHardwareAccelerated
            val adjustedStrokeWidth = strokeWidth / innerScale
            val adjustedSecondStrokeWidth = secondStrokeWidth / innerScale

            if (adjustedSecondStrokeWidth > 0f && (!shouldRenderPukupuku || secondBorderSdfBitmap == null || !isHardware)) {
                drawStickerLayer(
                    imageVector,
                    innerScale,
                    secondStrokeColor,
                    adjustedStrokeWidth + adjustedSecondStrokeWidth,
                    DrawMode.StrokeOnly,
                    BlendMode.SrcOver
                )
            }

            if (adjustedStrokeWidth > 0f && (!shouldRenderPukupuku || strokeSdfBitmap == null || !isHardware)) {
                drawStickerLayer(
                    imageVector, innerScale, strokeColor,
                    adjustedStrokeWidth, DrawMode.StrokeOnly, BlendMode.SrcOver
                )
            }

            if (!shouldRenderPukupuku || fillSdfBitmap == null || !isHardware) {
                drawStickerLayer(
                    imageVector, innerScale, fillColor,
                    0f, DrawMode.FillOnly, BlendMode.SrcOver
                )
            }
        }

        // ぷくぷく描画（SDFとPuffyTextRendererの適用）
        if (shouldRenderPukupuku) {
            if (secondBorderSdfBitmap != null) {
                PuffyTextRenderer(
                    sdfTextureBitmap = secondBorderSdfBitmap!!,
                    baseColor = secondStrokeColor,
                    scaleFactor = scaleFactor,
                    modifier = Modifier.matchParentSize(),
                    shaderParams = stickerShaderParams
                )
            }
            if (strokeSdfBitmap != null) {
                PuffyTextRenderer(
                    sdfTextureBitmap = strokeSdfBitmap!!,
                    baseColor = strokeColor,
                    scaleFactor = scaleFactor,
                    modifier = Modifier.matchParentSize(),
                    shaderParams = stickerShaderParams
                )
            }
            if (fillSdfBitmap != null) {
                PuffyTextRenderer(
                    sdfTextureBitmap = fillSdfBitmap!!,
                    baseColor = fillColor,
                    scaleFactor = scaleFactor,
                    modifier = Modifier.matchParentSize(),
                    shaderParams = stickerShaderParams
                )
            }
        }
    }
}

// --- Mask Generation Utilities ---

private enum class StickerLayer { FILL, STROKE, SECOND_STROKE }
private enum class DrawMode { StrokeOnly, FillOnly }

/**
 * ぷくぷくのSDF生成に必要な、レイヤーごとのマスク画像(Bitmap)を生成します。
 * TextItemContentの createTextMaskBitmap に相当する処理です。
 */
private fun createStickerMaskBitmap(
    imageVector: ImageVector,
    density: Density,
    boxSize: Size,
    scaleFactor: Float,
    innerScale: Float,
    strokeWidth: Float,
    secondStrokeWidth: Float,
    targetLayer: StickerLayer
): Bitmap {
    val scaledWidth = (boxSize.width * scaleFactor).toInt().coerceAtLeast(1)
    val scaledHeight = (boxSize.height * scaleFactor).toInt().coerceAtLeast(1)

    val imageBitmap = ImageBitmap(scaledWidth, scaledHeight)
    val composeCanvas = androidx.compose.ui.graphics.Canvas(imageBitmap)

    CanvasDrawScope().draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = composeCanvas,
        size = Size(scaledWidth.toFloat(), scaledHeight.toFloat())
    ) {
        val adjustedStrokeWidth = strokeWidth / innerScale
        val adjustedSecondStrokeWidth = secondStrokeWidth / innerScale
        val combinedStrokeWidth = adjustedStrokeWidth + adjustedSecondStrokeWidth

        when (targetLayer) {
            StickerLayer.FILL -> {
                drawStickerLayer(
                    imageVector,
                    innerScale,
                    Color.White,
                    0f,
                    DrawMode.FillOnly,
                    BlendMode.SrcOver
                )
            }

            StickerLayer.STROKE -> {
                // 枠線を白で描画し、SDFが内側に滲まないよう内側(Fill)をくり抜く
                drawStickerLayer(
                    imageVector,
                    innerScale,
                    Color.White,
                    adjustedStrokeWidth,
                    DrawMode.StrokeOnly,
                    BlendMode.SrcOver
                )
                drawStickerLayer(
                    imageVector,
                    innerScale,
                    Color.Black,
                    0f,
                    DrawMode.FillOnly,
                    BlendMode.Clear
                )
            }

            StickerLayer.SECOND_STROKE -> {
                // 外枠線を白で描画し、内側の要素をくり抜く
                drawStickerLayer(
                    imageVector,
                    innerScale,
                    Color.White,
                    combinedStrokeWidth,
                    DrawMode.StrokeOnly,
                    BlendMode.SrcOver
                )
                drawStickerLayer(
                    imageVector,
                    innerScale,
                    Color.Black,
                    adjustedStrokeWidth,
                    DrawMode.StrokeOnly,
                    BlendMode.Clear
                )
                drawStickerLayer(
                    imageVector,
                    innerScale,
                    Color.Black,
                    0f,
                    DrawMode.FillOnly,
                    BlendMode.Clear
                )
            }
        }
    }
    return imageBitmap.asAndroidBitmap()
}

/**
 * 指定された条件でImageVectorの単一レイヤーをCanvasに描画します。
 */
private fun DrawScope.drawStickerLayer(
    imageVector: ImageVector,
    innerScale: Float,
    color: Color,
    strokeWidth: Float,
    drawMode: DrawMode,
    blendMode: BlendMode
) {
    val scaleX = size.width / imageVector.viewportWidth
    val scaleY = size.height / imageVector.viewportHeight
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    withTransform({
        translate(centerX, centerY)
        scale(innerScale, innerScale, pivot = Offset.Zero)
        translate(-centerX, -centerY)
        scale(scaleX, scaleY, pivot = Offset.Zero)
    }) {
        drawVectorSubtree(imageVector.root, color, strokeWidth, drawMode, blendMode)
    }
}

/**
 * VectorGroupを再帰的に走査して描画する
 */
private fun DrawScope.drawVectorSubtree(
    group: VectorGroup,
    color: Color,
    strokeWidth: Float,
    drawMode: DrawMode,
    blendMode: BlendMode
) {
    withTransform({
        translate(group.translationX, group.translationY)
        rotate(group.rotation, pivot = Offset(group.pivotX, group.pivotY))
        scale(group.scaleX, group.scaleY, pivot = Offset(group.pivotX, group.pivotY))
    }) {
        for (i in 0 until group.size) {
            when (val node = group[i]) {
                is VectorPath -> drawVectorPath(node, color, strokeWidth, drawMode, blendMode)
                is VectorGroup -> drawVectorSubtree(node, color, strokeWidth, drawMode, blendMode)
            }
        }
    }
}

/**
 * VectorPathを描画する
 */
private fun DrawScope.drawVectorPath(
    path: VectorPath,
    color: Color,
    strokeWidth: Float,
    drawMode: DrawMode,
    blendMode: BlendMode
) {
    val androidPath = Path()
    PathParser().addPathNodes(path.pathData).toPath(androidPath)

    when (drawMode) {
        DrawMode.StrokeOnly -> {
            if (strokeWidth > 0f) {
                drawPath(
                    path = androidPath,
                    color = color,
                    style = Stroke(width = strokeWidth, join = StrokeJoin.Round),
                    blendMode = blendMode
                )
            }
        }

        DrawMode.FillOnly -> {
            drawPath(
                path = androidPath,
                color = color,
                style = Fill,
                blendMode = blendMode
            )
        }
    }
}

// region StickerItemContent Previews

private val previewStickerDecoration = Decoration.Sticker(
    id = "preview-sticker-1",
    label = StickerAsset.HEART.type,
    color = Color.Magenta,
    strokeColor = Color.White,
    strokeWidth = 3f,
    secondStrokeColor = Color.White,
    secondStrokeWidth = 0f,
)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun StickerItemContentPreview() {
    FansaUchiwaTheme {
        StickerItemContent(
            decoration = previewStickerDecoration,
            modifier = Modifier,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun StickerItemContentWithSecondStrokePreview() {
    FansaUchiwaTheme {
        StickerItemContent(
            decoration = previewStickerDecoration.copy(
                isPukupuku = true,
                secondStrokeColor = Color.Cyan,
                secondStrokeWidth = 6f,
            ),
            modifier = Modifier,
        )
    }
}

// endregion