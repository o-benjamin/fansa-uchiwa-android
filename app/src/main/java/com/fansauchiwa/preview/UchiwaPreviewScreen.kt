package com.fansauchiwa.preview

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import coil3.size.SizeResolver
import com.fansauchiwa.R
import com.fansauchiwa.ads.BannerAd
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UchiwaPreviewScreen(
    modifier: Modifier = Modifier,
    viewModel: UchiwaPreviewViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.logScreenView()
    }

    // BackHandlerで端末のバックキー押下時にAnalyticsイベントを送信
    BackHandler {
        viewModel.logEvent(AnalyticsActions.TAP_PREVIEW_BACK)
        onBack()
    }

    // 保存失敗時のみSnackbarで通知
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess == false) {
            snackbarHostState.showSnackbar("保存に失敗しました")
            viewModel.clearSaveStatus()
        }
    }

    // 共有シートの発火
    val shareChooserTitle = stringResource(R.string.share_chooser_title)
    LaunchedEffect(uiState.shareImagePath) {
        val shareImagePath = uiState.shareImagePath ?: return@LaunchedEffect
        val sourceFile = File(shareImagePath)
        if (!sourceFile.exists()) {
            viewModel.clearShareImage()
            return@LaunchedEffect
        }
        val sharedDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val destFile = File(sharedDir, sourceFile.name)
        sourceFile.copyTo(destFile, overwrite = true)
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            destFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(shareIntent, shareChooserTitle)
        )
        viewModel.clearShareImage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.logEvent(AnalyticsActions.TAP_PREVIEW_BACK)
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://fansauchiwa-578d22ff.web.app#how-to-print".toUri()
                            )
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = @Suppress("DEPRECATION") Icons.Outlined.HelpOutline,
                            contentDescription = stringResource(R.string.help)
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
            onSaveClick = {
                val activity = context as? Activity
                if (activity != null) {
                    viewModel.showRewardedAdAndSave(activity)
                }
            },
            onShareClick = {
                val activity = context as? Activity
                if (activity != null) {
                    viewModel.showRewardedAdAndShare(activity)
                }
            },
            onBackToHomeClick = {
                viewModel.logEvent(AnalyticsActions.TAP_PREVIEW_GO_HOME)
                onBackToHome()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }

    if (uiState.isLoadingAd && uiState.isSaveButtonPressed) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.black).copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )
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

@Composable
fun UchiwaPreviewContent(
    imagePath: String?,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPreview: Boolean = false
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            16.dp,
            Alignment.CenterVertically
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            if (isPreview && imagePath != null) {
                // Preview用のサンプルBox
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
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
            enabled = imagePath != null
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
        Button(
            onClick = onShareClick,
            enabled = imagePath != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = stringResource(R.string.send_to_print_app),
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        TextButton(
            onClick = onBackToHomeClick,
            enabled = imagePath != null
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
private fun UchiwaPreviewContentLoadingAdPreview() {
    FansaUchiwaTheme {
        UchiwaPreviewContent(
            imagePath = "/sample/path/uchiwa.png",
            onSaveClick = {},
            onShareClick = {},
            onBackToHomeClick = {},
            modifier = Modifier
                .padding(16.dp),
            isPreview = true
        )
    }
}
