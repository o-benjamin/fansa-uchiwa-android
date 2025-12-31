package com.example.fansauchiwa.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import coil3.size.SizeResolver
import com.example.fansauchiwa.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UchiwaPreviewScreen(
    viewModel: UchiwaPreviewViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 保存失敗時のみSnackbarで通知
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess == false) {
            snackbarHostState.showSnackbar("保存に失敗しました")
            viewModel.clearSaveStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                uiState.imagePath?.let { path ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(path)
                            .size(SizeResolver.ORIGINAL)
                            .addLastModifiedToFileCacheKey(true)
                            .build(),
                        contentDescription = stringResource(R.string.uchiwa_preview),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            TextButton(
                onClick = { viewModel.saveToGallery() },
                enabled = uiState.imagePath != null && !uiState.isSaving
            ) {
                Text(text = stringResource(R.string.save_as_image))
            }
            TextButton(
                onClick = onBackToHome,
                enabled = uiState.imagePath != null && !uiState.isSaving
            ) {
                Text(text = stringResource(R.string.back_to_home))
            }
        }
    }

    if (uiState.saveSuccess == true) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSaveStatus() },
            title = {
                Text(text = stringResource(R.string.save_success_title))
            },
            text = {
                Text(text = stringResource(R.string.save_success_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearSaveStatus()
                        onBackToHome()
                    }
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UchiwaPreviewScreenPreview() {
    UchiwaPreviewScreen()
}
