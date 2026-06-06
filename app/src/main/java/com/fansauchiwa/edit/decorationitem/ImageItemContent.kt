package com.fansauchiwa.edit.decorationitem

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.SizeResolver
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

/**
 * ImageデコレーションのコンテンツをAsyncImageで描画するComposable
 *
 * @param decoration 描画対象のImageデコレーション
 * @param imagePath 画像ファイルのパス
 * @param size 画像の表示サイズ
 * @param modifier Modifier
 */
@Composable
fun ImageItemContent(
    decoration: Decoration.Image,
    imagePath: String?,
    size: Dp,
    modifier: Modifier,
    colorFilter: ColorFilter? = null
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imagePath)
            .size(SizeResolver.ORIGINAL)
            .allowHardware(false)
            .build(),
        contentDescription = null,
        colorFilter = colorFilter,
        modifier = modifier
            .size(size)
    )
}

// region ImageItemContent Previews

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ImageItemContentPreview() {
    FansaUchiwaTheme {
        ImageItemContent(
            decoration = Decoration.Image(
                id = "preview-image-1",
                imageId = "preview-image-id"
            ),
            imagePath = null,
            size = 120.dp,
            modifier = Modifier,
        )
    }
}

// endregion
