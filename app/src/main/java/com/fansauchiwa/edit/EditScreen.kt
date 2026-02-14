package com.fansauchiwa.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fansauchiwa.R
import com.fansauchiwa.ads.BannerAd
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.ImageReference
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.analytics.AnalyticsBackDialogActions
import com.fansauchiwa.data.captureHighResBitmap
import com.fansauchiwa.edit.decorationitem.ImageItemContent
import com.fansauchiwa.edit.decorationitem.StickerItemContent
import com.fansauchiwa.edit.decorationitem.TextItemContent
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditScreen(
    viewModel: EditViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPreview: (String) -> Unit,
    onNavigateToImagePreview: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    val showBackDialog = remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    LaunchedEffect(Unit) {
        viewModel.logScreenView()
    }

    uiState.userMessage?.let { userMessage ->
        val snackbarText = stringResource(userMessage)
        LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
            snackbarHostState.showSnackbar(snackbarText)
            viewModel.snackbarMessageShown()
        }
    }

    LaunchedEffect(uiState.savedPath) {
        uiState.savedPath?.let {
            viewModel.resetIsUchiwaSaved()
            onPreview(URLEncoder.encode(it, "UTF-8"))
        }
    }

    // バックキー押下時にダイアログを表示
    BackHandler {
        viewModel.logEvent(AnalyticsActions.TAP_EDIT_BACK)
        showBackDialog.value = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.logEvent(AnalyticsActions.TAP_EDIT_BACK)
                        showBackDialog.value = true
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.logEvent(AnalyticsActions.TAP_EDIT_COMPLETE)
                            viewModel.saveUchiwa { uchiwaId ->
                                viewModel.resetEditUiState()
                                coroutineScope.launch {
                                    // uiStateを同期的にリセットしても、再コンポーズが非同期で実行されるため、描画完了が期待されるフレーム分待つ
                                    withFrameMillis { }
                                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                    val highResBitmap = captureHighResBitmap(
                                        graphicsLayer,
                                        density,
                                        layoutDirection
                                    ).asAndroidBitmap()
                                    viewModel.saveUchiwaBitmap(highResBitmap, uchiwaId)
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.complete),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        },
        bottomBar = {
            BannerAd(
                LocalContext.current,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.imePadding()
            )
        },
        floatingActionButton = {
            if (uiState.isDeletingImage) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = viewModel::cancelImageDeletionMode,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    val selectedCount = uiState.selectedDeletingImages.size
                    if (selectedCount > 0) {
                        FloatingActionButton(
                            onClick = viewModel::deleteSelectedImages,
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Text(
                                text = stringResource(R.string.delete_images, selectedCount),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.414f)
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        }
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
                        modifier = Modifier.fillMaxSize(),
                        images = uiState.images,
                        uchiwaColor = uiState.uchiwaColor,
                        backgroundColor = uiState.backgroundColor
                    )

                    if (uiState.isDeletingImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colorResource(R.color.black).copy(alpha = 0.5f))
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { /* タップを無効化 */ }
                        )
                    }
                }
                UndoRedoRow(
                    canUndo = uiState.canUndo,
                    canRedo = uiState.canRedo,
                    onUndoClick = viewModel::undo,
                    onRedoClick = viewModel::redo,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }

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
                onSecondBorderColorSelected = { color ->
                    uiState.selectedDecorationId?.let { decorationId ->
                        viewModel.updateSecondBorderColor(decorationId, color)
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
                onSecondBorderWeightChanged = { weight ->
                    uiState.selectedDecorationId?.let { decorationId ->
                        viewModel.updateSecondBorderWidth(decorationId, weight)
                    }
                },
                onImagePicked = { uri ->
                    val encodedUri = URLEncoder.encode(uri.toString(), "UTF-8")
                    onNavigateToImagePreview(encodedUri)
                },
                onImageClick = viewModel::addDecoration,
                onImageLongPress = viewModel::startImageDeletionMode,
                onUchiwaColorSelected = viewModel::updateUchiwaColor,
                onBackgroundColorSelected = viewModel::updateBackgroundColor,
                selectedDecoration = uiState.decorations.find { it.id == uiState.selectedDecorationId },
                allImages = uiState.allImages,
                isDeletingImage = uiState.isDeletingImage,
                selectedDeletingImages = uiState.selectedDeletingImages,
                onImageToggleSelection = viewModel::toggleImageSelection,
                uchiwaColor = uiState.uchiwaColor,
                backgroundColor = uiState.backgroundColor,
                decorations = uiState.decorations,
                selectedDecorationId = uiState.selectedDecorationId,
                onDecorationClick = viewModel::selectDecoration,
                onMoveDecoration = viewModel::moveDecoration,
                modifier = Modifier
            )
        }
    }

    // 保存確認ダイアログ
    if (showBackDialog.value) {
        AlertDialog(
            onDismissRequest = { showBackDialog.value = false },
            title = {
                Text(text = stringResource(R.string.confirm_save_dialog_title))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logEvent(
                            AnalyticsActions.TAP_EDIT_BACK_DIALOG,
                            mapOf("action" to AnalyticsBackDialogActions.ACTION_SAVE)
                        )
                        showBackDialog.value = false
                        viewModel.saveUchiwa { uchiwaId ->
                            viewModel.resetEditUiState()
                            coroutineScope.launch {
                                withFrameMillis { }
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                viewModel.saveUchiwaBitmap(bitmap, uchiwaId)
                                // 保存完了後に戻る
                                onBack()
                            }
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.save_and_back))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.logEvent(
                            AnalyticsActions.TAP_EDIT_BACK_DIALOG,
                            mapOf("action" to AnalyticsBackDialogActions.ACTION_DELETE)
                        )
                        showBackDialog.value = false
                        onBack()
                    }
                ) {
                    Text(text = stringResource(R.string.discard))
                }
            }
        )
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
    images: List<ImageReference> = emptyList(),
    uchiwaColor: Color,
    backgroundColor: Color
) {
    val focusManager = LocalFocusManager.current
    var uchiwaSize by remember { mutableStateOf<IntSize?>(null) }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .clickable(
                interactionSource = null,
                indication = null
            ) {
                onBackgroundTap()
                onDoneTextEdit()
                focusManager.clearFocus()
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.uchiwa_shape),
            contentDescription = null,
            colorFilter = ColorFilter.tint(uchiwaColor),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                // 保存するときに拡大しやすいよう、A3用紙の比率にする
                .aspectRatio(1.414f)
                .onSizeChanged { size ->
                    uchiwaSize = size
                },

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
                            onDrag = { dragAmount ->
                                offsetDiff = calculateClampedOffset(
                                    currentConfirmedOffset = decoration.offset,
                                    cumulativeOffset = offsetDiff,
                                    dragAmount = dragAmount,
                                    boundarySize = uchiwaSize
                                )
                            },
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
                                        6f
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
                            onDrag = { dragAmount ->
                                offsetDiff = calculateClampedOffset(
                                    currentConfirmedOffset = decoration.offset,
                                    cumulativeOffset = offsetDiff,
                                    dragAmount = dragAmount,
                                    boundarySize = uchiwaSize
                                )
                            },
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
                        val decorationSize = with(LocalDensity.current) {
                            Size(
                                IMAGE_SIZE_DEFAULT.toPx(),
                                IMAGE_SIZE_DEFAULT.toPx()
                            )
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
                            decorationSize = imageDpSize,
                            isSelected = isSelected,
                            onDecorationTap = { onDecorationTap(decoration.id) },
                            onDrag = { dragAmount ->
                                offsetDiff = calculateClampedOffset(
                                    currentConfirmedOffset = decoration.offset,
                                    cumulativeOffset = offsetDiff,
                                    dragAmount = dragAmount,
                                    boundarySize = uchiwaSize
                                )
                            },
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
                            currentRotation = decoration.rotation + rotationDiff,
                            imagePath = images.find { it.id == decoration.imageId }?.path
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
                        (GESTURE_INPUT_HANDLE_SIZE / 2),
                        (GESTURE_INPUT_HANDLE_SIZE / 2)
                    ),
                onTransformStart = onTransformStart,
                onTransform = onTransform,
                onTransformEnd = onTransformEnd,
                scale = scale
            )
            TapInputHandle(
                onTap = onTapDelete,
                scale = scale,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = 1 / scale
                        scaleY = 1 / scale
                    }
                    .align(Alignment.TopEnd)
                    .offset(
                        (GESTURE_INPUT_HANDLE_SIZE / 2),
                        -(GESTURE_INPUT_HANDLE_SIZE / 2)
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
        val textSize = 24.sp.nonScaledSp
        TextItemContent(
            decoration = decoration,
            isSelected = isSelected,
            isEditing = isEditing,
            textSize = textSize,
            modifier = borderModifier,
            onTextChanged = onTextChanged,
            onFinishEditing = onFinishEditing
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
        StickerItemContent(
            decoration = decoration,
            modifier = borderModifier,
            isSelected = isSelected
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
    imagePath: String? = null,
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
        ImageItemContent(
            decoration = decoration,
            imagePath = imagePath,
            size = IMAGE_SIZE_DEFAULT,
            modifier = Modifier.then(borderModifier),
            isSelected = isSelected
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
    scale: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(GESTURE_INPUT_HANDLE_SIZE / scale)
            .pointerInput(onTransform, onTransformStart, onTransformEnd) {
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
    scale: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(GESTURE_INPUT_HANDLE_SIZE / scale)
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
        painter = painterResource(R.drawable.outline_arrows_outward_24),
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

@Composable
private fun UndoRedoRow(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(start = 4.dp, bottom = 4.dp)
            .clip(CircleShape)
            .background(
                brush = SolidColor(MaterialTheme.colorScheme.surface),
                alpha = 0.75f
            ),
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(
            onClick = onUndoClick,
            enabled = canUndo
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = stringResource(R.string.undo),
                tint = if (canUndo) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }
        IconButton(
            onClick = onRedoClick,
            enabled = canRedo,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Redo,
                contentDescription = stringResource(R.string.redo),
                tint = if (canRedo) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }
    }
}

private val GESTURE_INPUT_HANDLE_SIZE = 24.dp
internal val TEXT_ITEM_PADDING = 8.dp
private val IMAGE_SIZE_DEFAULT = 64.dp

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
                    color = Color(0xFFFFFFFF),
                    strokeColor = Color(0xFF000000),
                    strokeWidth = 10f,
                    secondStrokeColor = Color(0xFFFF0000),
                    secondStrokeWidth = 5f
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
                    color = Color(0xFF00FF00),
                    strokeColor = Color(0xFFFF0000),
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

@Preview()
@Composable
private fun UndoRedoRowPreview() {
    MaterialTheme {
        Column {
            UndoRedoRow(
                canUndo = true,
                canRedo = true,
                onUndoClick = {},
                onRedoClick = {}
            )
            UndoRedoRow(
                canUndo = false,
                canRedo = false,
                onUndoClick = {},
                onRedoClick = {}
            )
        }
    }
}
