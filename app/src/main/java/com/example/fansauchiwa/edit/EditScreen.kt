package com.example.fansauchiwa.edit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.fansauchiwa.R
import com.example.fansauchiwa.data.Decoration
import com.example.fansauchiwa.ui.theme.FansaUchiwaTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditScreen(
    viewModel: EditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.userMessage?.let { userMessage ->
        val snackbarText = stringResource(userMessage)
        LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
            snackbarHostState.showSnackbar(snackbarText)
            viewModel.snackbarMessageShown()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.imePadding()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UchiwaPreview(
                decorations = uiState.decorations,
                selectedDecorationId = uiState.selectedDecorationId,
                editingTextId = uiState.editingTextId,
                onDecorationTap = viewModel::selectDecoration,
                onDecorationDoubleTap = { decorationId ->
                    viewModel.selectDecoration(decorationId)
                    viewModel.startEditingText(decorationId)
                },
                onBackgroundTap = {
                    viewModel.unSelectDecoration()
                    viewModel.finishEditingText()
                },
                onDecorationDragEnd = viewModel::updateDecorationGraphic,
                onTapDelete = viewModel::deleteDecoration,
                onTextChanged = viewModel::updateText,
                onDoneTextEdit = viewModel::finishEditingText,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )

            EditPager(
                onStickerClick = viewModel::addDecoration,
                onTextClick = viewModel::addDecoration,
                onColorSelected = { color ->
                    uiState.selectedDecorationId?.let { decorationId ->
                        viewModel.updateColor(decorationId, color)
                    }
                },
                onStrokeColorSelected = { color ->
                    uiState.selectedDecorationId?.let { decorationId ->
                        viewModel.updateStrokeColor(decorationId, color)
                    }
                },
                onTextWeightChanged = { weight ->
                    uiState.selectedDecorationId?.let { decorationId ->
                        viewModel.updateWidth(decorationId, weight)
                    }
                },
                onStrokeWeightChanged = { weight ->
                    uiState.selectedDecorationId?.let { decorationId ->
                        viewModel.updateStrokeWidth(decorationId, weight)
                    }
                },
                onAddImage = { (image, uri) ->
                    viewModel.addDecoration(image)
                    viewModel.saveImage(uri, image.id)
                },
                selectedDecoration = uiState.decorations.find { it.id == uiState.selectedDecorationId },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
        }
    }
}

