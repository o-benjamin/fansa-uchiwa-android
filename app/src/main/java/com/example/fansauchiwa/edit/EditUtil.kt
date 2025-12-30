package com.example.fansauchiwa.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.fansauchiwa.data.Transformation
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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