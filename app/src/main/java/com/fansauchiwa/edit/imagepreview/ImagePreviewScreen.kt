package com.fansauchiwa.edit.imagepreview

import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.fansauchiwa.R
import com.fansauchiwa.ads.BannerAd
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreviewScreen(
    onConfirm: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ImagePreviewViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.logScreenView()
    }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect {
            snackbarHostState.showSnackbar(
                message = "背景の削除に失敗しました"
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.confirmEvent.collect { imageUri ->
            onConfirm(imageUri)
        }
    }

    ImagePreviewScreenContent(
        uiState = uiState,
        onBack = onBack,
        onShowOriginal = viewModel::showOriginal,
        onShowTransparent = viewModel::showTransparent,
        onConfirmTapped = { imageUri, isOriginalSelected ->
            val activity = context as? Activity
            if (activity != null) {
                viewModel.onConfirmTapped(activity, imageUri, isOriginalSelected)
            }
        },
        onStartManualCorrection = viewModel::startManualCorrection,
        onCompleteManualCorrection = viewModel::completeManualCorrection,
        onUndoCorrection = viewModel::undoCorrection,
        onRedoCorrection = viewModel::redoCorrection,
        snackbarHostState = snackbarHostState,
        isPreview = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagePreviewScreenContent(
    uiState: ImagePreviewUiState,
    onBack: () -> Unit,
    onShowOriginal: () -> Unit,
    onShowTransparent: () -> Unit,
    onConfirmTapped: (String, Boolean) -> Unit,
    onStartManualCorrection: () -> Unit,
    onCompleteManualCorrection: () -> Unit,
    onUndoCorrection: () -> Unit,
    onRedoCorrection: () -> Unit,
    snackbarHostState: SnackbarHostState,
    isPreview: Boolean = false
) {
    val isOriginalSelected = when (uiState) {
        is ImagePreviewUiState.Ready.ShowingOriginal -> true
        is ImagePreviewUiState.Ready.ShowingTransparent -> false
        else -> true
    }

    val isManualCorrectionMode =
        uiState is ImagePreviewUiState.Ready.ShowingTransparent.ManualCorrection

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.image_preview_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column {
                ControlArea(
                    uiState = uiState,
                    onConfirmTapped = onConfirmTapped,
                    onShowOriginal = onShowOriginal,
                    onShowTransparent = onShowTransparent,
                    onBack = onBack,
                    onCompleteManualCorrection = onCompleteManualCorrection,
                    onUndoCorrection = onUndoCorrection,
                    onRedoCorrection = onRedoCorrection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                )
                BannerAd(
                    LocalContext.current,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        },
        floatingActionButton = {
            if (!isOriginalSelected && uiState is ImagePreviewUiState.Ready.ShowingTransparent.Success) {
                ExtendedFloatingActionButton(
                    onClick = onStartManualCorrection,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(text = stringResource(R.string.image_preview_manual_edit))
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ImageDisplayArea(
                uiState = uiState,
                isPreview = isPreview,
                modifier = Modifier.fillMaxSize()
            )
            if (uiState is ImagePreviewUiState.Loading || uiState is ImagePreviewUiState.Ready.ShowingTransparent.Loading) {
                LoadingOverlay()
            }
        }

    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ImageDisplayArea(
    uiState: ImagePreviewUiState,
    isPreview: Boolean,
    modifier: Modifier = Modifier
) {
    val scale = remember { mutableFloatStateOf(1f) }
    val offset = remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .background(
                Brush.linearGradient(
                    listOf(
                        colorResource(R.color.gray),
                        colorResource(R.color.gray).copy(alpha = 0.1f)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale.floatValue * zoom).coerceIn(1f, 5f)
                    scale.floatValue = newScale

                    if (scale.floatValue > 1f) {
                        val newOffset = offset.value + pan

                        // 画像がズームされたときの移動可能範囲を計算
                        val maxX = (size.width * (scale.floatValue - 1f)) / 2f
                        val maxY = (size.height * (scale.floatValue - 1f)) / 2f

                        // offsetを範囲内に制限
                        offset.value = Offset(
                            x = newOffset.x.coerceIn(-maxX, maxX),
                            y = newOffset.y.coerceIn(-maxY, maxY)
                        )
                    } else {
                        // scale が 1f の場合は offset をリセット
                        offset.value = Offset.Zero
                    }
                }
            }
    ) {
        val displayUri = when (uiState) {
            is ImagePreviewUiState.Ready.ShowingOriginal -> uiState.originalUri
            is ImagePreviewUiState.Ready.ShowingTransparent.Loading -> uiState.originalUri
            is ImagePreviewUiState.Ready.ShowingTransparent.Success -> uiState.transparentUri
            is ImagePreviewUiState.Ready.ShowingTransparent.ManualCorrection -> uiState.transparentUri
            else -> null
        }

        if (isPreview || displayUri == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.image_preview_load_failed),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale.floatValue,
                        scaleY = scale.floatValue,
                        translationX = offset.value.x,
                        translationY = offset.value.y
                    )
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(displayUri)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    onError = {}
                )
            }
        }
    }
}

@Composable
private fun ControlArea(
    uiState: ImagePreviewUiState,
    onConfirmTapped: (String, Boolean) -> Unit,
    onShowOriginal: () -> Unit,
    onShowTransparent: () -> Unit,
    onBack: () -> Unit,
    onCompleteManualCorrection: () -> Unit,
    onUndoCorrection: () -> Unit,
    onRedoCorrection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (uiState) {
            is ImagePreviewUiState.Ready.ShowingOriginal -> {
                Controls(
                    isOriginalSelected = true,
                    confirmUri = uiState.originalUri,
                    onConfirmTapped = onConfirmTapped,
                    onShowOriginal = onShowOriginal,
                    onShowTransparent = onShowTransparent
                )
            }

            is ImagePreviewUiState.Ready.ShowingTransparent.Loading -> {
                Controls(
                    isOriginalSelected = false,
                    confirmUri = uiState.originalUri,
                    onConfirmTapped = onConfirmTapped,
                    onShowOriginal = onShowOriginal,
                    onShowTransparent = onShowTransparent
                )
            }

            is ImagePreviewUiState.Ready.ShowingTransparent.Success -> {
                Controls(
                    isOriginalSelected = false,
                    confirmUri = uiState.transparentUri,
                    onConfirmTapped = onConfirmTapped,
                    onShowOriginal = onShowOriginal,
                    onShowTransparent = onShowTransparent
                )
            }

            is ImagePreviewUiState.Ready.ShowingTransparent.ManualCorrection -> {
                ManualCorrectionControls(
                    onCompleteManualCorrection = onCompleteManualCorrection,
                    onUndoCorrection = onUndoCorrection,
                    onRedoCorrection = onRedoCorrection
                )
            }

            is ImagePreviewUiState.LoadError -> {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.back))
                }
            }

            ImagePreviewUiState.Loading -> {
                // ローディング中は何も表示しない
            }
        }
    }
}

