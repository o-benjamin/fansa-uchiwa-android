package com.fansauchiwa.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.fansauchiwa.data.Transformation
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * デコレーションの移動量に対してキャンバス中央（X=0, Y=0）へのスナップ判定・補正を行う
 *
 * @param decorationOffset デコレーションの確定済み座標
 * @param offsetDiff 現在の移動量（calculateClampedOffset適用後）
 * @param snapThreshold スナップ判定の閾値（px）
 * @return スナップ補正後の結果
 */
internal fun applySnapToCenter(
    decorationOffset: Offset,
    offsetDiff: Offset,
    snapThreshold: Float
): SnapResult {
    val newX = decorationOffset.x + offsetDiff.x
    val newY = decorationOffset.y + offsetDiff.y

    val snappedX = abs(newX) <= snapThreshold
    val snappedY = abs(newY) <= snapThreshold

    val snappedOffsetDiff = Offset(
        x = if (snappedX) -decorationOffset.x else offsetDiff.x,
        y = if (snappedY) -decorationOffset.y else offsetDiff.y
    )

    return SnapResult(
        offsetDiff = snappedOffsetDiff,
        snappedX = snappedX,
        snappedY = snappedY
    )
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
