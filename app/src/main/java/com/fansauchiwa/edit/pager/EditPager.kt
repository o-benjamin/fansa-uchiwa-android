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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fansauchiwa.R
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.DecorationColors
import com.fansauchiwa.data.ImageReference
import com.fansauchiwa.edit.ColorAndWeightControl
import com.fansauchiwa.ui.composable.ColorPickerRow
import com.fansauchiwa.edit.DecorationTabType
import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.edit.HeaderTitle
import com.fansauchiwa.edit.ItemBadge
import com.fansauchiwa.edit.TestTags
import com.fansauchiwa.edit.buildRankIndexMap
import com.fansauchiwa.edit.decorationitem.ImageItemContent
import com.fansauchiwa.edit.decorationitem.StickerItemContent
import com.fansauchiwa.edit.decorationitem.TextItemContent
import com.fansauchiwa.edit.nonScaledSp
import com.fansauchiwa.ui.StickerAsset
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private const val OVERALL_BORDER_MIN_WIDTH = 0f
private const val OVERALL_BORDER_MAX_WIDTH = 24f
private const val OVERALL_BORDER_SLIDER_STEPS = 23

@Immutable
data class EditPagerUiState(
    val selectedDecoration: Decoration? = null,
    val allImages: List<ImageReference> = emptyList(),
    val isDeletingImage: Boolean = false,
    val selectedDeletingImages: List<String> = emptyList(),
    val uchiwaColor: Color,
    val backgroundColor: Color,
    val overallBorderColor: Color,
    val overallBorderWidth: Float,
    val isOverallBorderPuffyEnabled: Boolean,
    val decorations: List<Decoration> = emptyList(),
    val selectedDecorationId: String? = null,
    val isPukuPukuSupported: Boolean = false
)

