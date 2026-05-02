package com.fansauchiwa.data

import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.graphics.HardwareRenderer
import android.graphics.PixelFormat
import android.graphics.RenderNode
import android.hardware.HardwareBuffer
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * GraphicsLayerの内容を指定された解像度のBitmapとして描画して返します。
 */
suspend fun captureHighResBitmap(
    graphicsLayer: GraphicsLayer,
    density: Density,
    layoutDirection: LayoutDirection,
    targetWidth: Int = 2382,
    targetHeight: Int = 1684
): ImageBitmap {
    val layerSize = graphicsLayer.size
    val scaleX = targetWidth.toFloat() / layerSize.width
    val scaleY = targetHeight.toFloat() / layerSize.height

    val format = PixelFormat.RGBA_8888
    val usage = HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
    val reader = ImageReader.newInstance(targetWidth, targetHeight, format, 1, usage)

    val renderNode = RenderNode("HighResUchiwa")
    renderNode.setPosition(0, 0, targetWidth, targetHeight)

    val androidCanvas = renderNode.beginRecording()
    val composeCanvas = Canvas(androidCanvas)

    CanvasDrawScope().draw(
        density = density,
        layoutDirection = layoutDirection,
        canvas = composeCanvas,
        size = Size(targetWidth.toFloat(), targetHeight.toFloat())
    ) {
        // スケールを適用して描画
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
            drawLayer(graphicsLayer)
        }
    }
    renderNode.endRecording()

    val renderer = HardwareRenderer()
    renderer.setContentRoot(renderNode)
    renderer.setSurface(reader.surface)

    val image = suspendCancellableCoroutine { cont ->
        reader.setOnImageAvailableListener({ ir ->
            reader.setOnImageAvailableListener(null, null)
            val img = ir.acquireNextImage()
            if (img != null) {
                cont.resume(img)
            } else {
                cont.cancel(IllegalStateException("Image is null"))
            }
        }, Handler(Looper.getMainLooper()))

        renderer.createRenderRequest().syncAndDraw()
    }

    val hwBuffer = image.hardwareBuffer
    val bitmap = if (hwBuffer != null) {
        Bitmap.wrapHardwareBuffer(hwBuffer, ColorSpace.get(ColorSpace.Named.SRGB))
            ?.copy(Bitmap.Config.ARGB_8888, false)
    } else {
        null
    }

    image.close()
    reader.close()
    renderer.destroy()

    if (bitmap != null) {
        return bitmap.asImageBitmap()
    }

    // Hardware buffer failed fallback
    val fallbackBitmap = createBitmap(targetWidth, targetHeight).asImageBitmap()
    val fallbackCanvas = Canvas(fallbackBitmap)

    CanvasDrawScope().draw(
        density = density,
        layoutDirection = layoutDirection,
        canvas = fallbackCanvas,
        size = Size(targetWidth.toFloat(), targetHeight.toFloat())
    ) {
        // スケールを適用して描画
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
            drawLayer(graphicsLayer)
        }
    }

    return fallbackBitmap
}