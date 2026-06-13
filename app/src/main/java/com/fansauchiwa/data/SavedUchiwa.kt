package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color

data class SavedUchiwa(
    val decorations: List<Decoration>,
    val uchiwaColor: Color,
    val backgroundColor: Color,
    val overallBorderColor: Color = Color(0xFFFFFFFF),
    val overallBorderWidth: Float = 0f,
    val isOverallBorderPuffyEnabled: Boolean = false
)
