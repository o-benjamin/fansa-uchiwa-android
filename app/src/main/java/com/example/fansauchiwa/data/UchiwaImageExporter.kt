package com.example.fansauchiwa.data

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
    // 1. 指定サイズのAndroid Bitmapを作成 (ARGB_8888で高品質に)
    val bitmap = createBitmap(targetWidth, targetHeight).asImageBitmap()

    // 2. ComposeのCanvasでラップ
    val canvas = Canvas(bitmap)

    // 3. 現在のGraphicsLayerのサイズを取得
    val layerSize = graphicsLayer.size

    val scaleX = targetWidth.toFloat() / layerSize.width
    val scaleY = targetHeight.toFloat() / layerSize.height

    // 5. CanvasDrawScopeを使って描画
    CanvasDrawScope().draw(
        density = density,
        layoutDirection = layoutDirection,
        canvas = canvas,
        size = Size(targetWidth.toFloat(), targetHeight.toFloat())
    ) {
        // スケールを適用して描画
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
            drawLayer(graphicsLayer)
        }
    }

    return bitmap
}