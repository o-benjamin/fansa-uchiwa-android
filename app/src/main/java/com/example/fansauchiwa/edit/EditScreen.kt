package com.example.fansauchiwa.edit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            onDecorationDragEnd = viewModel::updateDecorationGraphic,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )

        EditPager(
            onAddSticker = viewModel::addDecoration,
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
    onDecorationDragEnd: (Decoration, Offset, Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clickable(
            interactionSource = null,
            indication = null
        ) { onDecorationClick(null) },
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
                var scaleDiff by remember { mutableFloatStateOf(0f) }
                var rotationDiff by remember { mutableFloatStateOf(0f) }
                val isSelected = decoration == selectedDecoration
                when (decoration) {
                    is Decoration.Sticker -> {
                        GestureInputLayer(
                            decoration = decoration,
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
                            }
                        )
                        StickerItem(
                            decoration = decoration,
                            isSelected = isSelected,
                            currentOffset = decoration.offset + offsetDiff,
                            currentScale = decoration.scale + scaleDiff,
                            currentRotation = decoration.rotation + rotationDiff
                        )

                        if (isSelected) {
                            val stickerSizePx = with(LocalDensity.current) { 100.dp.toPx() }

                            // Visual Handle (updates position every frame)
                            val visualHandleOffset = calculateHandleOffset(
                                baseOffset = decoration.offset + offsetDiff,
                                scale = decoration.scale + scaleDiff,
                                rotation = decoration.rotation + rotationDiff,
                                size = stickerSizePx,
                                corner = HandleCorner.BottomRight
                            )
                            TransformHandleIcon(
                                modifier = Modifier.graphicsLayer {
                                    translationX = visualHandleOffset.x
                                    translationY = visualHandleOffset.y
                                }
                            )

                            // Gesture Handle (position is fixed during a drag)
                            val gestureHandleOffset = calculateHandleOffset(
                                baseOffset = decoration.offset,
                                scale = decoration.scale,
                                rotation = decoration.rotation,
                                size = stickerSizePx,
                                corner = HandleCorner.BottomRight
                            )

                            var cumulativeOffset by remember { mutableStateOf(Offset.Zero) }

                            GestureInputHandle(
                                modifier = Modifier.graphicsLayer {
                                    translationX = gestureHandleOffset.x
                                    translationY = gestureHandleOffset.y
                                },
                                onTransformStart = { startPosition ->
                                    cumulativeOffset = Offset.Zero
                                },
                                onTransform = { dragAmount ->
                                    val center = decoration.offset
                                    val handleVector = gestureHandleOffset - center
                                    val transformation = calculateTransformations(
                                        dragAmount,
                                        cumulativeOffset,
                                        handleVector
                                    )
                                    cumulativeOffset += dragAmount
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

                    is Decoration.Text -> {
                        // TODO: implement
                    }
                }
            }
        }
    }
}

@Composable
private fun GestureInputLayer(
    decoration: Decoration.Sticker,
    onDecorationTap: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
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
            .size(100.dp)
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onDecorationTap
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
    )
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
            .size(100.dp)
    )
    {
        Image(
            painter = painterResource(decoration.resId),
            contentDescription = decoration.label,
            modifier = Modifier
                .fillMaxSize()
                .then(borderModifier)
        )
    }
}

@Composable
private fun GestureInputHandle(
    onTransformStart: (Offset) -> Unit,
    onTransform: (Offset) -> Unit,
    onTransformEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val handleSize = 48.dp
    Box(
        modifier = modifier
            .size(handleSize)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startPosition ->
                        onTransformStart(startPosition)
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
            .size(24.dp)
            .padding(4.dp)
    )
}

@Preview
@Composable
private fun EditScreenPreview() {
    FansaUchiwaTheme {
        EditScreen()
    }
}
