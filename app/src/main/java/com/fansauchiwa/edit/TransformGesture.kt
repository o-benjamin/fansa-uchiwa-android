package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset

sealed interface TransformGesture {
    data class HandleDrag(
        val dragAmount: Offset
    ) : TransformGesture

    data class DirectManipulation(
        val zoomChange: Float,
        val rotationChange: Float
    ) : TransformGesture
}
