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
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
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
import com.example.fansauchiwa.ui.theme.FansaUchiwaTheme
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditPager(
    modifier: Modifier = Modifier,
    onStickerClick: (Decoration.Sticker) -> Unit,
    onTextClick: (Decoration.Text) -> Unit,
    onColorSelected: (Color) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onAddImage: (Decoration.Image, Uri) -> Unit,
    onImageClick: (Decoration.Image) -> Unit,
    onImageLongPress: () -> Unit,
    onUchiwaColorSelected: (Color) -> Unit = {},
    onBackgroundColorSelected: (Color) -> Unit = {},
    selectedDecoration: Decoration? = null,
    allImages: List<ImageReference> = emptyList(),
    isDeletingImage: Boolean = false,
    selectedDeletingImages: List<String> = emptyList(),
    onImageToggleSelection: (String) -> Unit = {},
    uchiwaColor: Color,
    backgroundColor: Color,
) {
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val imageId = UUID.randomUUID().toString()
                val image = Decoration.Image(
                    id = UUID.randomUUID().toString(),
                    imageId = imageId
                )
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
            userScrollEnabled = !isDeletingImage
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
            }
        }
    }
}

@Composable
fun TextPage(
    onTextClick: (Decoration.Text) -> Unit,
    onColorSelected: (Color) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    selectedDecoration: Decoration? = null
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 16.dp)
        ) {
            FontFamilies.entries.forEach { fontFamily ->
                Button(
                    onClick = {
                        onTextClick(
                            Decoration.Text(
                                id = UUID.randomUUID().toString(),
                                font = fontFamily
                            )
                        )
                    },
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.38f
                        ),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                            alpha = 0.38f
                        )
                    ),
                    modifier = Modifier
                        .size(108.dp, 54.dp)
                ) {
                    val density = LocalDensity.current
                    Text(
                        text = "あA!",
                        fontSize = (20.dp.value / density.fontScale).sp,
                        fontFamily = fontFamily.value
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
    isDeletingImage: Boolean = false,
    selectedImages: List<String> = emptyList(),
    onImageToggleSelection: (String) -> Unit = {},
    isPreview: Boolean = false,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 56.dp),
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
            val isSelected = selectedImages.contains(image.id)
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
            ) {
                if (isPreview) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .background(colorResource(R.color.gray))
                            .combinedClickable(
                                onClick = {
                                    if (isDeletingImage) {
                                        onImageToggleSelection(image.id)
                                    } else {
                                        onImageClick(
                                            Decoration.Image(
                                                id = UUID.randomUUID().toString(),
                                                imageId = image.id
                                            )
                                        )
                                    }
                                },
                                onLongClick = { onImageLongPress() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Image",
                            color = colorResource(R.color.black),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    AsyncImage(
                        model = image.path,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .combinedClickable(
                                onClick = {
                                    if (isDeletingImage) {
                                        onImageToggleSelection(image.id)
                                    } else {
                                        onImageClick(
                                            Decoration.Image(
                                                id = UUID.randomUUID().toString(),
                                                imageId = image.id
                                            )
                                        )
                                    }
                                },
                                onLongClick = { onImageLongPress() }
                            )
                    )
                }
                if (isDeletingImage) {
                    Icon(
                        imageVector = if (isSelected) {
                            Icons.Filled.CheckCircle
                        } else {
                            Icons.Outlined.Circle
                        },
                        contentDescription = if (isSelected) "Selected" else "Not selected",
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            colorResource(R.color.white)
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(colorResource(R.color.white), CircleShape)
                                } else {
                                    Modifier
                                }
                            )
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
    selectedDecoration: Decoration? = null,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
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
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 16.dp)
        ) {
            StickerAsset.entries.forEach { sticker ->
                Box(
                    modifier = Modifier
                        .size(56.dp)
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
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TextDecorationControls(
    onColorSelected: (Color) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    textColor: Color,
    textWidth: Int,
    strokeColor: Color,
    strokeWidth: Float
) {
    ColorAndWeightControl(
        title = stringResource(R.string.text_color_and_weight),
        color = textColor,
        width = textWidth,
        onColorSelected = onColorSelected,
        onWeightChanged = onTextWeightChanged
    )

    ColorAndWeightControl(
        title = stringResource(R.string.stroke_color_and_weight),
        color = strokeColor,
        width = (strokeWidth * 10f).toInt(),
        onColorSelected = onStrokeColorSelected,
        onWeightChanged = { newValue ->
            onStrokeWeightChanged(newValue.toFloat() / 10)
        }
    )
}

@Composable
fun ColorAndWeightControl(
    title: String,
    color: Color,
    width: Int = FontWeight.W900.weight,
    onColorSelected: (Color) -> Unit = {},
    onWeightChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isColorPickerOpen = remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(top = 16.dp)) {
        HeaderTitle(title)

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
                            .border(1.dp, colorResource(R.color.gray), CircleShape)
                            .background(color = color)
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
                onColorSelected = onColorSelected,
                modifier = Modifier.padding(top = 8.dp),
                currentColor = color
            )
        }
    }

}

@Composable
private fun HeaderTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun ColorPickerRow(
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    currentColor: Color
) {
    var showColorPickerDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(1.dp, colorResource(R.color.gray), CircleShape)
                .background(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Red,
                            Color.Yellow,
                            Color.Green,
                            Color.Cyan,
                            Color.Blue,
                            Color.Magenta,
                            Color.Red
                        )
                    )
                )
                .clickable {
                    showColorPickerDialog = true
                }
        )
        DecorationColors.entries.forEach { decorationColor ->
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.dp, colorResource(R.color.gray), CircleShape)
                    .background(color = decorationColor.value)
                    .clickable {
                        onColorSelected(decorationColor.value)
                    }
            )
        }
    }

    if (showColorPickerDialog) {
        ColorPickerDialog(
            initialColor = currentColor,
            onDismiss = { showColorPickerDialog = false },
            onColorSelected = onColorSelected
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TextPagePreview() {
    FansaUchiwaTheme {
        TextPage(
            onTextClick = {},
            onColorSelected = {},
            onTextWeightChanged = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            selectedDecoration = Decoration.Text(
                id = "preview-id",
                font = FontFamilies.HACHI_MARU_POP,
                text = "プレビュー",
                color = Color(0xFF000000),
                strokeColor = Color(0xFFFFFFFF),
                width = 700,
                strokeWidth = 2.5f
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ImagePagePreview() {
    FansaUchiwaTheme {
        ImagePage(
            onClick = {},
            images = listOf(
                ImageReference(
                    id = "image-1",
                    path = "path/to/image1.jpg"
                ),
                ImageReference(
                    id = "image-2",
                    path = "path/to/image2.jpg"
                ),
                ImageReference(
                    id = "image-3",
                    path = "path/to/image3.jpg"
                ),
                ImageReference(
                    id = "image-4",
                    path = "path/to/image4.jpg"
                ),
            ),
            onImageClick = {},
            onImageLongPress = {},
            isDeletingImage = false,
            selectedImages = emptyList(),
            onImageToggleSelection = {},
            isPreview = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StickerPagePreview() {
    FansaUchiwaTheme {
        StickerPage(
            onStickerClick = {},
            onColorSelected = {},
            selectedDecoration = Decoration.Sticker(
                id = "preview-id",
                label = "star",
                color = Color(0xFFFF0000)
            )
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
