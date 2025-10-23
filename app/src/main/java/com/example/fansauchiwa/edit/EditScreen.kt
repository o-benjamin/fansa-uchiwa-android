package com.example.fansauchiwa.edit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fansauchiwa.data.Decoration
import com.example.fansauchiwa.ui.StickerAsset
import com.example.fansauchiwa.ui.theme.FansaUchiwaTheme
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditScreen(
    viewModel: EditViewModel = hiltViewModel()
) {
    val decorations = viewModel.decorations
    val selectedDecoration = viewModel.selectedDecoration
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UchiwaPreview(
            decorations = decorations,
            selectedDecoration = selectedDecoration,
            onDecorationClick = { viewModel.selectDecoration(it) },
            onDecorationDragEnd = viewModel::updateDecorationPosition,
            onDecorationRotateEnd = viewModel::rotateSelectedDecoration,
            onDecorationScaleEnd = viewModel::scaleSelectedDecoration,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )

        // Decoration selector
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            val pagerState = rememberPagerState(pageCount = { DecorationTabType.entries.size })
            val tabIndex = pagerState.currentPage
            val scope = rememberCoroutineScope()
            ScrollableTabRow(
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

            HorizontalPager(
                state = pagerState,
            ) { page ->
                when (page) {
                    0 -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 64.dp),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(StickerAsset.entries) { sticker ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            viewModel.addDecoration(
                                                Decoration.Sticker(
                                                    label = sticker.type,
                                                    offset = Offset.Zero,
                                                    rotation = 0f,
                                                    scale = 1f
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
}

private fun onStickerDrag(
    decoration: Decoration.Sticker,
    dragAmount: Offset,
    currentOffset: Offset,
    onDecorationClick: (Decoration) -> Unit,
): Offset {
    onDecorationClick(decoration)
    val angleRad = Math.toRadians(decoration.rotation.toDouble()).toFloat()
    val cos = cos(angleRad)
    val sin = sin(angleRad)
    val rotatedDragAmount = Offset(
        x = dragAmount.x * cos - dragAmount.y * sin,
        y = dragAmount.x * sin + dragAmount.y * cos
    )
    return currentOffset + rotatedDragAmount
}

@Composable
fun UchiwaPreview(
    decorations: List<Decoration>,
    selectedDecoration: Decoration?,
    onDecorationClick: (Decoration?) -> Unit,
    onDecorationDragEnd: (Decoration, Offset) -> Unit,
    onDecorationRotateEnd: (Float) -> Unit,
    onDecorationScaleEnd: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clickable { onDecorationClick(null) },
        contentAlignment = Alignment.Center
    ) {
        // TODO: make Uchiwa shape
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        decorations.forEach { decoration ->
            val isSelected = decoration == selectedDecoration
            when (decoration) {
                is Decoration.Sticker -> {
                    var currentOffset by remember { mutableStateOf(decoration.offset) }
                    var currentScale by remember { mutableFloatStateOf(decoration.scale) }
                    var currentRotation by remember { mutableFloatStateOf(decoration.rotation) }

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
                            .pointerInput(decoration) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        currentOffset = onStickerDrag(
                                            decoration,
                                            dragAmount,
                                            currentOffset,
                                            onDecorationClick
                                        )
                                    },
                                    onDragEnd = { onDecorationDragEnd(decoration, currentOffset) }
                                )
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onDecorationClick(decoration) })
                    {
                        Image(
                            painter = painterResource(decoration.resId),
                            contentDescription = decoration.label,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(borderModifier)
                        )
                        if (isSelected) {
                            ZoomAndRotate(
                                center = Offset(decoration.offset.x, decoration.offset.y),
                                onScale = { currentScale = it },
                                onScaleEnd = { onDecorationScaleEnd(it) },
                                onRotate = { currentRotation = it },
                                onRotateEnd = { onDecorationRotateEnd(it) },
                                modifier = Modifier.align(Alignment.BottomEnd)
                            )
                        }
                    }
                }

                is Decoration.Text -> {
                    var offset by remember { mutableStateOf(decoration.offset) }
                    val borderModifier = if (isSelected) Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary
                    ) else Modifier
                    Text(
                        text = decoration.text,
                        modifier = Modifier
                            .offset(offset.x.dp, offset.y.dp)
                            .pointerInput(decoration) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onDecorationClick(decoration)
                                        offset += dragAmount
                                    },
                                    onDragEnd = { onDecorationDragEnd(decoration, offset) }
                                )
                            }
                            .clickable { onDecorationClick(decoration) }
                            .then(borderModifier)
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoomAndRotate(
    center: Offset,
    onRotate: (Float) -> Unit,
    onRotateEnd: (Float) -> Unit,
    onScale: (Float) -> Unit,
    onScaleEnd: (Float) -> Unit,
    modifier: Modifier
) {
    var rotation by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }

    IconButton(
        onClick = {},
        modifier = modifier
            .offset(12.dp, 12.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .size(24.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startPosition ->
                        // 必要ならなにか定義する
                    },
                    onDrag = { change, offset ->
                        change.consume()
                        // ドラッグした距離からscaleを計算
//                        val dragMagnitude = offset.getDistance()
//                        val scaleFactor = 0.1f
//                        onScale(1f + (dragMagnitude * scaleFactor))

                        // ドラッグした角度からrotationを計算
//                        val newAngle = newVector.toAngleDegrees()
//                        rotation = newAngle - startAngle
//                        onRotate(rotation)
                    },
                    onDragEnd = {
                        onScaleEnd(scale)
                        onRotateEnd(rotation)
                    }
                )
            }
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Rotate",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(16.dp)
        )
    }
}

private fun Offset.toAngleDegrees(): Float {
    return atan2(y, x) * 180f / PI.toFloat()
}


@Preview
@Composable
private fun EditScreenPreview() {
    FansaUchiwaTheme {
        EditScreen()
    }
}