data class EditPagerActions(
    val onStickerSelected: (String) -> Unit,
    val onAddText: (FontFamilies) -> Unit,
    val onFontChanged: (FontFamilies) -> Unit,
    val onColorSelected: (Color) -> Unit,
    val onTextWeightChanged: (Int) -> Unit,
    val onTextWeightChangedFinished: () -> Unit,
    val onStrokeColorSelected: (Color) -> Unit,
    val onStrokeWeightChanged: (Float) -> Unit,
    val onStrokeWeightChangedFinished: () -> Unit,
    val onSecondBorderColorSelected: (Color) -> Unit,
    val onSecondBorderWeightChanged: (Float) -> Unit,
    val onSecondBorderWeightChangedFinished: () -> Unit,
    val onPuffyEnabledChanged: (Boolean) -> Unit,
    val onUnsupportedPuffyClick: () -> Unit,
    val onImageSelected: (String) -> Unit,
    val onImageLongPress: () -> Unit,
    val onImagePicked: (Uri) -> Unit,
    val onImageToggleSelection: (String) -> Unit,
    val onUchiwaColorSelected: (Color) -> Unit,
    val onBackgroundColorSelected: (Color) -> Unit,
    val onOverallBorderColorSelected: (Color) -> Unit,
    val onOverallBorderWeightChanged: (Float) -> Unit,
    val onOverallBorderWeightChangedFinished: () -> Unit,
    val onOverallBorderPuffyEnabledChanged: (Boolean) -> Unit,
    val onDecorationClick: (String) -> Unit,
    val onMoveDecoration: (fromIndex: Int, toIndex: Int) -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditPager(
    state: EditPagerUiState,
    actions: EditPagerActions,
    modifier: Modifier = Modifier
) {
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                actions.onImagePicked(uri)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    val pagerState = rememberPagerState(pageCount = { DecorationTabType.entries.size })
    val scope = rememberCoroutineScope()
    val selectedTabIndex = pagerState.currentPage
    val isLayerTabSelected = selectedTabIndex == DecorationTabType.LAYERS.ordinal
    val selectedTextDecoration = state.selectedDecoration as? Decoration.Text
    val selectedStickerDecoration = state.selectedDecoration as? Decoration.Sticker

    LaunchedEffect(state.selectedDecoration, isLayerTabSelected) {
        val targetPage = state.selectedDecoration.toDecorationPageIndex() ?: return@LaunchedEffect
        if (!isLayerTabSelected) {
            scope.launch {
                pagerState.animateScrollToPage(targetPage)
            }
        }
    }

    Column(
        modifier = modifier
    ) {
        EditPagerTabRow(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { index ->
                scope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }
        )

        HorizontalPager(
            state = pagerState
        ) { page ->
            when (page) {
                0 -> {
                    TextPage(
                        onAddText = actions.onAddText,
                        onFontChanged = actions.onFontChanged,
                        onColorSelected = actions.onColorSelected,
                        onTextWeightChanged = actions.onTextWeightChanged,
                        onTextWeightChangedFinished = actions.onTextWeightChangedFinished,
                        onStrokeColorSelected = actions.onStrokeColorSelected,
                        onStrokeWeightChanged = actions.onStrokeWeightChanged,
                        onStrokeWeightChangedFinished = actions.onStrokeWeightChangedFinished,
                        onSecondBorderColorSelected = actions.onSecondBorderColorSelected,
                        onSecondBorderWeightChanged = actions.onSecondBorderWeightChanged,
                        onSecondBorderWeightChangedFinished = actions.onSecondBorderWeightChangedFinished,
                        onPuffyEnabledChanged = actions.onPuffyEnabledChanged,
                        onPuffyUnsupportedClick = actions.onUnsupportedPuffyClick,
                        isPukuPukuSupported = state.isPukuPukuSupported,
                        selectedTextDecoration = selectedTextDecoration
                    )
                }

                1 -> {
                    ImagePage(
                        onAddImageClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        images = state.allImages,
                        onImageSelected = actions.onImageSelected,
                        onImageLongPress = actions.onImageLongPress,
                        isDeletingImage = state.isDeletingImage,
                        selectedImages = state.selectedDeletingImages,
                        onImageToggleSelection = actions.onImageToggleSelection
                    )
                }

                2 -> {
                    StickerPage(
                        onStickerSelected = actions.onStickerSelected,
                        onColorSelected = actions.onColorSelected,
                        onStrokeColorSelected = actions.onStrokeColorSelected,
                        onStrokeWeightChanged = actions.onStrokeWeightChanged,
                        onStrokeWeightChangedFinished = actions.onStrokeWeightChangedFinished,
                        onSecondStrokeColorSelected = actions.onSecondBorderColorSelected,
                        onSecondStrokeWeightChanged = actions.onSecondBorderWeightChanged,
                        onSecondStrokeWeightChangedFinished = actions.onSecondBorderWeightChangedFinished,
                        onPuffyEnabledChanged = actions.onPuffyEnabledChanged,
                        onPuffyUnsupportedClick = actions.onUnsupportedPuffyClick,
                        isPukuPukuSupported = state.isPukuPukuSupported,
                        selectedStickerDecoration = selectedStickerDecoration
                    )
                }

                3 -> {
                    UchiwaBackgroundPage(
                        onUchiwaColorSelected = actions.onUchiwaColorSelected,
                        onBackgroundColorSelected = actions.onBackgroundColorSelected,
                        onOverallBorderColorSelected = actions.onOverallBorderColorSelected,
                        onOverallBorderWeightChanged = actions.onOverallBorderWeightChanged,
                        onOverallBorderWeightChangedFinished = actions.onOverallBorderWeightChangedFinished,
                        onOverallBorderPuffyEnabledChanged = actions.onOverallBorderPuffyEnabledChanged,
                        onPuffyUnsupportedClick = actions.onUnsupportedPuffyClick,
                        currentUchiwaColor = state.uchiwaColor,
                        currentBackgroundColor = state.backgroundColor,
                        currentOverallBorderColor = state.overallBorderColor,
                        currentOverallBorderWidth = state.overallBorderWidth,
                        isOverallBorderPuffyEnabled = state.isOverallBorderPuffyEnabled,
                        isPukuPukuSupported = state.isPukuPukuSupported
                    )
                }

                4 -> {
                    LayerPage(
                        decorations = state.decorations,
                        selectedDecorationId = state.selectedDecorationId,
                        onDecorationClick = actions.onDecorationClick,
                        onMoveDecoration = actions.onMoveDecoration,
                        allImages = state.allImages
                    )
                }
            }
        }
    }
}

private fun Decoration?.toDecorationPageIndex(): Int? {
    return when (this) {
        is Decoration.Text -> DecorationTabType.TEXT.ordinal
        is Decoration.Image -> DecorationTabType.IMAGE.ordinal
        is Decoration.Sticker -> DecorationTabType.STAMP.ordinal
        null -> null
    }
}

@Composable
private fun EditPagerTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Box {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 0.dp
        ) {
            DecorationTabType.entries.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = { Text(text = stringResource(title.tabTextRes), maxLines = 1) }
                )
            }
        }
    }
}