@Composable
fun UchiwaPreview(
    decorations: List<Decoration>,
    selectedDecorationId: String?,
    editingTextId: String?,
    onDecorationTap: (String) -> Unit,
    onDecorationDoubleTap: (String) -> Unit,
    onBackgroundTap: () -> Unit,
    onDecorationDragEnd: (String, Offset, Float, Float) -> Unit,
    onTapDelete: (String) -> Unit,
    onTextChanged: (String, String) -> Unit,
    onDoneTextEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = modifier.clickable(
            interactionSource = null,
            indication = null
        ) {
            onBackgroundTap()
            onDoneTextEdit()
            focusManager.clearFocus()
        },
        contentAlignment = Alignment.Center
    ) {
        // TODO: うちわの形にする
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        decorations.forEach { decoration ->
            key(decoration.id) {
                var offsetDiff by remember { mutableStateOf(Offset.Zero) }
                var cumulativeOffset by remember { mutableStateOf(Offset.Zero) }
                var scaleDiff by remember { mutableFloatStateOf(0f) }
                var rotationDiff by remember { mutableFloatStateOf(0f) }
                val isSelected = decoration.id == selectedDecorationId
                when (decoration) {
                    is Decoration.Text -> {
                        TextItem(
                            decoration = decoration,
                            isSelected = isSelected,
                            isEditing = decoration.id == editingTextId,
                            currentOffset = decoration.offset + offsetDiff,
                            currentScale = decoration.scale + scaleDiff,
                            currentRotation = decoration.rotation + rotationDiff,
                            onTextChanged = { onTextChanged(decoration.id, it) },
                            onFinishEditing = onDoneTextEdit
                        )
                        val textMeasurer = rememberTextMeasurer()
                        val decorationSize = textMeasurer.measure(
                            decoration.text,
                            TextStyle(
                                fontSize = 24.sp.nonScaledSp,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        ).size.toSize()
                        val decorationDpSize = with(LocalDensity.current) {
                            decorationSize.toDpSize() + DpSize(TEXT_ITEM_PADDING, TEXT_ITEM_PADDING)
                        }
                        val handleOffset = calculateHandleOffset(
                            baseOffset = decoration.offset,
                            scale = decoration.scale,
                            rotation = decoration.rotation,
                            decorationSize = decorationSize,
                            corner = HandleCorner.BottomRight
                        )
                        GestureInputLayer(
                            offset = decoration.offset,
                            scale = decoration.scale,
                            rotation = decoration.rotation,
                            decorationSize = decorationDpSize,
                            isSelected = isSelected,
                            onDecorationTap = { onDecorationTap(decoration.id) },
                            onDecorationDoubleTap = { onDecorationDoubleTap(decoration.id) },
                            onDrag = { offsetDiff += it },
                            onDragEnd = {
                                onDecorationDragEnd(
                                    decoration.id,
                                    offsetDiff,
                                    scaleDiff,
                                    rotationDiff
                                )
                                offsetDiff = Offset.Zero
                                scaleDiff = 0f
                                rotationDiff = 0f
                            },
                            onTransformStart = {
                                cumulativeOffset = Offset.Zero
                            },
                            onTransform = { dragAmount ->
                                val transformation = calculateTransformations(
                                    cumulativeOffset,
                                    handleOffset - decoration.offset
                                )
                                cumulativeOffset += dragAmount.rotateBy(decoration.rotation) * decoration.scale
                                val targetScale =
                                    (decoration.scale + transformation.scaleDiff).coerceIn(
                                        0.5f,
                                        3f
                                    )
                                scaleDiff = targetScale - decoration.scale
                                rotationDiff = transformation.rotationDiff
                            },
                            onTransformEnd = {
                                onDecorationDragEnd(
                                    decoration.id,
                                    offsetDiff,
                                    scaleDiff,
                                    rotationDiff
                                )
                                offsetDiff = Offset.Zero
                                scaleDiff = 0f
                                rotationDiff = 0f
                            },
                            onTapDelete = { onTapDelete(decoration.id) }
                        )
                    }

                    is Decoration.Sticker -> {
                        val decorationSize = painterResource(decoration.resId).intrinsicSize
                        val decorationDpSize = with(LocalDensity.current) {
                            decorationSize.toDpSize()
                        }
                        val handleOffset = calculateHandleOffset(
                            baseOffset = decoration.offset,
                            scale = decoration.scale,
                            rotation = decoration.rotation,
                            decorationSize = decorationSize,
                            corner = HandleCorner.BottomRight
                        )

                        GestureInputLayer(
                            offset = decoration.offset,
                            scale = decoration.scale,
                            rotation = decoration.rotation,
                            decorationSize = decorationDpSize,
                            isSelected = isSelected,
                            onDecorationTap = { onDecorationTap(decoration.id) },
                            onDrag = { offsetDiff += it },
                            onDragEnd = {
                                onDecorationDragEnd(
                                    decoration.id,
                                    offsetDiff,
                                    scaleDiff,
                                    rotationDiff
                                )
                                offsetDiff = Offset.Zero
                                scaleDiff = 0f
                                rotationDiff = 0f
                            },
                            onTransformStart = {
                                cumulativeOffset = Offset.Zero
                            },
                            onTransform = { dragAmount ->
                                val transformation = calculateTransformations(
                                    cumulativeOffset,
                                    handleOffset - decoration.offset
                                )
                                cumulativeOffset += dragAmount.rotateBy(decoration.rotation) * decoration.scale
                                val targetScale =
                                    (decoration.scale + transformation.scaleDiff).coerceIn(
                                        0.5f,
                                        3f
                                    )
                                scaleDiff = targetScale - decoration.scale
                                rotationDiff = transformation.rotationDiff
                            },
                            onTransformEnd = {
                                onDecorationDragEnd(
                                    decoration.id,
                                    offsetDiff,
                                    scaleDiff,
                                    rotationDiff
                                )
                                offsetDiff = Offset.Zero
                                scaleDiff = 0f
                                rotationDiff = 0f
                            },
                            onTapDelete = { onTapDelete(decoration.id) }
                        )
                        StickerItem(
                            decoration = decoration,
                            isSelected = isSelected,
                            currentOffset = decoration.offset + offsetDiff,
                            currentScale = decoration.scale + scaleDiff,
                            currentRotation = decoration.rotation + rotationDiff
                        )
                    }

                    is Decoration.Image -> {
                        val imageDpSize = DpSize(IMAGE_SIZE_DEFAULT, IMAGE_SIZE_DEFAULT)
                        val handleOffset = calculateHandleOffset(
                            baseOffset = decoration.offset,
                            scale = decoration.scale,
                            rotation = decoration.rotation,
                            decorationSize = Size(
                                IMAGE_SIZE_DEFAULT.value,
                                IMAGE_SIZE_DEFAULT.value
                            ),
                            corner = HandleCorner.BottomRight
                        )

                        GestureInputLayer(
                            offset = decoration.offset,
                            scale = decoration.scale,
                            rotation = decoration.rotation,
                            decorationSize = imageDpSize,
                            isSelected = isSelected,
                            onDecorationTap = { onDecorationTap(decoration.id) },
                            onDrag = { offsetDiff += it },
                            onDragEnd = {
                                onDecorationDragEnd(
                                    decoration.id,
                                    offsetDiff,
                                    scaleDiff,
                                    rotationDiff
                                )
                                offsetDiff = Offset.Zero
                                scaleDiff = 0f
                                rotationDiff = 0f
                            },
                            onTransformStart = {
                                cumulativeOffset = Offset.Zero
                            },
                            onTransform = { dragAmount ->
                                val transformation = calculateTransformations(
                                    cumulativeOffset,
                                    handleOffset - decoration.offset
                                )
                                cumulativeOffset += dragAmount.rotateBy(decoration.rotation) * decoration.scale
                                val targetScale =
                                    (decoration.scale + transformation.scaleDiff).coerceIn(
                                        0.5f,
                                        3f
                                    )
                                scaleDiff = targetScale - decoration.scale
                                rotationDiff = transformation.rotationDiff
                            },
                            onTransformEnd = {
                                onDecorationDragEnd(
                                    decoration.id,
                                    offsetDiff,
                                    scaleDiff,
                                    rotationDiff
                                )
                                offsetDiff = Offset.Zero
                                scaleDiff = 0f
                                rotationDiff = 0f
                            },
                            onTapDelete = { onTapDelete(decoration.id) }
                        )
                        ImageItem(
                            decoration = decoration,
                            isSelected = isSelected,
                            currentOffset = decoration.offset + offsetDiff,
                            currentScale = decoration.scale + scaleDiff,
                            currentRotation = decoration.rotation + rotationDiff
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GestureInputLayer(
    offset: Offset,
    scale: Float,
    rotation: Float,
    decorationSize: DpSize,
    isSelected: Boolean,
    onDecorationTap: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onTransformStart: () -> Unit,
    onTransform: (Offset) -> Unit,
    onTransformEnd: () -> Unit,
    onTapDelete: () -> Unit,
    onDecorationDoubleTap: () -> Unit = {}
) {

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = offset.x
                translationY = offset.y
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .size(decorationSize)
            .combinedClickable(
                interactionSource = null,
                indication = null,
                onClick = onDecorationTap,
                onDoubleClick = onDecorationDoubleTap
            )
            .pointerInput(offset, scale, rotation) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDecorationTap()
                        onDrag(
                            rotatedDragAmount(
                                rotation,
                                scale,
                                dragAmount
                            )
                        )
                    },
                    onDragEnd = onDragEnd
                )
            }
    ) {
        if (isSelected) {
            GestureInputHandle(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * scale,
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * scale
                    ),
                onTransformStart = onTransformStart,
                onTransform = onTransform,
                onTransformEnd = onTransformEnd
            )
            TapInputHandle(
                onTap = onTapDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * scale,
                        -((GESTURE_INPUT_HANDLE_SIZE / 2) * scale)
                    )
            )
        }
    }
}

@Composable
private fun TextItem(
    decoration: Decoration.Text,
    isSelected: Boolean,
    isEditing: Boolean,
    currentOffset: Offset,
    currentScale: Float,
    currentRotation: Float,
    onTextChanged: (String) -> Unit,
    onFinishEditing: () -> Unit
) {
    val borderModifier = if (isSelected) Modifier.border(
        1.dp,
        MaterialTheme.colorScheme.primary
    ) else Modifier

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = currentOffset.x
                translationY = currentOffset.y
                scaleX = currentScale
                scaleY = currentScale
                rotationZ = currentRotation
            }
            .wrapContentSize()
    )
    {
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        var textFieldValue by remember {
            mutableStateOf(
                TextFieldValue(
                    text = decoration.text,
                    selection = TextRange(decoration.text.length)
                )
            )
        }
        val measurer = rememberTextMeasurer()
        val textSize = 24.sp.nonScaledSp
        val textColor = colorResource(decoration.color)
        val strokeColor = colorResource(decoration.strokeColor)
        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onTextChanged(it.text)
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = textSize,
                // カスタムの文字描画を上書きするため、元の描画は透明にする
                color = colorResource(R.color.transparent),
                textAlign = TextAlign.Start,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            readOnly = !isEditing,
            singleLine = true,
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onFinishEditing()
                }
            ),
            modifier = Modifier
                .then(borderModifier)
                .padding(TEXT_ITEM_PADDING)
                .focusRequester(focusRequester)
                .width(IntrinsicSize.Min)
                .drawWithContent {
                    // 基本的な文字設定
                    val layoutResult = measurer.measure(
                        text = AnnotatedString(decoration.text),
                        style = TextStyle(
                            fontFamily = decoration.font.value,
                            fontWeight = FontWeight(decoration.width),
                            color = Color.White,
                            fontSize = textSize
                        )
                    )
                    // 枠線
                    drawText(
                        textLayoutResult = layoutResult,
                        drawStyle = Stroke(width = decoration.strokeWidth, join = StrokeJoin.Round),
                        color = strokeColor
                    )
                    // 塗りつぶし
                    drawText(
                        textLayoutResult = layoutResult,
                        drawStyle = Fill,
                        color = textColor
                    )
                    // 最後に描画しないと入力カーソルが埋もれて消えてしまうため、明示的に最後に描画
                    drawContent()
                }
        )
        LaunchedEffect(isEditing) {
            if (isEditing) {
                focusRequester.requestFocus()
            } else {
                focusManager.clearFocus()
                onFinishEditing()
            }
        }
        if (isSelected) {
            TransformHandleIcon(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = 1 / currentScale
                        scaleY = 1 / currentScale
                    }
                    .align(Alignment.BottomEnd)
                    .offset(
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale,
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale
                    )
            )
            DeleteIcon(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = 1 / currentScale
                        scaleY = 1 / currentScale
                    }
                    .align(Alignment.TopEnd)
                    .offset(
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale,
                        -((GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale)
                    )
            )
        }
    }
}

