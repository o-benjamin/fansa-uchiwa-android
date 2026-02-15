package com.fansauchiwa.ui

import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap

@Composable
fun rememberTransparencyGridBrush(
    gridSize: Dp = 10.dp,
    lightColor: Color = Color.White,
    darkColor: Color = Color(0xFFDDDDDD) // 薄いグレー
): ShaderBrush {
    val density = LocalDensity.current

    return remember(gridSize, lightColor, darkColor, density) {
        val sizePx = with(density) { gridSize.toPx().toInt() }
        val bitmapSize = sizePx * 2

        val bitmap = createBitmap(bitmapSize, bitmapSize).apply {
            val canvas = Canvas(this)
            val paint = Paint()

            // 左上
            paint.color = lightColor.toArgb()
            canvas.drawRect(0f, 0f, sizePx.toFloat(), sizePx.toFloat(), paint)
            // 右下
            canvas.drawRect(
                sizePx.toFloat(),
                sizePx.toFloat(),
                bitmapSize.toFloat(),
                bitmapSize.toFloat(),
                paint
            )

            // 右上
            paint.color = darkColor.toArgb()
            canvas.drawRect(sizePx.toFloat(), 0f, bitmapSize.toFloat(), sizePx.toFloat(), paint)
            // 左下
            canvas.drawRect(0f, sizePx.toFloat(), sizePx.toFloat(), bitmapSize.toFloat(), paint)
        }

        ShaderBrush(
            ImageShader(
                bitmap.asImageBitmap(),
                TileMode.Repeated,
                TileMode.Repeated
            )
        )
    }
}

