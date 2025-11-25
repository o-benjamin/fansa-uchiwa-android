package com.example.fansauchiwa.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.fansauchiwa.R

enum class DecorationColors(val colorResId: Int) {
    RED(R.color.decoration_red),
    BLUE(R.color.decoration_blue),
    GREEN(R.color.decoration_green),
    YELLOW(R.color.decoration_yellow),
    MAGENTA(R.color.decoration_magenta),
    CYAN(R.color.decoration_cyan),
    GRAY(R.color.decoration_gray),
}

@Composable
fun DecorationColors.getColor(): Color {
    return colorResource(id = colorResId)
}

