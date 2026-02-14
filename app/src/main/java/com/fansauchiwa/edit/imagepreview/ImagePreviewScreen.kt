package com.fansauchiwa.edit.imagepreview

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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

    // エラーイベントをSharedFlowで購読してSnackbarを表示
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect {
            snackbarHostState.showSnackbar(
                message = "背景の削除に失敗しました"
            )
        }
    }

    ImagePreviewScreenContent(
        uiState = uiState,
        onConfirm = onConfirm,
        onBack = onBack,
        onShowOriginal = { viewModel.showOriginal() },
        onShowTransparent = { viewModel.showTransparent() },
        snackbarHostState = snackbarHostState,
        isPreview = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagePreviewScreenContent(
    uiState: ImagePreviewUiState,
    onConfirm: (String) -> Unit,
    onBack: () -> Unit,
    onShowOriginal: () -> Unit,
    onShowTransparent: () -> Unit,
    snackbarHostState: SnackbarHostState,
    isPreview: Boolean = false
) {
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                ImageDisplayArea(
                    uiState = uiState,
                    isPreview = isPreview,
                    modifier = Modifier.weight(1f)
                )
                ControlArea(
                    uiState = uiState,
                    onConfirm = onConfirm,
                    onShowOriginal = onShowOriginal,
                    onShowTransparent = onShowTransparent,
                    onBack = onBack,
                    modifier = Modifier.padding(16.dp)
                )
            }
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        colorResource(R.color.gray),
                        colorResource(R.color.gray).copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        val displayUri = when (uiState) {
            is ImagePreviewUiState.Ready.ShowingOriginal -> uiState.originalUri
            is ImagePreviewUiState.Ready.ShowingTransparent.Loading -> uiState.originalUri
            is ImagePreviewUiState.Ready.ShowingTransparent.Success -> uiState.transparentUri
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

@Composable
private fun ControlArea(
    uiState: ImagePreviewUiState,
    onConfirm: (String) -> Unit,
    onShowOriginal: () -> Unit,
    onShowTransparent: () -> Unit,
    onBack: () -> Unit,
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
                    onConfirm = onConfirm,
                    onShowOriginal = onShowOriginal,
                    onShowTransparent = onShowTransparent
                )
            }

            is ImagePreviewUiState.Ready.ShowingTransparent.Loading -> {
                Controls(
                    isOriginalSelected = false,
                    confirmUri = uiState.originalUri,
                    onConfirm = onConfirm,
                    onShowOriginal = onShowOriginal,
                    onShowTransparent = onShowTransparent
                )
            }

            is ImagePreviewUiState.Ready.ShowingTransparent.Success -> {
                Controls(
                    isOriginalSelected = false,
                    confirmUri = uiState.transparentUri,
                    onConfirm = onConfirm,
                    onShowOriginal = onShowOriginal,
                    onShowTransparent = onShowTransparent
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
    onConfirm: (String) -> Unit,
    onShowOriginal: () -> Unit,
    onShowTransparent: () -> Unit
) {
    ImageTypeSelector(
        isOriginalSelected = isOriginalSelected,
        onShowOriginal = onShowOriginal,
        onShowTransparent = onShowTransparent
    )
    Button(
        onClick = { onConfirm(confirmUri.toString()) },
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

// Preview関数：すべての UiState を網羅
@Preview(showBackground = true)
@Composable
private fun ImagePreviewScreenLoadingPreview() {
    FansaUchiwaTheme {
        ImagePreviewScreenContent(
            uiState = ImagePreviewUiState.Loading,
            onConfirm = {},
            onBack = {},
            onShowOriginal = {},
            onShowTransparent = {},
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
            onConfirm = {},
            onBack = {},
            onShowOriginal = {},
            onShowTransparent = {},
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
            onConfirm = {},
            onBack = {},
            onShowOriginal = {},
            onShowTransparent = {},
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
            onConfirm = {},
            onBack = {},
            onShowOriginal = {},
            onShowTransparent = {},
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
            onConfirm = {},
            onBack = {},
            onShowOriginal = {},
            onShowTransparent = {},
            snackbarHostState = SnackbarHostState(),
            isPreview = true
        )
    }
}



