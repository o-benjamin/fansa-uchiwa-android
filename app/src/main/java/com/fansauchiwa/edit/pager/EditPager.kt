package com.fansauchiwa.edit.pager

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fansauchiwa.R
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.ImageReference
import com.fansauchiwa.edit.ColorAndWeightControl
import com.fansauchiwa.edit.ColorPickerRow
import com.fansauchiwa.edit.DecorationTabType
import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.edit.HeaderTitle
import com.fansauchiwa.edit.ItemBadge
import com.fansauchiwa.edit.buildRankIndexMap
import com.fansauchiwa.edit.decorationitem.ImageItemContent
import com.fansauchiwa.edit.decorationitem.StickerItemContent
import com.fansauchiwa.edit.decorationitem.TextItemContent
import com.fansauchiwa.edit.nonScaledSp
import com.fansauchiwa.ui.DecorationColors
import com.fansauchiwa.ui.StickerAsset
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditPager(
    modifier: Modifier = Modifier,
    onStickerClick: (Decoration.Sticker) -> Unit,
    onTextClick: (Decoration.Text) -> Unit,
    onFontChanged: (FontFamilies) -> Unit,
    onColorSelected: (Color) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onSecondBorderColorSelected: (Color) -> Unit,
    onSecondBorderWeightChanged: (Float) -> Unit,
    onImageClick: (Decoration.Image) -> Unit,
    onImageLongPress: () -> Unit,
    onImagePicked: (Uri) -> Unit,
    onUchiwaColorSelected: (Color) -> Unit,
    onBackgroundColorSelected: (Color) -> Unit,
    selectedDecoration: Decoration? = null,
    allImages: List<ImageReference>,
    isDeletingImage: Boolean = false,
    selectedDeletingImages: List<String> = emptyList(),
    onImageToggleSelection: (String) -> Unit,
    uchiwaColor: Color,
    backgroundColor: Color,
    decorations: List<Decoration> = emptyList(),
    selectedDecorationId: String? = null,
    onDecorationClick: (String) -> Unit,
    onMoveDecoration: (fromIndex: Int, toIndex: Int) -> Unit
) {
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                // 保存せず、プレビュー画面へ遷移
                onImagePicked(uri)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    Column(
        modifier = modifier
    ) {
        val pagerState = rememberPagerState(pageCount = { DecorationTabType.entries.size })
        val tabIndex = pagerState.currentPage
        val scope = rememberCoroutineScope()

        // selectedDecorationの種類に応じてページを自動的に切り替える
        val isLayerTabSelected = tabIndex == DecorationTabType.LAYERS.ordinal
        LaunchedEffect(selectedDecoration) {
            if (selectedDecoration != null && !isLayerTabSelected) {
                val targetPage = when (selectedDecoration) {
                    is Decoration.Text -> 0
                    is Decoration.Image -> 1
                    is Decoration.Sticker -> 2
                }
                scope.launch {
                    pagerState.animateScrollToPage(targetPage)
                }
            }
        }

        Box {
            PrimaryScrollableTabRow(
                selectedTabIndex = tabIndex,
                edgePadding = 0.dp
            ) {
                DecorationTabType.entries.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(text = title.tabText, maxLines = 1) }
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState
        ) { page ->
            when (page) {
                0 -> {
                    TextPage(
                        onTextClick = onTextClick,
                        onFontChanged = onFontChanged,
                        onColorSelected = onColorSelected,
                        onTextWeightChanged = onTextWeightChanged,
                        onStrokeColorSelected = onStrokeColorSelected,
                        onStrokeWeightChanged = onStrokeWeightChanged,
                        onSecondBorderColorSelected = onSecondBorderColorSelected,
                        onSecondBorderWeightChanged = onSecondBorderWeightChanged,
                        selectedDecoration = selectedDecoration
                    )
                }

                1 -> {
                    ImagePage(
                        onClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        images = allImages,
                        onImageClick = onImageClick,
                        onImageLongPress = onImageLongPress,
                        isDeletingImage = isDeletingImage,
                        selectedImages = selectedDeletingImages,
                        onImageToggleSelection = onImageToggleSelection
                    )
                }

                2 -> {
                    StickerPage(
                        onStickerClick = onStickerClick,
                        onColorSelected = onColorSelected,
                        onStrokeColorSelected = onStrokeColorSelected,
                        onStrokeWeightChanged = onStrokeWeightChanged,
                        onSecondStrokeColorSelected = onSecondBorderColorSelected,
                        onSecondStrokeWeightChanged = onSecondBorderWeightChanged,
                        selectedDecoration = selectedDecoration
                    )
                }

                3 -> {
                    UchiwaBackgroundPage(
                        onUchiwaColorSelected = onUchiwaColorSelected,
                        onBackgroundColorSelected = onBackgroundColorSelected,
                        currentUchiwaColor = uchiwaColor,
                        currentBackgroundColor = backgroundColor
                    )
                }

                4 -> {
                    LayerPage(
                        decorations = decorations,
                        selectedDecorationId = selectedDecorationId,
                        onDecorationClick = onDecorationClick,
                        onMoveDecoration = onMoveDecoration,
                        allImages = allImages
                    )
                }
            }
        }
    }
}

