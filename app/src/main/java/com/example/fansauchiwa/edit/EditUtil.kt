package com.example.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal fun calculateHandleOffset(
    baseOffset: Offset,
    scale: Float,
    rotation: Float,
    size: Float,
    corner: HandleCorner,
): Offset {
    val cornerOffset = when (corner) {
        HandleCorner.TopLeft -> Offset(-size / 2f, -size / 2f)
        HandleCorner.TopRight -> Offset(size / 2f, -size / 2f)
        HandleCorner.BottomLeft -> Offset(-size / 2f, size / 2f)
        HandleCorner.BottomRight -> Offset(size / 2f, size / 2f)
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
    dragAmount: Offset,
): Offset {
    val angleRad = Math.toRadians(currentRotation.toDouble()).toFloat()
    val cos = cos(angleRad)
    val sin = sin(angleRad)
    val rotatedDragAmount = Offset(
        x = dragAmount.x * cos - dragAmount.y * sin,
        y = dragAmount.x * sin + dragAmount.y * cos
    )
    return rotatedDragAmount
}

internal fun Offset.toAngleDegrees(): Float {
    return atan2(y, x) * 180f / PI.toFloat()
}
