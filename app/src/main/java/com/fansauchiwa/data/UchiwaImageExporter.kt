package com.fansauchiwa.data

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

private const val DEFAULT_EXPORT_WIDTH = 2382
private const val DEFAULT_EXPORT_HEIGHT = 1684

/**
 * GraphicsLayerの内容を指定された解像度のBitmapとして描画して返します。
 */
suspend fun captureHighResBitmap(
    graphicsLayer: GraphicsLayer,
    density: Density,
    layoutDirection: LayoutDirection,
    targetWidth: Int = DEFAULT_EXPORT_WIDTH,
    targetHeight: Int = DEFAULT_EXPORT_HEIGHT
): ImageBitmap {
    return UchiwaImageExporter.export(
        graphicsLayer = graphicsLayer,
        density = density,
        layoutDirection = layoutDirection,
        targetWidth = targetWidth,
        targetHeight = targetHeight
    )
}

private object UchiwaImageExporter {

    fun export(
        graphicsLayer: GraphicsLayer,
        density: Density,
        layoutDirection: LayoutDirection,
        targetWidth: Int,
        targetHeight: Int
    ): ImageBitmap {
        val renderPlan = createUchiwaImageRenderPlan(
            layerWidth = graphicsLayer.size.width,
            layerHeight = graphicsLayer.size.height,
            targetWidth = targetWidth,
            targetHeight = targetHeight
        )
        val bitmap = createExportBitmap(targetWidth = targetWidth, targetHeight = targetHeight)

        drawGraphicsLayer(
            bitmap = bitmap,
            graphicsLayer = graphicsLayer,
            density = density,
            layoutDirection = layoutDirection,
            renderPlan = renderPlan
        )

        return bitmap
    }

    private fun createExportBitmap(
        targetWidth: Int,
        targetHeight: Int
    ): ImageBitmap {
        validateBitmapSize(targetWidth = targetWidth, targetHeight = targetHeight)
        return createBitmap(targetWidth, targetHeight).asImageBitmap()
    }

    private fun drawGraphicsLayer(
        bitmap: ImageBitmap,
        graphicsLayer: GraphicsLayer,
        density: Density,
        layoutDirection: LayoutDirection,
        renderPlan: UchiwaImageRenderPlan
    ) {
        CanvasDrawScope().draw(
            density = density,
            layoutDirection = layoutDirection,
            canvas = Canvas(bitmap),
            size = Size(renderPlan.targetWidth.toFloat(), renderPlan.targetHeight.toFloat())
        ) {
            scale(
                scaleX = renderPlan.scaleX,
                scaleY = renderPlan.scaleY,
                pivot = Offset.Zero
            ) {
                drawLayer(graphicsLayer)
            }
        }
    }
}

internal fun createUchiwaImageRenderPlan(
    layerWidth: Float,
    layerHeight: Float,
    targetWidth: Int,
    targetHeight: Int
): UchiwaImageRenderPlan {
    require(layerWidth > 0f) { "layerWidth must be greater than 0." }
    require(layerHeight > 0f) { "layerHeight must be greater than 0." }
    validateBitmapSize(targetWidth = targetWidth, targetHeight = targetHeight)

    return UchiwaImageRenderPlan(
        targetWidth = targetWidth,
        targetHeight = targetHeight,
        scaleX = targetWidth.toFloat() / layerWidth,
        scaleY = targetHeight.toFloat() / layerHeight
    )
}

internal class UchiwaImageRenderPlan(
    val targetWidth: Int,
    val targetHeight: Int,
    val scaleX: Float,
    val scaleY: Float
)

private fun validateBitmapSize(
    targetWidth: Int,
    targetHeight: Int
) {
    require(targetWidth > 0) { "targetWidth must be greater than 0." }
    require(targetHeight > 0) { "targetHeight must be greater than 0." }
}