@Composable
fun StickerPage(
    onStickerClick: (Decoration.Sticker) -> Unit,
    onColorSelected: (Color) -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onSecondStrokeColorSelected: (Color) -> Unit,
    onSecondStrokeWeightChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    selectedDecoration: Decoration? = null,
) {
    val scrollState = rememberScrollState()
    // isNew = false のエントリだけで 0 始まりの通し番号を付与するマップ
    val rankIndexMap = remember {
        buildRankIndexMap(StickerAsset.entries) { it.isNew }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (selectedDecoration is Decoration.Sticker) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                HeaderTitle(title = stringResource(R.string.sticker_color))
                ColorPickerRow(
                    onColorSelected = onColorSelected,
                    modifier = Modifier.padding(top = 8.dp),
                    currentColor = selectedDecoration.color
                )
            }

            ColorAndWeightControl(
                title = stringResource(R.string.stroke_color_and_weight),
                color = selectedDecoration.strokeColor,
                width = selectedDecoration.strokeWidth,
                valueRange = 0f..16f,
                steps = 8,
                onColorSelected = onStrokeColorSelected,
                onWeightChanged = onStrokeWeightChanged
            )

            ColorAndWeightControl(
                title = stringResource(R.string.second_stroke_color_and_weight),
                color = selectedDecoration.secondStrokeColor,
                width = selectedDecoration.secondStrokeWidth,
                valueRange = 0f..16f,
                steps = 8,
                onColorSelected = onSecondStrokeColorSelected,
                onWeightChanged = onSecondStrokeWeightChanged
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 16.dp)
        ) {
            StickerAsset.entries.forEach { sticker ->
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            onStickerClick(
                                Decoration.Sticker(
                                    label = sticker.type,
                                    id = UUID.randomUUID().toString(),
                                )
                            )
                        }
                ) {
                    Image(
                        painter = painterResource(id = sticker.resId),
                        contentDescription = sticker.type,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    )
                    val rankIndex = rankIndexMap[sticker]
                    ItemBadge(
                        rankIndex = rankIndex,
                        isNew = sticker.isNew,
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                }
            }
        }
    }
}


