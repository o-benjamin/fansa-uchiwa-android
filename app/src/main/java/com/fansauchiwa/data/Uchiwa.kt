package com.fansauchiwa.data

import androidx.compose.ui.graphics.Color

data class Uchiwa(
    val id: String,
    val decorations: List<Decoration>,
    val uchiwaColor: Color,
    val backgroundColor: Color
)

