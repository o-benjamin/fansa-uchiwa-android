package com.fansauchiwa.edit

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint as AndroidPaint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.TooltipState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
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
import com.fansauchiwa.edit.decorationitem.PuffyTextRenderer
import com.fansauchiwa.edit.decorationitem.StickerItemContent
import com.fansauchiwa.edit.decorationitem.TextItemContent
import com.fansauchiwa.edit.decorationitem.generateSdfTexture
import com.fansauchiwa.edit.decorationitem.supportsPukuPukuEffect
import com.fansauchiwa.edit.pager.EditPager
import com.fansauchiwa.edit.pager.EditPagerActions
import com.fansauchiwa.edit.pager.EditPagerUiState
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.util.FansaHapticType
import com.fansauchiwa.ui.util.rememberFansaHapticManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    viewModel: EditViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPreview: (String) -> Unit,
    onNavigateToImagePreview: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    val showBackDialog = remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current
    val hapticManager = rememberFansaHapticManager()
    val completionTooltipState = rememberTooltipState(isPersistent = true)

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

    LaunchedEffect(uiState.showCompletionTooltip) {
        if (uiState.showCompletionTooltip) {
            completionTooltipState.show()
        } else {
            completionTooltipState.dismiss()
        }
    }

    // バックキー押下時にダイアログを表示
    BackHandler {
        viewModel.logEvent(AnalyticsActions.TAP_EDIT_BACK)
        showBackDialog.value = true
    }

    val showExportDialog = remember { mutableStateOf(false) }

    // シェイク検知
    val isDebuggable = remember {
        (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    if (isDebuggable) {
        ShakeDetector(onShake = { showExportDialog.value = true })
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
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                    IconButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://fansauchiwa-578d22ff.web.app/guide.html".toUri()
                            )
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = stringResource(R.string.help)
                        )
                    }
                    CompleteEditButton(
                        completionTooltipState = completionTooltipState,
                        onTooltipDismissed = viewModel::onTooltipDismissed,
                        onClick = {
                            viewModel.logEvent(AnalyticsActions.TAP_EDIT_COMPLETE)
                            viewModel.saveUchiwa { uchiwaId ->
                                viewModel.resetEditUiState()
                                coroutineScope.launch {
                                    // uiStateを同期的にリセットしても、再コンポーズが非同期で実行されるため、描画完了が期待されるフレーム分待つ
                                    delay(150L)
                                    val highResBitmap = captureHighResBitmap(
                                        graphicsLayer,
                                        density,
                                        layoutDirection
                                    ).asAndroidBitmap()
                                    viewModel.saveUchiwaBitmap(highResBitmap, uchiwaId)
                                }
                            }
                        }
                    )
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
                modifier = Modifier
                    .imePadding()
                    .padding(bottom = 48.dp)
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
        Box(modifier = Modifier.fillMaxSize()) {
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
                            onDecorationTap = { decorationId ->
                                if (uiState.selectedDecorationId == decorationId) {
                                    viewModel.startEditingText(decorationId)
                                } else {
                                    viewModel.selectDecoration(decorationId)
                                }
                            },
                            onBackgroundTap = {
                                viewModel.unSelectDecoration()
                                viewModel.finishEditingText()
                            },
                            onDecorationDragEnd = viewModel::updateDecorationGraphic,
                            onTapDelete = viewModel::deleteDecoration,
                            onTapDuplicate = viewModel::duplicateDecoration,
                            modifier = Modifier.fillMaxSize(),
                            images = uiState.images,
                            uchiwaColor = uiState.uchiwaColor,
                            backgroundColor = uiState.backgroundColor,
                            overallBorderColor = uiState.overallBorderColor,
                            overallBorderWidth = uiState.overallBorderWidth,
                            isOverallBorderPuffyEnabled = uiState.isOverallBorderPuffyEnabled,
                            isDragging = uiState.isDragging,
                            onDraggingChanged = viewModel::setDragging
                        )


                    }
                    UndoRedoRow(
                        canUndo = uiState.canUndo,
                        canRedo = uiState.canRedo,
                        onUndoClick = {
                            hapticManager.perform(FansaHapticType.VIRTUAL_KEY)
                            viewModel.undo()
                        },
                        onRedoClick = {
                            hapticManager.perform(FansaHapticType.VIRTUAL_KEY)
                            viewModel.redo()
                        },
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }

                val selectedDecoration =
                    uiState.decorations.find { it.id == uiState.selectedDecorationId }

                EditPager(
                    state = EditPagerUiState(
                        selectedDecoration = selectedDecoration,
                        allImages = uiState.allImages,
                        isDeletingImage = uiState.isDeletingImage,
                        selectedDeletingImages = uiState.selectedDeletingImages,
                        uchiwaColor = uiState.uchiwaColor,
                        backgroundColor = uiState.backgroundColor,
                        overallBorderColor = uiState.overallBorderColor,
                        overallBorderWidth = uiState.overallBorderWidth,
                        isOverallBorderPuffyEnabled = uiState.isOverallBorderPuffyEnabled,
                        decorations = uiState.decorations,
                        selectedDecorationId = uiState.selectedDecorationId,
                        isPukuPukuSupported = uiState.isPukuPukuSupported
                    ),
                    actions = EditPagerActions(
                        onStickerSelected = viewModel::addStickerDecoration,
                        onAddText = viewModel::addTextDecoration,
                        onFontChanged = { font ->
                            uiState.selectedDecorationId?.let { id ->
                                viewModel.updateFont(id, font)
                            }
                        },
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
                        onPuffyEnabledChanged = { isPuffyEnabled ->
                            uiState.selectedDecorationId?.let { decorationId ->
                                viewModel.updatePuffyEnabled(decorationId, isPuffyEnabled)
                            }
                        },
                        onUnsupportedPuffyClick = viewModel::notifyPukuPukuUnsupported,
                        onImagePicked = { uri ->
                            val encodedUri = URLEncoder.encode(uri.toString(), "UTF-8")
                            onNavigateToImagePreview(encodedUri)
                        },
                        onImageSelected = viewModel::addImageDecoration,
                        onImageLongPress = viewModel::startImageDeletionMode,
                        onImageToggleSelection = viewModel::toggleImageSelection,
                        onUchiwaColorSelected = viewModel::updateUchiwaColor,
                        onBackgroundColorSelected = viewModel::updateBackgroundColor,
                        onOverallBorderColorSelected = viewModel::updateOverallBorderColor,
                        onOverallBorderWeightChanged = viewModel::updateOverallBorderWidth,
                        onOverallBorderPuffyEnabledChanged = viewModel::updateOverallBorderPuffyEnabled,
                        onDecorationClick = viewModel::selectDecoration,
                        onMoveDecoration = { fromIndex, toIndex ->
                            hapticManager.perform(FansaHapticType.VIRTUAL_KEY)
                            viewModel.moveDecoration(fromIndex, toIndex)
                        }
                    ),
                    modifier = Modifier
                )
            }

            val editingTextId = uiState.editingTextId
            if (editingTextId != null) {
                val editingDecoration = remember(editingTextId, uiState.decorations) {
                    uiState.decorations
                        .filterIsInstance<Decoration.Text>()
                        .find { it.id == editingTextId }
                }
                editingDecoration?.let { decoration ->
                    key(editingTextId) {
                        TextInputBar(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .imePadding(),
                            initialText = decoration.text,
                            onTextChanged = { newText ->
                                viewModel.updateText(editingTextId, newText)
                            },
                            onDone = { viewModel.finishEditingText() },
                            onDismissBlocked = { viewModel.notifyDismissBlocked() }
                        )
                    }
                }

            }
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
                                delay(150L)
                                val highResBitmap = captureHighResBitmap(
                                    graphicsLayer,
                                    density,
                                    layoutDirection
                                ).asAndroidBitmap()
                                viewModel.saveUchiwaBitmap(highResBitmap, uchiwaId)
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

    // 画像削除警告ダイアログ
    if (uiState.showImageDeleteWarningDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissImageDeleteWarningDialog() },
            title = {
                Text(text = stringResource(R.string.delete_image_warning_title))
            },
            text = {
                Text(text = stringResource(R.string.delete_image_warning_message))
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.proceedImageDeletion() }
                ) {
                    Text(text = stringResource(R.string.delete_image_warning_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissImageDeleteWarningDialog() }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    // テンプレートエクスポートダイアログ（シェイクで表示）
    if (showExportDialog.value) {
        AlertDialog(
            onDismissRequest = { showExportDialog.value = false },
            title = {
                Text(text = stringResource(R.string.export_template))
            },
            text = {
                Text(text = stringResource(R.string.export_template_dialog_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportDialog.value = false
                        viewModel.exportTemplateCode { uchiwaId ->
                            viewModel.resetEditUiState()
                            coroutineScope.launch {
                                delay(150L)
                                val highResBitmap = captureHighResBitmap(
                                    graphicsLayer,
                                    density,
                                    layoutDirection
                                ).asAndroidBitmap()
                                viewModel.saveUchiwaBitmap(highResBitmap, uchiwaId)
                            }
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExportDialog.value = false }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun UchiwaPreview(
    decorations: List<Decoration>,
    selectedDecorationId: String?,
    onDecorationTap: (String) -> Unit,
    onBackgroundTap: () -> Unit,
    onDecorationDragEnd: (String, Offset, Float, Float) -> Unit,
    onTapDelete: (String) -> Unit,
    onTapDuplicate: (String) -> Unit,
    modifier: Modifier = Modifier,
    images: List<ImageReference> = emptyList(),
    uchiwaColor: Color,
    backgroundColor: Color,
    overallBorderColor: Color,
    overallBorderWidth: Float,
    isOverallBorderPuffyEnabled: Boolean,
    isDragging: Boolean,
    onDraggingChanged: (Boolean) -> Unit
) {
    var uchiwaSize by remember { mutableStateOf<IntSize?>(null) }
    var decorationLayerSize by remember { mutableStateOf(IntSize.Zero) }
    var snappedX by remember { mutableStateOf(false) }
    var snappedY by remember { mutableStateOf(false) }
    var rawOffsetDiff by remember { mutableStateOf(Offset.Zero) }
    var offsetDiff by remember { mutableStateOf(Offset.Zero) }
    var cumulativeOffset by remember { mutableStateOf(Offset.Zero) }
    var scaleDiff by remember { mutableFloatStateOf(1f) }
    var rotationDiff by remember { mutableFloatStateOf(0f) }
    var wasRotationSnapped by remember { mutableStateOf(false) }
    var overallBorderBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var overallBorderSdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val decorationVisualLayer = rememberGraphicsLayer()
    val snapThreshold = with(density) { 2.dp.toPx() }
    val hapticManager = rememberFansaHapticManager()
    val currentOnBackgroundTap by rememberUpdatedState(onBackgroundTap)
    val currentSelectedDecoration by rememberUpdatedState(
        decorations.find { it.id == selectedDecorationId }
    )
    val shouldRenderOverallBorder = overallBorderWidth > 0f && !isDragging

    fun clearOverallBorderBitmaps() {
        overallBorderBitmap?.recycle()
        overallBorderBitmap = null
        overallBorderSdfBitmap?.recycle()
        overallBorderSdfBitmap = null
    }

    fun resetDecorationTransform() {
        rawOffsetDiff = Offset.Zero
        offsetDiff = Offset.Zero
        cumulativeOffset = Offset.Zero
        scaleDiff = 1f
        rotationDiff = 0f
        wasRotationSnapped = false
        snappedX = false
        snappedY = false
    }

    fun commitDecorationTransform(decoration: Decoration) {
        onDecorationDragEnd(
            decoration.id,
            offsetDiff,
            calculateCommittedScaleDiff(decoration.scale, scaleDiff),
            rotationDiff
        )
        onDraggingChanged(false)
        resetDecorationTransform()
    }

    LaunchedEffect(selectedDecorationId) {
        onDraggingChanged(false)
        resetDecorationTransform()
    }

    DisposableEffect(Unit) {
        onDispose {
            clearOverallBorderBitmaps()
        }
    }

    LaunchedEffect(
        decorations,
        images,
        overallBorderWidth,
        overallBorderColor,
        isOverallBorderPuffyEnabled,
        shouldRenderOverallBorder,
        decorationLayerSize
    ) {
        clearOverallBorderBitmaps()
        if (!shouldRenderOverallBorder) return@LaunchedEffect
        if (decorationLayerSize.width <= 0 || decorationLayerSize.height <= 0) return@LaunchedEffect

        withFrameNanos { }
        val bufferBitmap = captureGraphicsLayerBitmap(
            graphicsLayer = decorationVisualLayer,
            density = density,
            layoutDirection = layoutDirection,
            targetSize = decorationLayerSize
        )
        val borderMaskBitmap = createOverallBorderMaskBitmap(
            sourceBitmap = bufferBitmap,
            overallBorderWidth = overallBorderWidth
        )
        bufferBitmap.recycle()
        if (borderMaskBitmap == null) return@LaunchedEffect

        if (isOverallBorderPuffyEnabled && supportsPukuPukuEffect()) {
            overallBorderSdfBitmap = generateSdfTexture(borderMaskBitmap)
        } else {
            overallBorderBitmap = createOverallBorderBitmap(
                maskBitmap = borderMaskBitmap,
                borderColor = overallBorderColor
            )
        }
        borderMaskBitmap.recycle()
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .pointerInput(selectedDecorationId) {
                if (selectedDecorationId == null) return@pointerInput
                detectTransformGesturesWithEnd(
                    onEnd = {
                        currentSelectedDecoration?.let(::commitDecorationTransform)
                        onDraggingChanged(false)
                    },
                    onGesture = { _, pan, zoom, _ ->
                        val decoration =
                            currentSelectedDecoration ?: return@detectTransformGesturesWithEnd
                        onDraggingChanged(true)
                        rawOffsetDiff = calculateClampedOffset(
                            currentConfirmedOffset = decoration.offset,
                            cumulativeOffset = rawOffsetDiff,
                            dragAmount = pan,
                            boundarySize = uchiwaSize
                        )
                        val snapResult = applySnapToCenter(
                            decorationOffset = decoration.offset,
                            offsetDiff = rawOffsetDiff,
                            snapThreshold = snapThreshold
                        )
                        offsetDiff = snapResult.offsetDiff
                        snappedX = snapResult.snappedX
                        snappedY = snapResult.snappedY

                        val targetScale =
                            (decoration.scale * scaleDiff * zoom).coerceIn(decoration.scaleRange())
                        scaleDiff = calculateScaleFactor(
                            baseScale = decoration.scale,
                            targetScale = targetScale
                        )
                    }
                )
            }
            .pointerInput(Unit) {
                detectNonConsumingTap {
                    currentOnBackgroundTap()
                }
            }
            .then(
                if (selectedDecorationId == null) {
                    Modifier.clip(UchiwaShape())
                } else {
                    Modifier
                }
            ),
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
        Box(modifier = Modifier.matchParentSize()) {
            if (shouldRenderOverallBorder) {
                when {
                    overallBorderSdfBitmap != null -> {
                        PuffyTextRenderer(
                            sdfTextureBitmap = overallBorderSdfBitmap!!,
                            baseColor = overallBorderColor,
                            scaleFactor = 1f,
                            modifier = Modifier.matchParentSize()
                        )
                    }

                    overallBorderBitmap != null -> {
                        Image(
                            bitmap = overallBorderBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .onSizeChanged { decorationLayerSize = it }
                    .drawWithContent {
                        decorationVisualLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(decorationVisualLayer)
                    },
                contentAlignment = Alignment.Center
            ) {
                decorations.forEach { decoration ->
                    key("visual-${decoration.id}") {
                        val currentOffset = resolveDecorationOffset(
                            decorationId = decoration.id,
                            selectedDecorationId = selectedDecorationId,
                            baseOffset = decoration.offset,
                            offsetDiff = offsetDiff
                        )
                        val currentScale = resolveDecorationScale(
                            decorationId = decoration.id,
                            selectedDecorationId = selectedDecorationId,
                            baseScale = decoration.scale,
                            scaleDiff = scaleDiff
                        )
                        val currentRotation = resolveDecorationRotation(
                            decorationId = decoration.id,
                            selectedDecorationId = selectedDecorationId,
                            baseRotation = decoration.rotation,
                            rotationDiff = rotationDiff
                        )
                        val decorationZIndex = resolveDecorationZIndex(
                            decorationId = decoration.id,
                            selectedDecorationId = selectedDecorationId
                        )
                        when (decoration) {
                            is Decoration.Text -> {
                                TextItem(
                                    decoration = decoration,
                                    currentOffset = currentOffset,
                                    currentScale = currentScale,
                                    currentRotation = currentRotation,
                                    zIndex = decorationZIndex,
                                )
                            }

                            is Decoration.Sticker -> {
                                StickerItem(
                                    decoration = decoration,
                                    currentOffset = currentOffset,
                                    currentScale = currentScale,
                                    currentRotation = currentRotation,
                                    zIndex = decorationZIndex,
                                )
                            }

                            is Decoration.Image -> {
                                ImageItem(
                                    decoration = decoration,
                                    currentOffset = currentOffset,
                                    currentScale = currentScale,
                                    currentRotation = currentRotation,
                                    imagePath = images.find { it.id == decoration.imageId }?.path,
                                    zIndex = decorationZIndex,
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.Center
            ) {
                decorations.forEach { decoration ->
                    key("overlay-${decoration.id}") {
                        val isSelected = decoration.id == selectedDecorationId
                        val currentOffset = resolveDecorationOffset(
                            decorationId = decoration.id,
                            selectedDecorationId = selectedDecorationId,
                            baseOffset = decoration.offset,
                            offsetDiff = offsetDiff
                        )
                        val currentScale = resolveDecorationScale(
                            decorationId = decoration.id,
                            selectedDecorationId = selectedDecorationId,
                            baseScale = decoration.scale,
                            scaleDiff = scaleDiff
                        )
                        val currentRotation = resolveDecorationRotation(
                            decorationId = decoration.id,
                            selectedDecorationId = selectedDecorationId,
                            baseRotation = decoration.rotation,
                            rotationDiff = rotationDiff
                        )
                        val decorationZIndex = resolveDecorationZIndex(
                            decorationId = decoration.id,
                            selectedDecorationId = selectedDecorationId
                        )
                        when (decoration) {
                            is Decoration.Text -> {
                                val textMeasurer = rememberTextMeasurer()
                                val layoutResult = textMeasurer.measure(
                                    decoration.text,
                                    TextStyle(
                                        fontFamily = decoration.font.value,
                                        fontWeight = FontWeight(decoration.width),
                                        fontSize = 24.sp.nonScaledSp,
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    )
                                )
                                val maxStroke = decoration.strokeWidth + decoration.secondBorderWidth
                                val decorationSize = Size(
                                    layoutResult.size.width + maxStroke,
                                    layoutResult.size.height + maxStroke
                                )
                                val decorationDpSize = with(density) { decorationSize.toDpSize() }
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
                                    onTransformStart = {
                                        cumulativeOffset = Offset.Zero
                                        onDraggingChanged(true)
                                    },
                                    onTransform = { dragAmount ->
                                        val transformation = calculateTransformations(
                                            cumulativeOffset,
                                            handleOffset - decoration.offset
                                        )
                                        cumulativeOffset += dragAmount.rotateBy(decoration.rotation) * decoration.scale
                                        val targetScale =
                                            (decoration.scale + transformation.scaleDiff).coerceIn(
                                                decoration.scaleRange()
                                            )
                                        scaleDiff = calculateScaleFactor(
                                            baseScale = decoration.scale,
                                            targetScale = targetScale
                                        )
                                        val rotationResult = applyRotationSnap(
                                            decoration.rotation + transformation.rotationDiff
                                        )
                                        if (rotationResult.isSnapped && !wasRotationSnapped) {
                                            hapticManager.perform(FansaHapticType.SEGMENT_TICK)
                                        }
                                        wasRotationSnapped = rotationResult.isSnapped
                                        rotationDiff = rotationResult.snappedRotation - decoration.rotation
                                    },
                                    onTransformEnd = {
                                        commitDecorationTransform(decoration)
                                    },
                                    onTapDelete = { onTapDelete(decoration.id) },
                                    onTapDuplicate = { onTapDuplicate(decoration.id) },
                                    zIndex = decorationZIndex,
                                )
                                if (isSelected) {
                                    SelectionOutline(
                                        offset = currentOffset,
                                        scale = currentScale,
                                        rotation = currentRotation,
                                        decorationSize = decorationDpSize,
                                        borderColor = getSelectionBorderColor(currentRotation),
                                        currentScale = currentScale,
                                        modifier = Modifier
                                            .testTag("TextItemBorder")
                                            .semantics {
                                                this.borderColor =
                                                    getSelectionBorderColor(currentRotation)
                                            },
                                        zIndex = decorationZIndex + 0.1f
                                    )
                                }
                            }

                            is Decoration.Sticker -> {
                                val decorationSize = painterResource(decoration.resId).intrinsicSize
                                val decorationDpSize = with(density) { decorationSize.toDpSize() }
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
                                    onTransformStart = {
                                        cumulativeOffset = Offset.Zero
                                        onDraggingChanged(true)
                                    },
                                    onTransform = { dragAmount ->
                                        val transformation = calculateTransformations(
                                            cumulativeOffset,
                                            handleOffset - decoration.offset
                                        )
                                        cumulativeOffset += dragAmount.rotateBy(decoration.rotation) * decoration.scale
                                        val targetScale =
                                            (decoration.scale + transformation.scaleDiff).coerceIn(
                                                decoration.scaleRange()
                                            )
                                        scaleDiff = calculateScaleFactor(
                                            baseScale = decoration.scale,
                                            targetScale = targetScale
                                        )
                                        val rotationResult = applyRotationSnap(
                                            decoration.rotation + transformation.rotationDiff
                                        )
                                        if (rotationResult.isSnapped && !wasRotationSnapped) {
                                            hapticManager.perform(FansaHapticType.SEGMENT_TICK)
                                        }
                                        wasRotationSnapped = rotationResult.isSnapped
                                        rotationDiff = rotationResult.snappedRotation - decoration.rotation
                                    },
                                    onTransformEnd = {
                                        commitDecorationTransform(decoration)
                                    },
                                    onTapDelete = { onTapDelete(decoration.id) },
                                    onTapDuplicate = { onTapDuplicate(decoration.id) },
                                    zIndex = decorationZIndex,
                                )
                                if (isSelected) {
                                    SelectionOutline(
                                        offset = currentOffset,
                                        scale = currentScale,
                                        rotation = currentRotation,
                                        decorationSize = decorationDpSize,
                                        borderColor = getSelectionBorderColor(currentRotation),
                                        currentScale = currentScale,
                                        zIndex = decorationZIndex + 0.1f
                                    )
                                }
                            }

                            is Decoration.Image -> {
                                val decorationDpSize = DpSize(IMAGE_SIZE_DEFAULT, IMAGE_SIZE_DEFAULT)
                                val decorationSize = with(density) {
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
                                    decorationSize = decorationDpSize,
                                    isSelected = isSelected,
                                    onDecorationTap = { onDecorationTap(decoration.id) },
                                    onTransformStart = {
                                        cumulativeOffset = Offset.Zero
                                        onDraggingChanged(true)
                                    },
                                    onTransform = { dragAmount ->
                                        val transformation = calculateTransformations(
                                            cumulativeOffset,
                                            handleOffset - decoration.offset
                                        )
                                        cumulativeOffset += dragAmount.rotateBy(decoration.rotation) * decoration.scale
                                        val targetScale =
                                            (decoration.scale + transformation.scaleDiff).coerceIn(
                                                decoration.scaleRange()
                                            )
                                        scaleDiff = calculateScaleFactor(
                                            baseScale = decoration.scale,
                                            targetScale = targetScale
                                        )
                                        val rotationResult = applyRotationSnap(
                                            decoration.rotation + transformation.rotationDiff
                                        )
                                        if (rotationResult.isSnapped && !wasRotationSnapped) {
                                            hapticManager.perform(FansaHapticType.SEGMENT_TICK)
                                        }
                                        wasRotationSnapped = rotationResult.isSnapped
                                        rotationDiff = rotationResult.snappedRotation - decoration.rotation
                                    },
                                    onTransformEnd = {
                                        commitDecorationTransform(decoration)
                                    },
                                    onTapDelete = { onTapDelete(decoration.id) },
                                    onTapDuplicate = { onTapDuplicate(decoration.id) },
                                    zIndex = decorationZIndex,
                                )
                                if (isSelected) {
                                    SelectionOutline(
                                        offset = currentOffset,
                                        scale = currentScale,
                                        rotation = currentRotation,
                                        decorationSize = decorationDpSize,
                                        borderColor = getSelectionBorderColor(currentRotation),
                                        currentScale = currentScale,
                                        zIndex = decorationZIndex + 0.1f
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (snappedX || snappedY) {
            val guideLineColor = MaterialTheme.colorScheme.secondary
            val guideLineWidth = with(density) { 1.dp.toPx() }
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (snappedX) {
                    hapticManager.perform(FansaHapticType.SEGMENT_TICK)
                    drawLine(
                        color = guideLineColor,
                        start = Offset(size.width / 2f, 0f),
                        end = Offset(size.width / 2f, size.height),
                        strokeWidth = guideLineWidth
                    )
                }
                if (snappedY) {
                    hapticManager.perform(FansaHapticType.SEGMENT_TICK)
                    drawLine(
                        color = guideLineColor,
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = guideLineWidth
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionOutline(
    offset: Offset,
    scale: Float,
    rotation: Float,
    decorationSize: DpSize,
    borderColor: Color,
    currentScale: Float,
    zIndex: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .zIndex(zIndex)
            .graphicsLayer {
                translationX = offset.x
                translationY = offset.y
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .size(decorationSize)
            .border(1.dp, borderColor)
    ) {
        DecorationHandleIcons(currentScale = currentScale)
    }
}

private fun captureGraphicsLayerBitmap(
    graphicsLayer: GraphicsLayer,
    density: Density,
    layoutDirection: LayoutDirection,
    targetSize: IntSize
): Bitmap {
    val safeWidth = targetSize.width.coerceAtLeast(1)
    val safeHeight = targetSize.height.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
    val canvas = ComposeCanvas(AndroidCanvas(bitmap))
    CanvasDrawScope().draw(
        density = density,
        layoutDirection = layoutDirection,
        canvas = canvas,
        size = Size(safeWidth.toFloat(), safeHeight.toFloat())
    ) {
        drawLayer(graphicsLayer)
    }
    return bitmap
}

private fun createOverallBorderMaskBitmap(
    sourceBitmap: Bitmap,
    overallBorderWidth: Float
): Bitmap? {
    val blurPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        maskFilter = BlurMaskFilter(
            (overallBorderWidth * OVERALL_BORDER_BLUR_RADIUS_MULTIPLIER).coerceAtLeast(1f),
            BlurMaskFilter.Blur.NORMAL
        )
    }
    val offset = IntArray(2)
    val blurredAlphaBitmap = sourceBitmap.extractAlpha(blurPaint, offset) ?: return null
    val maskBitmap = Bitmap.createBitmap(
        sourceBitmap.width,
        sourceBitmap.height,
        Bitmap.Config.ARGB_8888
    )
    val canvas = AndroidCanvas(maskBitmap)
    val maskPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(createOverallBorderMaskColorMatrix())
    }
    canvas.drawBitmap(blurredAlphaBitmap, offset[0].toFloat(), offset[1].toFloat(), maskPaint)
    blurredAlphaBitmap.recycle()
    return maskBitmap
}

private fun createOverallBorderBitmap(
    maskBitmap: Bitmap,
    borderColor: Color
): Bitmap {
    val coloredBitmap = Bitmap.createBitmap(
        maskBitmap.width,
        maskBitmap.height,
        Bitmap.Config.ARGB_8888
    )
    val androidColor = borderColor.toArgb()
    val colorPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(createOverallBorderColorMatrix(androidColor))
    }
    AndroidCanvas(coloredBitmap).drawBitmap(maskBitmap, 0f, 0f, colorPaint)
    return coloredBitmap
}

private fun createOverallBorderMaskColorMatrix(): ColorMatrix = ColorMatrix(
    floatArrayOf(
        0f, 0f, 0f, 0f, 255f,
        0f, 0f, 0f, 0f, 255f,
        0f, 0f, 0f, 0f, 255f,
        0f, 0f, 0f, 255f, 0f
    )
)

private fun createOverallBorderColorMatrix(androidColor: Int): ColorMatrix = ColorMatrix(
    floatArrayOf(
        0f, 0f, 0f, 0f, AndroidColor.red(androidColor).toFloat(),
        0f, 0f, 0f, 0f, AndroidColor.green(androidColor).toFloat(),
        0f, 0f, 0f, 0f, AndroidColor.blue(androidColor).toFloat(),
        0f, 0f, 0f, 1f, 0f
    )
)

@Composable
private fun GestureInputLayer(
    offset: Offset,
    scale: Float,
    rotation: Float,
    decorationSize: DpSize,
    isSelected: Boolean,
    onDecorationTap: () -> Unit,
    onTransformStart: () -> Unit,
    onTransform: (Offset) -> Unit,
    onTransformEnd: () -> Unit,
    onTapDelete: () -> Unit,
    onTapDuplicate: () -> Unit,
    zIndex: Float,
) {
    val currentOnDecorationTap by rememberUpdatedState(onDecorationTap)

    Box(
        modifier = Modifier
            .zIndex(zIndex)
            .graphicsLayer {
                translationX = offset.x
                translationY = offset.y
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .size(decorationSize)
            .pointerInput(Unit) {
                detectNonConsumingTap {
                    currentOnDecorationTap()
                }
            }
    ) {
        if (isSelected) {
            GestureInputHandle(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(
                        (GESTURE_INPUT_HANDLE_SIZE / 2) / scale,
                        (GESTURE_INPUT_HANDLE_SIZE / 2) / scale
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
                    .align(Alignment.TopEnd)
                    .offset(
                        (GESTURE_INPUT_HANDLE_SIZE / 2) / scale,
                        -(GESTURE_INPUT_HANDLE_SIZE / 2) / scale
                    )
            )
            TapInputHandle(
                onTap = onTapDuplicate,
                scale = scale,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        -(GESTURE_INPUT_HANDLE_SIZE / 2) / scale,
                        -(GESTURE_INPUT_HANDLE_SIZE / 2) / scale
                    )
            )
        }
    }
}

@Composable
private fun TextItem(
    decoration: Decoration.Text,
    currentOffset: Offset,
    currentScale: Float,
    currentRotation: Float,
    zIndex: Float,
) {
    Box(
        modifier = Modifier
            .zIndex(zIndex)
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
            textSize = textSize,
            modifier = Modifier
        )
    }
}

@Composable
private fun StickerItem(
    decoration: Decoration.Sticker,
    currentOffset: Offset,
    currentScale: Float,
    currentRotation: Float,
    zIndex: Float,
) {
    Box(
        modifier = Modifier
            .zIndex(zIndex)
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
            modifier = Modifier,
        )
    }
}

@Composable
private fun ImageItem(
    decoration: Decoration.Image,
    currentOffset: Offset,
    currentScale: Float,
    currentRotation: Float,
    imagePath: String?,
    zIndex: Float,
) {
    Box(
        modifier = Modifier
            .zIndex(zIndex)
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
            modifier = Modifier,
        )
    }
}

@Composable
private fun TapInputHandle(
    onTap: () -> Unit,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val currentOnTap by rememberUpdatedState(onTap)

    Box(
        modifier = modifier
            .size(GESTURE_INPUT_HANDLE_SIZE / scale)
            .pointerInput(Unit) {
                detectNonConsumingTap {
                    currentOnTap()
                }
            }
    )
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
                awaitEachGesture {
                    val down = awaitFirstDown()

                    // Composeによるタッチ領域の自動拡張分を無視し、枠線内か判定
                    val isInside = down.position.x in 0f..size.width.toFloat() &&
                            down.position.y in 0f..size.height.toFloat()
                    if (!isInside) return@awaitEachGesture // 範囲外なら無視して次のジェスチャー待ちへ

                    down.consume()
                    onTransformStart()
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null) {
                            val dragAmount = change.position - change.previousPosition
                            if (dragAmount != Offset.Zero) {
                                onTransform(dragAmount)
                            }
                            change.consume()
                        }
                    } while (change?.pressed == true)
                    onTransformEnd()
                }
            }
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
                contentDescription = stringResource(R.string.undo)
            )
        }
        IconButton(
            onClick = onRedoClick,
            enabled = canRedo,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Redo,
                contentDescription = stringResource(R.string.redo)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteEditButton(
    completionTooltipState: TooltipState,
    onTooltipDismissed: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticManager = rememberFansaHapticManager()
    TooltipBox(
        positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        state = completionTooltipState,
        tooltip = {
            RichTooltip(
                title = { Text(text = stringResource(R.string.edit_completion_tooltip_title)) },
                action = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(onClick = onTooltipDismissed) {
                            Text(text = stringResource(R.string.ok))
                        }
                    }
                },
                caretShape = TooltipDefaults.caretShape()
            ) {
                Text(text = stringResource(R.string.edit_completion_tooltip_message))
            }
        },
        modifier = modifier
    ) {
        Button(
            onClick = {
                hapticManager.perform(FansaHapticType.CONFIRM)
                onClick()
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
}

internal val GESTURE_INPUT_HANDLE_SIZE = 24.dp
internal val TEXT_ITEM_PADDING = 8.dp
private val IMAGE_SIZE_DEFAULT = 64.dp
private const val OVERALL_BORDER_BLUR_RADIUS_MULTIPLIER = 3f

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
                currentRotation = 0f,
                zIndex = SELECTED_DECORATION_Z_INDEX,
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
                currentOffset = Offset.Zero,
                currentScale = 1f,
                currentRotation = 0f,
                zIndex = SELECTED_DECORATION_Z_INDEX,
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun CompleteEditButtonPreview() {
    FansaUchiwaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CompleteEditButton(
                completionTooltipState = rememberTooltipState(true),
                onTooltipDismissed = {},
                onClick = {}
            )
        }
    }
}