@Composable
private fun StickerItem(
    decoration: Decoration.Sticker,
    isSelected: Boolean,
    currentOffset: Offset,
    currentScale: Float,
    currentRotation: Float,
) {
    val borderModifier = if (isSelected) Modifier.border(
        1.dp,
        MaterialTheme.colorScheme.primary
    ) else Modifier

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = currentOffset.x
                translationY = currentOffset.y
                scaleX = currentScale
                scaleY = currentScale
                rotationZ = currentRotation
            }
            .wrapContentSize()
    )
    {
        Image(
            painter = painterResource(decoration.resId),
            contentDescription = decoration.label,
            modifier = Modifier
                .then(borderModifier)
        )
        if (isSelected) {
            TransformHandleIcon(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = 1 / currentScale
                        scaleY = 1 / currentScale
                    }
                    .align(Alignment.BottomEnd)
                    .offset(
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale,
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale
                    )
            )
            DeleteIcon(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = 1 / currentScale
                        scaleY = 1 / currentScale
                    }
                    .align(Alignment.TopEnd)
                    .offset(
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale,
                        -((GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale)
                    )
            )
        }
    }
}

@Composable
private fun ImageItem(
    decoration: Decoration.Image,
    isSelected: Boolean,
    currentOffset: Offset,
    currentScale: Float,
    currentRotation: Float,
) {
    val borderModifier = if (isSelected) Modifier.border(
        1.dp,
        MaterialTheme.colorScheme.primary
    ) else Modifier

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = currentOffset.x
                translationY = currentOffset.y
                scaleX = currentScale
                scaleY = currentScale
                rotationZ = currentRotation
            }
            .wrapContentSize()
    )
    {
        AsyncImage(
            // TODO: Decorationとは別なプロパティとして画像データを保持することにする
            model = decoration,
            contentDescription = null,
            modifier = Modifier
                .size(IMAGE_SIZE_DEFAULT)
                .then(borderModifier)
        )
        if (isSelected) {
            TransformHandleIcon(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = 1 / currentScale
                        scaleY = 1 / currentScale
                    }
                    .align(Alignment.BottomEnd)
                    .offset(
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale,
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale
                    )
            )
            DeleteIcon(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = 1 / currentScale
                        scaleY = 1 / currentScale
                    }
                    .align(Alignment.TopEnd)
                    .offset(
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale,
                        -((GESTURE_INPUT_HANDLE_SIZE / 2) * currentScale)
                    )
            )
        }
    }
}


