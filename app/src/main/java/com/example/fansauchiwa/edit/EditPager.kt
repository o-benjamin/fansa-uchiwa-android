package com.example.fansauchiwa.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fansauchiwa.R
import com.example.fansauchiwa.data.Decoration
import com.example.fansauchiwa.ui.DecorationColors
import com.example.fansauchiwa.ui.StickerAsset
import com.example.fansauchiwa.ui.getColor
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditPager(
    modifier: Modifier = Modifier,
    onStickerClick: (Decoration.Sticker) -> Unit,
    onTextClick: (Decoration.Text) -> Unit,
    onColorSelected: (Int) -> Unit,
    onStrokeColorSelected: (Int) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    selectedDecoration: Decoration? = null,
) {
    Column(
        modifier = modifier
    ) {
        val pagerState = rememberPagerState(pageCount = { DecorationTabType.entries.size })
        val tabIndex = pagerState.currentPage
        val scope = rememberCoroutineScope()

        // selectedDecorationの種類に応じてページを自動的に切り替える
        LaunchedEffect(selectedDecoration) {
            if (selectedDecoration != null) {
                val targetPage = when (selectedDecoration) {
                    is Decoration.Text -> 0
                    is Decoration.Sticker -> 1
                }
                scope.launch {
                    pagerState.animateScrollToPage(targetPage)
                }
            }
        }

        TabRow(
            selectedTabIndex = tabIndex
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

        HorizontalPager(
            state = pagerState,
        ) { page ->
            when (page) {
                0 -> {
                    TextPage(
                        onTextClick = onTextClick,
                        onColorSelected = onColorSelected,
                        onTextWeightChanged = onTextWeightChanged,
                        onStrokeWeightChanged = onStrokeWeightChanged,
                        selectedDecoration = selectedDecoration
                    )
                }

                1 -> {
                    StickerPage(onStickerClick = onStickerClick)
                }

                else -> {
                    // Placeholder for other tabs
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "${DecorationTabType.entries[page]} content")
                    }
                }
            }
        }
    }
}

@Composable
fun StickerPage(
    onStickerClick: (Decoration.Sticker) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 64.dp),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(StickerAsset.entries) { sticker ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        onStickerClick(
                            Decoration.Sticker(
                                label = sticker.type,
                                id = UUID.randomUUID().toString(),
                                offset = Offset.Zero,
                                rotation = 0f,
                                scale = 1f,
                                color = R.color.decoration_blue,
                                strokeColor = R.color.decoration_yellow,
                                strokeWidth = 200f
                            )
                        )
                    }
            ) {
                Image(
                    painter = painterResource(id = sticker.resId),
                    contentDescription = sticker.type,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun TextPage(
    onTextClick: (Decoration.Text) -> Unit,
    onColorSelected: (Int) -> Unit = {},
    onTextWeightChanged: (Int) -> Unit = {},
    onStrokeWeightChanged: (Float) -> Unit = {},
    selectedDecoration: Decoration? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        if (selectedDecoration is Decoration.Text) {
            TextDecorationControls(
                onColorSelected = onColorSelected,
                onTextWeightChanged = onTextWeightChanged,
                onStrokeWeightChanged = onStrokeWeightChanged,
                textColor = selectedDecoration.color,
                strokeColor = selectedDecoration.color,
                textWidth = selectedDecoration.width,
                strokeWidth = selectedDecoration.strokeWidth
            )
        }

        Button(
            onClick = {
                onTextClick(
                    Decoration.Text(
                        text = DEFAULT_TEXT,
                        id = UUID.randomUUID().toString(),
                        offset = Offset.Zero,
                        rotation = 0f,
                        scale = 1f,
                        color = R.color.decoration_white,
                        width = FontWeight.W900.weight,
                        strokeColor = R.color.decoration_yellow,
                        strokeWidth = 30f
                    )
                )
            },
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text(text = "テキストを追加")
        }
    }
}

@Composable
fun TextDecorationControls(
    onColorSelected: (Int) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    textColor: Int,
    textWidth: Int,
    strokeColor: Int,
    strokeWidth: Float
) {
    ColorAndWeightControl(
        title = stringResource(R.string.text_color_and_weight),
        color = textColor,
        width = textWidth,
        onColorSelected = onColorSelected,
        onWeightChanged = onTextWeightChanged,
        modifier = Modifier.padding(top = 32.dp)
    )

    ColorAndWeightControl(
        title = stringResource(R.string.stroke_color_and_weight),
        color = strokeColor,
        width = (strokeWidth * 10f).toInt(),
        onColorSelected = onColorSelected,
        onWeightChanged = { newValue ->
            onStrokeWeightChanged(newValue.toFloat() / 10)
        },
        modifier = Modifier.padding(top = 32.dp)
    )
}

@Composable
fun ColorAndWeightControl(
    title: String,
    color: Int,
    width: Int = FontWeight.W900.weight,
    onColorSelected: (Int) -> Unit = {},
    onWeightChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isColorPickerOpen = remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(
                    onClick = {
                        isColorPickerOpen.value = false
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandLess,
                        contentDescription = "Color picker toggle"
                    )
                }
                this@Column.AnimatedVisibility(
                    visible = !isColorPickerOpen.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                color = colorResource(color)
                            )
                            .clickable {
                                isColorPickerOpen.value = true
                            }
                    )
                }
            }
            Slider(
                value = width.toFloat(),
                onValueChange = { newValue ->
                    onWeightChanged(newValue.toInt())
                    isColorPickerOpen.value = false
                },
                valueRange = 100f..900f,
                steps = 9,
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedVisibility(isColorPickerOpen.value) {
            ColorPickerRow(
                onColorSelected = { color ->
                    onColorSelected(color.colorResId)
                }
            )
        }
    }

}

@Composable
fun ColorPickerRow(
    onColorSelected: (DecorationColors) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DecorationColors.entries.forEach { decorationColor ->
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color = decorationColor.getColor())
                    .clickable {
                        onColorSelected(decorationColor)
                    }
            )
        }
    }
}

const val DEFAULT_TEXT = "テキストを入力"
