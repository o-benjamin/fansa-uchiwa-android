package com.fansauchiwa.edit.decorationitem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import com.fansauchiwa.data.Decoration

/**
 * StickerのコンテンツをCanvasを使って描画するComposable
 *
 * @param decoration 描画対象のStickerデコレーション
 * @param modifier Modifier
 * @param isSelected 選択状態かどうか（BlendModeに影響）
 */
@Composable
fun StickerItemContent(
    decoration: Decoration.Sticker,
    modifier: Modifier,
    isSelected: Boolean
) {
    if (decoration.resId == 0) return

    val imageVector = ImageVector.vectorResource(id = decoration.resId)
    val painter = rememberVectorPainter(image = imageVector)
    val fillColor = decoration.color
    val strokeColor = decoration.strokeColor
    val strokeWidth = decoration.strokeWidth
    val secondStrokeColor = decoration.secondStrokeColor
    val secondStrokeWidth = decoration.secondStrokeWidth

    // intrinsicSizeを基準に、strokeWidth分を考慮した内部スケールを計算
    val intrinsicSize = painter.intrinsicSize
    // 枠線1と枠線2の合計太さを考慮したスケール計算
    val totalStrokeWidth = strokeWidth + secondStrokeWidth
    val innerScaleX = if (intrinsicSize.width > totalStrokeWidth * 2) {
        (intrinsicSize.width - totalStrokeWidth * 10) / intrinsicSize.width
    } else {
        0.1f // 最小スケール
    }
    val innerScaleY = if (intrinsicSize.height > totalStrokeWidth * 2) {
        (intrinsicSize.height - totalStrokeWidth * 10) / intrinsicSize.height
    } else {
        0.1f // 最小スケール
    }
    val innerScale = minOf(innerScaleX, innerScaleY)

    Canvas(
        modifier = modifier
            .size(
                with(LocalDensity.current) { intrinsicSize.width.toDp() },
                with(LocalDensity.current) { intrinsicSize.height.toDp() }
            )
            .drawWithCache {
                onDrawWithContent {
                    // 選択されていない場合はうちわ形状でクリップ
                    drawContext.canvas.withSaveLayer(
                        bounds = size.toRect(),
                        paint = Paint().apply {
                            blendMode =
                                if (!isSelected) BlendMode.SrcAtop else BlendMode.SrcOver
                        }
                    ) {
                        drawStickerWithStrokeScaled(
                            imageVector = imageVector,
                            fillColor = fillColor,
                            strokeColor = strokeColor,
                            strokeWidth = strokeWidth,
                            innerScale = innerScale,
                            secondStrokeColor = secondStrokeColor,
                            secondStrokeWidth = secondStrokeWidth
                        )
                    }
                }
            }
    ) {
        // 描画はdrawWithCache内で行う
    }
}

/**
 * 描画モードを定義する列挙型
 * - StrokeOnly: 枠線のみを描画
 * - FillOnly: 塗りつぶしのみを描画
 */
private enum class DrawMode { StrokeOnly, FillOnly }

/**
 * ImageVectorを枠線付きで描画する
 * strokeWidthが大きくなっても外形サイズが変わらないように内部画像を縮小して描画する
 *
 * @param imageVector 描画対象のImageVector
 * @param fillColor 塗りつぶし色
 * @param strokeColor 枠線の色（枠線1）
 * @param strokeWidth 枠線の太さ（枠線1）
 * @param innerScale 内部画像のスケール（0.0〜1.0）
 * @param secondStrokeColor 二つ目の枠線の色（枠線2）
 * @param secondStrokeWidth 二つ目の枠線の太さ（枠線2）
 */
private fun DrawScope.drawStickerWithStrokeScaled(
    imageVector: ImageVector,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float,
    innerScale: Float,
    secondStrokeColor: Color,
    secondStrokeWidth: Float
) {
    // viewport と canvas サイズの比率を計算
    val scaleX = size.width / imageVector.viewportWidth
    val scaleY = size.height / imageVector.viewportHeight

    // 中心を基点にスケーリングするための平行移動量を計算
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    withTransform({
        // 中心を基点にinnerScaleを適用
        translate(centerX, centerY)
        scale(innerScale, innerScale, pivot = Offset.Zero)
        translate(-centerX, -centerY)
        // 元のviewport→canvasスケーリング
        scale(scaleX, scaleY, pivot = Offset.Zero)
    }) {
        // strokeWidthもinnerScaleに合わせて調整（見た目の太さを維持）
        val adjustedStrokeWidth = strokeWidth / innerScale
        val adjustedSecondStrokeWidth = secondStrokeWidth / innerScale

        // パス1: 枠線2（最背面）- 二つ目の枠線を描画（secondStrokeWidthが0より大きい場合のみ）
        if (adjustedSecondStrokeWidth > 0f) {
            val combinedStrokeWidth = adjustedStrokeWidth + adjustedSecondStrokeWidth
            drawVectorSubtree(
                imageVector.root,
                fillColor,
                secondStrokeColor,
                combinedStrokeWidth,
                DrawMode.StrokeOnly
            )
        }

        // パス2: 枠線1（中間）- 一つ目の枠線を描画（strokeWidthが0より大きい場合のみ）
        if (adjustedStrokeWidth > 0f) {
            drawVectorSubtree(
                imageVector.root,
                fillColor,
                strokeColor,
                adjustedStrokeWidth,
                DrawMode.StrokeOnly
            )
        }

        // パス3: 塗りつぶし（最前面）
        drawVectorSubtree(
            imageVector.root,
            fillColor,
            strokeColor,
            adjustedStrokeWidth,
            DrawMode.FillOnly
        )
    }
}

/**
 * VectorGroupを再帰的に走査して描画する
 * @param drawMode 描画モード（枠線のみ、または塗りつぶしのみ）
 */
private fun DrawScope.drawVectorSubtree(
    group: VectorGroup,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float,
    drawMode: DrawMode
) {
    withTransform({
        // VectorGroupのtransformを適用
        translate(group.translationX, group.translationY)
        rotate(group.rotation, pivot = Offset(group.pivotX, group.pivotY))
        scale(group.scaleX, group.scaleY, pivot = Offset(group.pivotX, group.pivotY))
    }) {
        // グループ内の各要素を処理
        for (i in 0 until group.size) {
            when (val node = group[i]) {
                is VectorPath -> {
                    drawVectorPath(node, fillColor, strokeColor, strokeWidth, drawMode)
                }

                is VectorGroup -> {
                    drawVectorSubtree(node, fillColor, strokeColor, strokeWidth, drawMode)
                }
            }
        }
    }
}

/**
 * VectorPathを描画する
 * @param drawMode 描画モード（枠線のみ、または塗りつぶしのみ）
 */
private fun DrawScope.drawVectorPath(
    path: VectorPath,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float,
    drawMode: DrawMode
) {
    val pathData = path.pathData
    val androidPath = Path()
    PathParser().addPathNodes(pathData).toPath(androidPath)

    when (drawMode) {
        DrawMode.StrokeOnly -> {
            // 枠線を描画（strokeWidthが0より大きい場合のみ）
            if (strokeWidth > 0f) {
                drawPath(
                    path = androidPath,
                    color = strokeColor,
                    style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
                )
            }
        }

        DrawMode.FillOnly -> {
            // 塗りつぶしを描画
            drawPath(
                path = androidPath,
                color = fillColor,
                style = Fill
            )
        }
    }
}

