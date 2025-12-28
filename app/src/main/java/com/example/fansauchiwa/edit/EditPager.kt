package com.example.fansauchiwa.edit

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.fansauchiwa.R
import com.example.fansauchiwa.data.Decoration
import com.example.fansauchiwa.data.ImageReference
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
    onTextWeightChanged: (Int) -> Unit,
    onStrokeColorSelected: (Int) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onAddImage: (Decoration.Image, Uri) -> Unit,
    onImageClick: (Decoration.Image) -> Unit,
    onImageLongPress: () -> Unit,
    selectedDecoration: Decoration? = null,
    allImages: List<ImageReference> = emptyList(),
    isDeletingImage: Boolean = false,
) {
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val image = Decoration.Image(id = UUID.randomUUID().toString())
                onAddImage(image, uri)
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
        LaunchedEffect(selectedDecoration) {
            if (selectedDecoration != null) {
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

            if (isDeletingImage) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(colorResource(R.color.black).copy(alpha = 0.5f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { /* タップを無効化 */ }
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
                        onStrokeColorSelected = onStrokeColorSelected,
                        onStrokeWeightChanged = onStrokeWeightChanged,
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
                        onImageLongPress = onImageLongPress
                    )
                }

                2 -> {
                    StickerPage(onStickerClick = onStickerClick)

                }
            }
        }
    }
}

@Composable
fun TextPage(
    onTextClick: (Decoration.Text) -> Unit,
    onColorSelected: (Int) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onStrokeColorSelected: (Int) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    selectedDecoration: Decoration? = null
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        if (selectedDecoration is Decoration.Text) {
            TextDecorationControls(
                onColorSelected = onColorSelected,
                onTextWeightChanged = onTextWeightChanged,
                onStrokeColorSelected = onStrokeColorSelected,
                onStrokeWeightChanged = onStrokeWeightChanged,
                textColor = selectedDecoration.color,
                strokeColor = selectedDecoration.strokeColor,
                textWidth = selectedDecoration.width,
                strokeWidth = selectedDecoration.strokeWidth
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 32.dp)
        ) {
            FontFamilies.entries.forEach {
                Button(
                    onClick = {
                        onTextClick(
                            Decoration.Text(
                                id = UUID.randomUUID().toString(),
                                font = it
                            )
                        )
                    }
                ) {
                    Text(
                        text = "推し",
                        fontSize = 24.sp,
                        fontFamily = it.value
                    )
                }
            }
        }
    }
}

@Composable
fun ImagePage(
    onClick: () -> Unit,
    images: List<ImageReference>,
    onImageClick: (Decoration.Image) -> Unit,
    onImageLongPress: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            IconButton(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                colors = IconButtonDefaults.filledIconButtonColors(),
                modifier = Modifier
                    .aspectRatio(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                )
            }
        }
        items(images) { image ->
            AsyncImage(
                model = image.path,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .aspectRatio(1f)
                    .combinedClickable(
                        onClick = { onImageClick(Decoration.Image(image.id)) },
                        onLongClick = { onImageLongPress() }
                    )
            )
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
fun TextDecorationControls(
    onColorSelected: (Int) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onStrokeColorSelected: (Int) -> Unit,
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
        onColorSelected = onStrokeColorSelected,
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
                            .border(1.dp, colorResource(R.color.black), CircleShape)
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
                    .border(1.dp, colorResource(R.color.black), CircleShape)
                    .background(color = decorationColor.getColor())
                    .clickable {
                        onColorSelected(decorationColor)
                    }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImagePagePreview() {
    ImagePage(
        onClick = {},
        images = emptyList(),
        onImageClick = {},
        onImageLongPress = {}
    )
}