package com.fansauchiwa.ui.util

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Canvas
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativeCanvas as AndroidCanvas
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import android.graphics.Color as AndroidColor
import androidx.core.graphics.createBitmap

const val OVERALL_BORDER_BLUR_RADIUS_MULTIPLIER = 3f

fun captureGraphicsLayerBitmap(
    graphicsLayer: GraphicsLayer,
    density: Density,
    layoutDirection: LayoutDirection,
    targetSize: IntSize
): Bitmap {
    val safeWidth = targetSize.width.coerceAtLeast(1)
    val safeHeight = targetSize.height.coerceAtLeast(1)
    val bitmap = createBitmap(safeWidth, safeHeight)
    val canvas = ComposeCanvas(AndroidCanvas(bitmap))
    CanvasDrawScope().draw(
        density = density,
        layoutDirection = layoutDirection,
        canvas = canvas,
        size = Size(safeWidth.toFloat(), safeHeight.toFloat())
    ) {
        drawLayer(graphicsLayer)
    }
    return bitmap
}

fun createOverallBorderMaskBitmap(
    sourceBitmap: Bitmap,
    overallBorderWidth: Float
): Bitmap? {
    val blurPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        maskFilter = BlurMaskFilter(
            (overallBorderWidth * OVERALL_BORDER_BLUR_RADIUS_MULTIPLIER).coerceAtLeast(1f),
            BlurMaskFilter.Blur.NORMAL
        )
    }
    val offset = IntArray(2)
    val blurredAlphaBitmap = sourceBitmap.extractAlpha(blurPaint, offset) ?: return null
    val maskBitmap = createBitmap(sourceBitmap.width, sourceBitmap.height)
    val canvas = Canvas(maskBitmap)
    val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(createOverallBorderMaskColorMatrix())
    }
    canvas.drawBitmap(blurredAlphaBitmap, offset[0].toFloat(), offset[1].toFloat(), maskPaint)

    val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OUT)
    }
    canvas.drawBitmap(sourceBitmap, 0f, 0f, clearPaint)

    blurredAlphaBitmap.recycle()
    return maskBitmap
}

fun createOverallBorderBitmap(
    maskBitmap: Bitmap,
    borderColor: Color
): Bitmap {
    val coloredBitmap = createBitmap(maskBitmap.width, maskBitmap.height)
    val androidColor = borderColor.toArgb()
    val colorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(createOverallBorderColorMatrix(androidColor))
    }
    Canvas(coloredBitmap).drawBitmap(maskBitmap, 0f, 0f, colorPaint)
    return coloredBitmap
}

fun createOverallBorderMaskColorMatrix(): ColorMatrix = ColorMatrix(
    floatArrayOf(
        0f, 0f, 0f, 0f, 255f,
        0f, 0f, 0f, 0f, 255f,
        0f, 0f, 0f, 0f, 255f,
        0f, 0f, 0f, 255f, 0f
    )
)

fun createOverallBorderColorMatrix(androidColor: Int): ColorMatrix = ColorMatrix(
    floatArrayOf(
        0f, 0f, 0f, 0f, AndroidColor.red(androidColor).toFloat(),
        0f, 0f, 0f, 0f, AndroidColor.green(androidColor).toFloat(),
        0f, 0f, 0f, 0f, AndroidColor.blue(androidColor).toFloat(),
        0f, 0f, 0f, 1f, 0f
    )
)
