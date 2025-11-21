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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
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
import com.example.fansauchiwa.R
import com.example.fansauchiwa.data.Decoration
import com.example.fansauchiwa.ui.theme.FansaUchiwaTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditScreen(
    viewModel: EditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UchiwaPreview(
            decorations = uiState.decorations,
            selectedDecoration = uiState.selectedDecoration,
            onDecorationClick = viewModel::selectDecoration,
            onDecorationDoubleClick = viewModel::onDecorationDoubleClick,
            onDecorationDragEnd = viewModel::updateDecorationGraphic,
            onTextChanged = viewModel::updateText,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )

        EditPager(
            onStickerClick = viewModel::addDecoration,
            onTextClick = viewModel::addDecoration,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )
    }
}

@Composable
fun UchiwaPreview(
    decorations: List<Decoration>,
    selectedDecoration: Decoration?,
    onDecorationClick: (Decoration?) -> Unit,
    onDecorationDoubleClick: (Decoration) -> Unit,
    onDecorationDragEnd: (Decoration, Offset, Float, Float) -> Unit,
    onTextChanged: (String, Decoration.Text) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = modifier.clickable(
            interactionSource = null,
            indication = null
        ) {
            onDecorationClick(null)
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
            key(decoration) {
                var offsetDiff by remember { mutableStateOf(Offset.Zero) }
                var cumulativeOffset by remember { mutableStateOf(Offset.Zero) }
                var scaleDiff by remember { mutableFloatStateOf(0f) }
                var rotationDiff by remember { mutableFloatStateOf(0f) }
                val isSelected = decoration == selectedDecoration
                when (decoration) {
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
                            decoration = decoration,
                            decorationSize = decorationDpSize,
                            isSelected = isSelected,
                            onDecorationTap = { onDecorationClick(decoration) },
                            onDrag = { offsetDiff += it },
                            onDragEnd = {
                                onDecorationDragEnd(
                                    decoration,
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
                                    decoration,
                                    offsetDiff,
                                    scaleDiff,
                                    rotationDiff
                                )
                                offsetDiff = Offset.Zero
                                scaleDiff = 0f
                                rotationDiff = 0f
                            }
                        )
                        StickerItem(
                            decoration = decoration,
                            isSelected = isSelected,
                            currentOffset = decoration.offset + offsetDiff,
                            currentScale = decoration.scale + scaleDiff,
                            currentRotation = decoration.rotation + rotationDiff
                        )
                    }

                    is Decoration.Text -> {
                        TextItem(
                            decoration = decoration,
                            isSelected = isSelected,
                            isEditing = decoration.isEditingText,
                            currentOffset = decoration.offset + offsetDiff,
                            currentScale = decoration.scale + scaleDiff,
                            currentRotation = decoration.rotation + rotationDiff,
                            onTextChanged = { onTextChanged(it, decoration) }
                        )
                        val textMeasurer = rememberTextMeasurer()
                        val decorationSize = textMeasurer.measure(
                            decoration.text,
                            TextStyle(fontSize = 24.sp)
                        ).size.toSize()
                        val decorationDpSize = with(LocalDensity.current) {
                            decorationSize.toDpSize() + DpSize(16.dp, 16.dp)
                        }
                        val handleOffset = calculateHandleOffset(
                            baseOffset = decoration.offset,
                            scale = decoration.scale,
                            rotation = decoration.rotation,
                            decorationSize = decorationSize,
                            corner = HandleCorner.BottomRight
                        )
                        GestureInputLayer(
                            decoration = decoration,
                            decorationSize = decorationDpSize,
                            isSelected = isSelected,
                            onDecorationTap = { onDecorationClick(decoration) },
                            onDecorationDoubleTap = { onDecorationDoubleClick(decoration) },
                            onDrag = { offsetDiff += it },
                            onDragEnd = {
                                onDecorationDragEnd(
                                    decoration,
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
                                    decoration,
                                    offsetDiff,
                                    scaleDiff,
                                    rotationDiff
                                )
                                offsetDiff = Offset.Zero
                                scaleDiff = 0f
                                rotationDiff = 0f
                            }
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
    decoration: Decoration,
    decorationSize: DpSize,
    isSelected: Boolean,
    onDecorationTap: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onTransformStart: () -> Unit,
    onTransform: (Offset) -> Unit,
    onTransformEnd: () -> Unit,
    onDecorationDoubleTap: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = decoration.offset.x
                translationY = decoration.offset.y
                scaleX = decoration.scale
                scaleY = decoration.scale
                rotationZ = decoration.rotation
            }
            .size(decorationSize)
            .combinedClickable(
                interactionSource = null,
                indication = null,
                onClick = onDecorationTap,
                onDoubleClick = onDecorationDoubleTap
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDecorationTap()
                        onDrag(
                            rotatedDragAmount(
                                decoration.rotation,
                                decoration.scale,
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
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * decoration.scale,
                        (GESTURE_INPUT_HANDLE_SIZE / 2) * decoration.scale
                    ),
                onTransformStart = onTransformStart,
                onTransform = onTransform,
                onTransformEnd = onTransformEnd
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
    onTextChanged: (String) -> Unit
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
        TextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onTextChanged(it.text)
            },
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = colorResource(R.color.transparent),
                unfocusedIndicatorColor = colorResource(R.color.transparent),
                focusedContainerColor = colorResource(R.color.transparent),
                unfocusedContainerColor = colorResource(R.color.transparent)
            ),
            readOnly = !isEditing,
            singleLine = true,
            modifier = Modifier
                .align(Alignment.Center)
                .then(borderModifier)
                .focusRequester(focusRequester)
        )
        LaunchedEffect(isEditing) {
            if (isEditing) {
                focusRequester.requestFocus()
            } else {
                focusManager.clearFocus()
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

private val GESTURE_INPUT_HANDLE_SIZE = 24.dp

@Preview
@Composable
private fun EditScreenPreview() {
    FansaUchiwaTheme {
        EditScreen()
    }
}