@Composable
private fun Controls(
    isOriginalSelected: Boolean,
    confirmUri: Uri,
    onConfirmTapped: (String, Boolean) -> Unit,
    onShowOriginal: () -> Unit,
    onShowTransparent: () -> Unit
) {
    ImageTypeSelector(
        isOriginalSelected = isOriginalSelected,
        onShowOriginal = onShowOriginal,
        onShowTransparent = onShowTransparent
    )
    Button(
        onClick = { onConfirmTapped(confirmUri.toString(), isOriginalSelected) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.ok))
    }
}

@Composable
private fun ImageTypeSelector(
    isOriginalSelected: Boolean,
    onShowOriginal: () -> Unit,
    onShowTransparent: () -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        SegmentedButton(
            selected = isOriginalSelected,
            onClick = onShowOriginal,
            shape = SegmentedButtonDefaults.itemShape(
                index = 0,
                count = 2
            )
        ) {
            Text(text = stringResource(R.string.image_preview_original))
        }
        SegmentedButton(
            selected = !isOriginalSelected,
            onClick = onShowTransparent,
            shape = SegmentedButtonDefaults.itemShape(
                index = 1,
                count = 2
            )
        ) {
            Text(text = stringResource(R.string.image_preview_transparent))
        }
    }
}

@Composable
private fun ManualCorrectionControls(
    onCompleteManualCorrection: () -> Unit,
    onUndoCorrection: () -> Unit,
    onRedoCorrection: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onUndoCorrection,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = stringResource(R.string.undo)
                )
            }
            IconButton(
                onClick = onRedoCorrection,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = stringResource(R.string.redo)
                )
            }
        }
        Button(
            onClick = onCompleteManualCorrection,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.done))
        }
    }
}