@Composable
private fun GestureInputHandle(
    onTransformStart: () -> Unit,
    onTransform: (Offset) -> Unit,
    onTransformEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(GESTURE_INPUT_HANDLE_SIZE)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        onTransformStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onTransform(dragAmount)
                    },
                    onDragEnd = onTransformEnd
                )
            }
    )
}

@Composable
private fun TapInputHandle(
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(GESTURE_INPUT_HANDLE_SIZE)
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onTap
            )
    )
}

@Composable
private fun TransformHandleIcon(
    modifier: Modifier
) {
    Icon(
        imageVector = Icons.Default.OpenWith,
        contentDescription = "Zoom and Rotate",
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .size(GESTURE_INPUT_HANDLE_SIZE)
            .padding(4.dp)
    )
}

@Composable
private fun DeleteIcon(
    modifier: Modifier
) {
    Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = "Delete",
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .size(GESTURE_INPUT_HANDLE_SIZE)
            .padding(4.dp)
    )
}

private val GESTURE_INPUT_HANDLE_SIZE = 24.dp
private val TEXT_ITEM_PADDING = 8.dp
private val IMAGE_SIZE_DEFAULT = 64.dp

@Preview
@Composable
private fun EditScreenPreview() {
    FansaUchiwaTheme {
        EditScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun StickerItemPreview() {
    FansaUchiwaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            StickerItem(
                decoration = Decoration.Sticker(
                    label = "heart",
                    id = "",
                    offset = Offset.Zero,
                    rotation = 0f,
                    scale = 1f,
                    color = R.color.decoration_white,
                    strokeColor = R.color.decoration_black,
                    strokeWidth = 10f
                ),
                isSelected = true,
                currentOffset = Offset.Zero,
                currentScale = 1f,
                currentRotation = 0f
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TextItemPreview() {
    FansaUchiwaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TextItem(
                decoration = Decoration.Text(
                    text = "サンプルテキスト",
                    id = "",
                    offset = Offset.Zero,
                    rotation = 0f,
                    scale = 1f,
                    color = R.color.decoration_green,
                    strokeColor = R.color.decoration_red,
                    strokeWidth = 30f,
                    width = 900,
                    font = FontFamilies.ZEN_MARU_GOTHIC
                ),
                isSelected = true,
                isEditing = false,
                currentOffset = Offset.Zero,
                currentScale = 1f,
                currentRotation = 0f,
                onTextChanged = {},
                onFinishEditing = {}
            )
        }
    }
}
