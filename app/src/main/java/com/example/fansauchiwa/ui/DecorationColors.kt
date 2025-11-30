package com.example.fansauchiwa.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.fansauchiwa.R

enum class DecorationColors(val colorResId: Int) {
    CYAN(R.color.decoration_black),
    RED(R.color.decoration_red),
    MAGENTA(R.color.decoration_magenta),
    BLUE(R.color.decoration_blue),
    GREEN(R.color.decoration_green),
    YELLOW(R.color.decoration_yellow),
    WHITE(R.color.decoration_white),
}

@Composable
fun DecorationColors.getColor(): Color {
    return colorResource(id = colorResId)
}