@Composable
fun LayerPage(
    decorations: List<Decoration>,
    selectedDecorationId: String?,
    onDecorationClick: (String) -> Unit,
    onMoveDecoration: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    allImages: List<ImageReference>
) {
    // UIでは reversed() を表示（上が手前）
    val displayDecorations = remember(decorations) { decorations.reversed() }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMoveDecoration(from.index, to.index)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        state = lazyListState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = displayDecorations,
            key = { decoration -> decoration.id }
        ) { decoration ->
            val isSelected = decoration.id == selectedDecorationId

            ReorderableItem(reorderableLazyListState, key = decoration.id) { isDragging ->
                LayerItem(
                    decoration = decoration,
                    isSelected = isSelected,
                    isDragging = isDragging,
                    onClick = { onDecorationClick(decoration.id) },
                    modifier = Modifier.draggableHandle(),
                    allImages = allImages
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LayerItem(
    decoration: Decoration,
    isSelected: Boolean,
    isDragging: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    allImages: List<ImageReference>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                if (isDragging) {
                    alpha = 0.9f
                    shadowElevation = 8f
                }
            }
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) {
                    colorResource(R.color.gray).copy(alpha = 0.3f)
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 選択インジケータ
        Box(
            modifier = Modifier
                .size(4.dp, 48.dp)
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        // プレビュー
        LayerItemPreview(
            decoration = decoration,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .height(48.dp)
                .weight(1f),
            allImages = allImages
        )

        // ドラッグハンドル
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Reorder",
            modifier = modifier.size(24.dp)
        )
    }
}

@Composable
private fun LayerItemPreview(
    decoration: Decoration,
    modifier: Modifier = Modifier,
    allImages: List<ImageReference>
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(colorResource(R.color.gray).copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        when (decoration) {
            is Decoration.Text -> {
                TextItemContent(
                    decoration = decoration,
                    textSize = 14.sp.nonScaledSp,
                    modifier = Modifier.padding(4.dp)
                )
            }

            is Decoration.Sticker -> {
                StickerItemContent(
                    decoration = decoration,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(36.dp),
                )
            }

            is Decoration.Image -> {
                val imageReference = allImages.find { it.id == decoration.imageId }
                if (imageReference != null) {
                    ImageItemContent(
                        decoration = decoration,
                        imagePath = imageReference.path,
                        size = 36.dp,
                        modifier = Modifier.padding(4.dp),
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun StickerPagePreview() {
    FansaUchiwaTheme {
        StickerPage(
            onStickerClick = {},
            onColorSelected = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onSecondStrokeColorSelected = {},
            onSecondStrokeWeightChanged = {},
            selectedDecoration = Decoration.Sticker(
                id = "preview-id",
                label = "star",
                color = Color(0xFFFF0000)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LayerPagePreview() {
    FansaUchiwaTheme {
        LayerPage(
            decorations = listOf(
                Decoration.Text(
                    id = "text-1",
                    text = "サンプル",
                    font = FontFamilies.HACHI_MARU_POP
                ),
                Decoration.Sticker(
                    id = "sticker-1",
                    label = StickerAsset.HEART.type
                ),
                Decoration.Text(
                    id = "text-2",
                    text = "テスト",
                    font = FontFamilies.NOTO_SANS_JP
                )
            ),
            selectedDecorationId = "text-1",
            onDecorationClick = {},
            onMoveDecoration = { _, _ -> },
            allImages = emptyList()
        )
    }
}

@Composable
fun UchiwaBackgroundPage(
    modifier: Modifier = Modifier,
    onUchiwaColorSelected: (Color) -> Unit,
    onBackgroundColorSelected: (Color) -> Unit,
    currentUchiwaColor: Color,
    currentBackgroundColor: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        HeaderTitle(
            title = stringResource(R.string.uchiwa_color),
            modifier = Modifier.padding(top = 16.dp)
        )
        ColorPickerRow(
            onColorSelected = { color ->
                onUchiwaColorSelected(color)
            },
            modifier = Modifier.padding(top = 8.dp),
            currentColor = currentUchiwaColor
        )
        HeaderTitle(
            title = stringResource(R.string.background_color),
            modifier = Modifier.padding(top = 16.dp)
        )
        ColorPickerRow(
            onColorSelected = { color ->
                onBackgroundColorSelected(color)
            },
            modifier = Modifier.padding(top = 8.dp),
            currentColor = currentBackgroundColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UchiwaBackgroundPagePreview() {
    FansaUchiwaTheme {
        UchiwaBackgroundPage(
            onUchiwaColorSelected = {},
            onBackgroundColorSelected = {},
            currentUchiwaColor = DecorationColors.RED.value,
            currentBackgroundColor = DecorationColors.BLUE.value
        )
    }
}