// Preview関数：すべての UiState を網羅
@Preview(showBackground = true)
@Composable
private fun ImagePreviewScreenLoadingPreview() {
    FansaUchiwaTheme {
        ImagePreviewScreenContent(
            uiState = ImagePreviewUiState.Loading,
            onConfirmTapped = { _, _ -> },
            onBack = {},
            onShowOriginal = {},
            onShowTransparent = {},
            onStartManualCorrection = {},
            onCompleteManualCorrection = {},
            onUndoCorrection = {},
            onRedoCorrection = {},
            snackbarHostState = SnackbarHostState(),
            isPreview = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImagePreviewScreenLoadErrorPreview() {
    FansaUchiwaTheme {
        ImagePreviewScreenContent(
            uiState = ImagePreviewUiState.LoadError(Exception("Failed to load image")),
            onConfirmTapped = { _, _ -> },
            onBack = {},
            onShowOriginal = {},
            onShowTransparent = {},
            onStartManualCorrection = {},
            onCompleteManualCorrection = {},
            onUndoCorrection = {},
            onRedoCorrection = {},
            snackbarHostState = SnackbarHostState(),
            isPreview = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImagePreviewScreenShowingOriginalPreview() {
    FansaUchiwaTheme {
        ImagePreviewScreenContent(
            uiState = ImagePreviewUiState.Ready.ShowingOriginal(
                originalUri = "content://example/image.jpg".toUri()
            ),
            onConfirmTapped = { _, _ -> },
            onBack = {},
            onShowOriginal = {},
            onShowTransparent = {},
            onStartManualCorrection = {},
            onCompleteManualCorrection = {},
            onUndoCorrection = {},
            onRedoCorrection = {},
            snackbarHostState = SnackbarHostState(),
            isPreview = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImagePreviewScreenShowingTransparentLoadingPreview() {
    FansaUchiwaTheme {
        ImagePreviewScreenContent(
            uiState = ImagePreviewUiState.Ready.ShowingTransparent.Loading(
                originalUri = "content://example/image.jpg".toUri()
            ),
            onConfirmTapped = { _, _ -> },
            onBack = {},
            onShowOriginal = {},
            onShowTransparent = {},
            onStartManualCorrection = {},
            onCompleteManualCorrection = {},
            onUndoCorrection = {},
            onRedoCorrection = {},
            snackbarHostState = SnackbarHostState(),
            isPreview = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImagePreviewScreenShowingTransparentSuccessPreview() {
    FansaUchiwaTheme {
        ImagePreviewScreenContent(
            uiState = ImagePreviewUiState.Ready.ShowingTransparent.Success(
                originalUri = "content://example/image.jpg".toUri(),
                transparentUri = "content://example/transparent.jpg".toUri()
            ),
            onConfirmTapped = { _, _ -> },
            onBack = {},
            onShowOriginal = {},
            onShowTransparent = {},
            onStartManualCorrection = {},
            onCompleteManualCorrection = {},
            onUndoCorrection = {},
            onRedoCorrection = {},
            snackbarHostState = SnackbarHostState(),
            isPreview = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImagePreviewScreenManualCorrectionPreview() {
    FansaUchiwaTheme {
        ImagePreviewScreenContent(
            uiState = ImagePreviewUiState.Ready.ShowingTransparent.ManualCorrection(
                originalUri = "content://example/image.jpg".toUri(),
                transparentUri = "content://example/transparent.jpg".toUri()
            ),
            onConfirmTapped = { _, _ -> },
            onBack = {},
            onShowOriginal = {},
            onShowTransparent = {},
            onStartManualCorrection = {},
            onCompleteManualCorrection = {},
            onUndoCorrection = {},
            onRedoCorrection = {},
            snackbarHostState = SnackbarHostState(),
            isPreview = true
        )
    }
}





