package com.fansauchiwa.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.fansauchiwa.data.Transformation
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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
internal fun DrawScope.drawStickerWithStrokeScaled(
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

internal fun calculateTransformations(
    cumulativeOffset: Offset,
    dragStartPosition: Offset,
): Transformation {
    // ドラッグしている場所と要素の中心との距離からScaleを計算
    val initialDistance = dragStartPosition.getDistance()
    val currentDistance = (dragStartPosition + cumulativeOffset).getDistance()
    val distanceChange = currentDistance - initialDistance
    val scaleFactor = 0.005f
    val scaleDiff = distanceChange * scaleFactor

    // ドラッグしている場所と要素の中心との角度からRotationを計算
    val initialAngle = dragStartPosition.toAngleDegrees()
    val currentPosition = dragStartPosition + cumulativeOffset
    val currentAngle = currentPosition.toAngleDegrees()
    val rotationDiff = currentAngle - initialAngle

    return Transformation(scaleDiff, rotationDiff)
}


internal fun calculateHandleOffset(
    baseOffset: Offset,
    scale: Float,
    rotation: Float,
    decorationSize: Size,
    corner: HandleCorner,
): Offset {
    val cornerOffset = when (corner) {
        HandleCorner.TopLeft -> Offset(-decorationSize.width / 2f, -decorationSize.height / 2f)
        HandleCorner.TopRight -> Offset(decorationSize.width / 2f, -decorationSize.height / 2f)
        HandleCorner.BottomLeft -> Offset(-decorationSize.width / 2f, decorationSize.height / 2f)
        HandleCorner.BottomRight -> Offset(decorationSize.width / 2f, decorationSize.height / 2f)
    }
    val scaledCornerOffset = cornerOffset * scale
    return baseOffset + scaledCornerOffset.rotateBy(rotation)
}

internal fun rotatedDragAmount(
    currentRotation: Float,
    currentScale: Float,
    dragAmount: Offset,
): Offset {
    val scaledDragAmount = dragAmount * currentScale
    return scaledDragAmount.rotateBy(currentRotation)
}

internal fun Offset.toAngleDegrees(): Float {
    return atan2(y, x) * 180f / PI.toFloat()
}

internal fun Offset.rotateBy(degrees: Float): Offset {
    val angleRad = Math.toRadians(degrees.toDouble()).toFloat()
    val cos = cos(angleRad)
    val sin = sin(angleRad)
    return Offset(
        x = this.x * cos - this.y * sin,
        y = this.x * sin + this.y * cos
    )
}

internal val TextUnit.nonScaledSp: TextUnit
    @Composable
    get() = (value / LocalDensity.current.fontScale).sp

/**
 * デコレーションアイテムの移動量を境界内に制限する
 *
 * @param currentConfirmedOffset 現在の確定座標
 * @param cumulativeOffset 累積の移動量
 * @param dragAmount 今回のドラッグ量
 * @param boundarySize 境界サイズ（うちわ画像のサイズ）
 * @return 制限を適用した後の新しい累積の移動量
 */
internal fun calculateClampedOffset(
    currentConfirmedOffset: Offset,
    cumulativeOffset: Offset,
    dragAmount: Offset,
    boundarySize: IntSize?
): Offset {
    if (boundarySize == null || boundarySize.width == 0 || boundarySize.height == 0) {
        return cumulativeOffset + dragAmount
    }

    // 新しい位置を計算
    val newCumulativeOffset = cumulativeOffset + dragAmount
    val newPosition = currentConfirmedOffset + newCumulativeOffset

    // 境界範囲を計算（中心が原点なので、-width/2 から width/2 まで）
    val halfWidth = (boundarySize.width / 2f) * 0.7f
    val halfHeight = (boundarySize.height / 2f) * 0.9f

    // 新しい位置を境界内に制限
    val clampedX = newPosition.x.coerceIn(-halfWidth, halfWidth)
    val clampedY = newPosition.y.coerceIn(-halfHeight, halfHeight)

    // 制限された位置から累積の移動量を逆算
    return Offset(
        x = clampedX - currentConfirmedOffset.x,
        y = clampedY - currentConfirmedOffset.y
    )
}
