package com.fansauchiwa.edit.decorationitem

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.SizeResolver
import com.fansauchiwa.data.Decoration

/**
 * ImageデコレーションのコンテンツをAsyncImageで描画するComposable
 *
 * @param decoration 描画対象のImageデコレーション
 * @param imagePath 画像ファイルのパス
 * @param size 画像の表示サイズ
 * @param modifier Modifier
 * @param isSelected 選択状態かどうか（BlendModeに影響）
 */
@Composable
fun ImageItemContent(
    decoration: Decoration.Image,
    imagePath: String?,
    size: Dp,
    modifier: Modifier,
    isSelected: Boolean
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imagePath)
            .size(SizeResolver.ORIGINAL)
            .allowHardware(false)
            .build(),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .drawWithContent {
                drawContext.canvas.withSaveLayer(
                    bounds = this.size.toRect(),
                    paint = Paint().apply {
                        blendMode = if (!isSelected) BlendMode.SrcAtop else BlendMode.SrcOver
                    }
                ) {
                    drawContent()
                }
            }
    )
}
