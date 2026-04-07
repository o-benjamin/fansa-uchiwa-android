package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.fansauchiwa.data.UchiwaShapeSpec

class UchiwaShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = PathParser().parsePathString(UchiwaShapeSpec.PATH_DATA).toPath()

        // 1. Imageの ContentScale.Fit と同じように、枠に収まる最大のスケール（倍率）を計算する
        val defaultSize = size * 0.95f
        val scaleX = defaultSize.width / UchiwaShapeSpec.VIEWPORT_WIDTH
        val scaleY = defaultSize.height / UchiwaShapeSpec.VIEWPORT_HEIGHT
        val scale = minOf(scaleX, scaleY)

        // 2. スケール適用後の実際のパスのサイズを計算する
        val scaledWidth = UchiwaShapeSpec.VIEWPORT_WIDTH * scale
        val scaledHeight = UchiwaShapeSpec.VIEWPORT_HEIGHT * scale

        // 3. 中央に配置するためのオフセット（余白の半分）を計算する
        val offsetX = (size.width - scaledWidth) / 2f
        val offsetY = (size.height - scaledHeight) / 2f

        // 4. パスにスケール（拡大縮小）を適用
        val scaleMatrix = Matrix()
        scaleMatrix.scale(x = scale, y = scale)
        path.transform(scaleMatrix)

        // 5. パスにオフセット（平行移動）を適用して中央揃えにする
        val translateMatrix = Matrix()
        translateMatrix.translate(x = offsetX, y = offsetY)
        path.transform(translateMatrix)

        return Outline.Generic(path)
    }
}
