package com.example.fansauchiwa.preview

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import coil3.size.SizeResolver
import com.example.fansauchiwa.R
import com.example.fansauchiwa.ads.BannerAd
import com.example.fansauchiwa.ui.theme.FansaUchiwaTheme

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
    val context = LocalContext.current

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
        bottomBar = {
            BannerAd(
                LocalContext.current,
                Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        UchiwaPreviewContent(
            imagePath = uiState.imagePath,
            isSaving = uiState.isSaving,
            onSaveClick = {
                val activity = context as? Activity
                if (activity != null) {
                    viewModel.showRewardedAdAndSave(activity)
                }
            },
            onBackToHomeClick = onBackToHome,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
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

@Composable
fun UchiwaPreviewContent(
    imagePath: String?,
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPreview: Boolean = false
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .aspectRatio(1f)
        ) {
            if (isPreview && imagePath != null) {
                // Preview用のサンプルBox
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(id = R.color.gray)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "うちわプレビュー\nサンプル画像",
                        color = colorResource(id = R.color.white)
                    )
                }
            } else {
                imagePath?.let { path ->
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(path)
                            .size(SizeResolver.ORIGINAL)
                            .addLastModifiedToFileCacheKey(true)
                            .build(),
                        contentDescription = stringResource(R.string.uchiwa_preview),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = colorResource(id = R.color.white),
                                shape = RoundedCornerShape(16.dp)
                            )
                    )
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.design_saved),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.print_instructions),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
        }
        Button(
            onClick = onSaveClick,
            enabled = imagePath != null && !isSaving
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = stringResource(R.string.save_as_image),
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        TextButton(
            onClick = onBackToHomeClick,
            enabled = imagePath != null && !isSaving
        ) {
            Text(
                text = stringResource(R.string.back_to_home),
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UchiwaPreviewContentPreview() {
    FansaUchiwaTheme {
        UchiwaPreviewContent(
            imagePath = "/sample/path/uchiwa.png",
            isSaving = false,
            onSaveClick = {},
            onBackToHomeClick = {},
            modifier = Modifier
                .padding(16.dp),
            isPreview = true
        )
    }
}