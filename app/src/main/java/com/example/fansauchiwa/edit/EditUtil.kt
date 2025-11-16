package com.example.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.example.fansauchiwa.data.Transformation
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal fun calculateTransformations(
    dragAmount: Offset,
    cumulativeOffset: Offset,
    dragStartPosition: Offset,
): Transformation {
    val totalOffset = cumulativeOffset + dragAmount

    // ドラッグしている場所と要素の中心との距離からScaleを計算
    val initialDistance = dragStartPosition.getDistance()
    val currentDistance = (dragStartPosition + totalOffset).getDistance()
    val distanceChange = currentDistance - initialDistance
    val scaleFactor = 0.005f
    val scaleDiff = distanceChange * scaleFactor

    // ドラッグしている場所と要素の中心との角度からRotationを計算
    val initialAngle = dragStartPosition.toAngleDegrees()
    val currentPosition = dragStartPosition + totalOffset
    val currentAngle = currentPosition.toAngleDegrees()
    val rotationDiff = currentAngle - initialAngle

    return Transformation(scaleDiff, rotationDiff)
}


internal fun calculateHandleSize(
    // TODO: Sizeの計算をするように変更する
    baseOffset: Offset,
    scale: Float,
    rotation: Float,
    size: Size,
    corner: HandleCorner,
): Offset {
    val cornerOffset = when (corner) {
        HandleCorner.TopLeft -> Offset(-size.width / 2f, -size.height / 2f)
        HandleCorner.TopRight -> Offset(size.width / 2f, -size.height / 2f)
        HandleCorner.BottomLeft -> Offset(-size.width / 2f, size.height / 2f)
        HandleCorner.BottomRight -> Offset(size.width / 2f, size.height / 2f)
    }
    val scaledCornerOffset = cornerOffset * scale

    val angleRad = Math.toRadians(rotation.toDouble()).toFloat()
    val cos = cos(angleRad)
    val sin = sin(angleRad)
    val rotatedCornerOffset = Offset(
        x = scaledCornerOffset.x * cos - scaledCornerOffset.y * sin,
        y = scaledCornerOffset.x * sin + scaledCornerOffset.y * cos
    )
    return baseOffset + rotatedCornerOffset
}

internal fun rotatedDragAmount(
    currentRotation: Float,
    currentScale: Float,
    dragAmount: Offset,
): Offset {
    val scaledDragAmount = dragAmount * currentScale
    val angleRad = Math.toRadians(currentRotation.toDouble()).toFloat()
    val cos = cos(angleRad)
    val sin = sin(angleRad)
    val rotatedDragAmount = Offset(
        x = scaledDragAmount.x * cos - scaledDragAmount.y * sin,
        y = scaledDragAmount.x * sin + scaledDragAmount.y * cos
    )
    return rotatedDragAmount
}

internal fun Offset.toAngleDegrees(): Float {
    return atan2(y, x) * 180f / PI.toFloat()
}