@Composable
fun StickerPage(
    onStickerSelected: (String) -> Unit,
    onColorSelected: (Color) -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onStrokeWeightChangedFinished: () -> Unit,
    onSecondStrokeColorSelected: (Color) -> Unit,
    onSecondStrokeWeightChanged: (Float) -> Unit,
    onSecondStrokeWeightChangedFinished: () -> Unit,
    onPuffyEnabledChanged: (Boolean) -> Unit,
    onPuffyUnsupportedClick: () -> Unit,
    isPukuPukuSupported: Boolean,
    modifier: Modifier = Modifier,
    selectedStickerDecoration: Decoration.Sticker? = null,
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
        if (selectedStickerDecoration != null) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                HeaderTitle(title = stringResource(R.string.sticker_color))
                ColorPickerRow(
                    onColorSelected = onColorSelected,
                    modifier = Modifier.padding(top = 8.dp),
                    currentColor = selectedStickerDecoration.color
                )
            }

            ColorAndWeightControl(
                title = stringResource(R.string.stroke_color_and_weight),
                color = selectedStickerDecoration.strokeColor,
                width = selectedStickerDecoration.strokeWidth,
                valueRange = 0f..16f,
                steps = 15,
                onColorSelected = onStrokeColorSelected,
                onWeightChanged = onStrokeWeightChanged,
                onWeightChangedFinished = onStrokeWeightChangedFinished
            )

            ColorAndWeightControl(
                title = stringResource(R.string.second_stroke_color_and_weight),
                color = selectedStickerDecoration.secondStrokeColor,
                width = selectedStickerDecoration.secondStrokeWidth,
                valueRange = 0f..16f,
                steps = 15,
                onColorSelected = onSecondStrokeColorSelected,
                onWeightChanged = onSecondStrokeWeightChanged,
                onWeightChangedFinished = onSecondStrokeWeightChangedFinished
            )

            PuffyEffectToggleRow(
                label = stringResource(R.string.sticker_puffy_enabled),
                isEnabled = isPukuPukuSupported,
                isChecked = selectedStickerDecoration.isPukupuku,
                onCheckedChange = onPuffyEnabledChanged,
                onUnsupportedClick = onPuffyUnsupportedClick,
                modifier = Modifier.testTag(TestTags.PUFFY_STICKER_ROW),
                switchModifier = Modifier.testTag(TestTags.PUFFY_STICKER_SWITCH)
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
                        .clickable { onStickerSelected(sticker.type) }
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
            onStickerSelected = {},
            onColorSelected = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onStrokeWeightChangedFinished = {},
            onSecondStrokeColorSelected = {},
            onSecondStrokeWeightChanged = {},
            onSecondStrokeWeightChangedFinished = {},
            onPuffyEnabledChanged = {},
            onPuffyUnsupportedClick = {},
            isPukuPukuSupported = true,
            selectedStickerDecoration = Decoration.Sticker(
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
    onOverallBorderColorSelected: (Color) -> Unit,
    onOverallBorderWeightChanged: (Float) -> Unit,
    onOverallBorderWeightChangedFinished: () -> Unit,
    onOverallBorderPuffyEnabledChanged: (Boolean) -> Unit,
    onPuffyUnsupportedClick: () -> Unit,
    currentUchiwaColor: Color,
    currentBackgroundColor: Color,
    currentOverallBorderColor: Color,
    currentOverallBorderWidth: Float,
    isOverallBorderPuffyEnabled: Boolean,
    isPukuPukuSupported: Boolean
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
        ColorAndWeightControl(
            title = stringResource(R.string.overall_border),
            color = currentOverallBorderColor,
            width = currentOverallBorderWidth,
            valueRange = OVERALL_BORDER_MIN_WIDTH..OVERALL_BORDER_MAX_WIDTH,
            steps = OVERALL_BORDER_SLIDER_STEPS,
            onColorSelected = onOverallBorderColorSelected,
            onWeightChanged = onOverallBorderWeightChanged,
            onWeightChangedFinished = onOverallBorderWeightChangedFinished
        )
        PuffyEffectToggleRow(
            label = stringResource(R.string.overall_border_puffy_enabled),
            isEnabled = isPukuPukuSupported,
            isChecked = isOverallBorderPuffyEnabled,
            onCheckedChange = onOverallBorderPuffyEnabledChanged,
            onUnsupportedClick = onPuffyUnsupportedClick
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
            onOverallBorderColorSelected = {},
            onOverallBorderWeightChanged = {},
            onOverallBorderWeightChangedFinished = {},
            onOverallBorderPuffyEnabledChanged = {},
            onPuffyUnsupportedClick = {},
            currentUchiwaColor = DecorationColors.RED.value,
            currentBackgroundColor = DecorationColors.BLUE.value,
            currentOverallBorderColor = DecorationColors.WHITE.value,
            currentOverallBorderWidth = 8f,
            isOverallBorderPuffyEnabled = true,
            isPukuPukuSupported = true
        )
    }
}
