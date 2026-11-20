package com.fansauchiwa.edit.aimaterial

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fansauchiwa.R
import com.fansauchiwa.data.AiMaterialImage
import com.fansauchiwa.data.AiMaterialPayment
import com.fansauchiwa.data.AiMaterialRejectReason
import com.fansauchiwa.ui.rememberTransparencyGridBrush
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.util.FansaHapticType
import com.fansauchiwa.ui.util.rememberFansaHapticManager

@Composable
fun AiMaterialScreen(
    onConfirm: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: AiMaterialViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.logScreenView()
    }

    LaunchedEffect(Unit) {
        viewModel.confirmEvent.collect { imageUri ->
            onConfirm(imageUri)
        }
    }

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) viewModel.updateSourceImage(uri)
        }

    AiMaterialScreenContent(
        uiState = uiState,
        onBack = onBack,
        onPromptChange = viewModel::updatePrompt,
        onPickSourceImage = {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onRemoveSourceImage = { viewModel.updateSourceImage(null) },
        onGenerate = viewModel::generate,
        onDiscardResult = viewModel::discardResult,
        onConfirm = viewModel::confirm
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiMaterialScreenContent(
    uiState: AiMaterialUiState,
    onBack: () -> Unit,
    onPromptChange: (String) -> Unit,
    onPickSourceImage: () -> Unit,
    onRemoveSourceImage: () -> Unit,
    onGenerate: () -> Unit,
    onDiscardResult: () -> Unit,
    onConfirm: () -> Unit
) {
    val generation = uiState.generation
    val isGenerating = generation is AiMaterialGeneration.Generating
    val isShowingResult = generation is AiMaterialGeneration.Succeeded
    var showLeaveResultDialog by remember { mutableStateOf(false) }
    val onBackClick = { if (isShowingResult) showLeaveResultDialog = true else onBack() }

    // 生成中に戻ると、回数だけ減って絵が残らない
    BackHandler(enabled = isGenerating) {}
    // 結果を見ている間に戻ると、回数を使った素材を置かずに捨てることになるので確かめる
    BackHandler(enabled = isShowingResult) { showLeaveResultDialog = true }

    if (showLeaveResultDialog) {
        AiMaterialLeaveResultDialog(
            onLeave = {
                showLeaveResultDialog = false
                onBack()
            },
            onDismiss = { showLeaveResultDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.ai_material_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, enabled = !isGenerating) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (generation is AiMaterialGeneration.Succeeded) {
                AiMaterialResult(
                    image = generation.image,
                    onDiscardResult = onDiscardResult,
                    onConfirm = onConfirm
                )
            } else {
                AiMaterialInput(
                    uiState = uiState,
                    isGenerating = isGenerating,
                    onPromptChange = onPromptChange,
                    onPickSourceImage = onPickSourceImage,
                    onRemoveSourceImage = onRemoveSourceImage,
                    onGenerate = onGenerate
                )
            }
        }
    }
}

@Composable
private fun AiMaterialInput(
    uiState: AiMaterialUiState,
    isGenerating: Boolean,
    onPromptChange: (String) -> Unit,
    onPickSourceImage: () -> Unit,
    onRemoveSourceImage: () -> Unit,
    onGenerate: () -> Unit
) {
    val hapticManager = rememberFansaHapticManager()

    OutlinedTextField(
        value = uiState.prompt,
        onValueChange = onPromptChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isGenerating,
        label = { Text(text = stringResource(R.string.ai_material_prompt_label)) },
        placeholder = { Text(text = stringResource(R.string.ai_material_prompt_placeholder)) },
        isError = uiState.isPromptTooLong,
        supportingText = {
            Text(
                text = stringResource(
                    R.string.ai_material_prompt_counter,
                    uiState.promptLength,
                    AI_MATERIAL_MAX_PROMPT_LENGTH
                )
            )
        },
        minLines = 3
    )

    AiMaterialSourceImage(
        sourceImageUri = uiState.sourceImageUri,
        enabled = !isGenerating,
        onPick = onPickSourceImage,
        onRemove = onRemoveSourceImage
    )

    uiState.generation.messageResId()?.let { messageResId ->
        Text(
            text = stringResource(messageResId),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (isGenerating) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Text(text = stringResource(R.string.ai_material_generating))
        }
    }

    Button(
        onClick = {
            hapticManager.perform(FansaHapticType.CONFIRM)
            onGenerate()
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState.canGenerate
    ) {
        Text(text = stringResource(R.string.ai_material_generate))
    }
}

@Composable
private fun AiMaterialSourceImage(
    sourceImageUri: Uri?,
    enabled: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit
) {
    if (sourceImageUri == null) {
        OutlinedButton(
            onClick = onPick,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            Text(text = stringResource(R.string.ai_material_source_image_pick))
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = sourceImageUri,
                contentDescription = stringResource(R.string.ai_material_source_image),
                modifier = Modifier.size(96.dp)
            )
            OutlinedButton(onClick = onRemove, enabled = enabled) {
                Text(text = stringResource(R.string.ai_material_source_image_remove))
            }
        }
    }
    Text(
        text = stringResource(R.string.ai_material_source_image_note),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun AiMaterialResult(
    image: AiMaterialImage,
    onDiscardResult: () -> Unit,
    onConfirm: () -> Unit
) {
    val hapticManager = rememberFansaHapticManager()

    AsyncImage(
        model = image.uri,
        contentDescription = stringResource(R.string.ai_material_result),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(rememberTransparencyGridBrush())
    )

    if (!image.backgroundRemoved) {
        Text(
            text = stringResource(R.string.ai_material_background_not_removed),
            style = MaterialTheme.typography.bodyMedium
        )
    }

    Button(
        onClick = {
            hapticManager.perform(FansaHapticType.CONFIRM)
            onConfirm()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.ai_material_confirm))
    }
    OutlinedButton(onClick = onDiscardResult, modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.ai_material_discard))
    }
}

@Composable
private fun AiMaterialLeaveResultDialog(
    onLeave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.ai_material_leave_result_title)) },
        text = { Text(text = stringResource(R.string.ai_material_leave_result_message)) },
        confirmButton = {
            TextButton(onClick = onLeave) {
                Text(text = stringResource(R.string.ai_material_leave_result_leave))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@StringRes
private fun AiMaterialGeneration.messageResId(): Int? = when (this) {
    is AiMaterialGeneration.Rejected -> when (reason) {
        AiMaterialRejectReason.INVALID_PROMPT -> R.string.ai_material_error_invalid_prompt
        AiMaterialRejectReason.INVALID_IMAGE -> R.string.ai_material_error_invalid_image
        AiMaterialRejectReason.FACE_IN_SOURCE -> R.string.ai_material_error_face_in_source
        AiMaterialRejectReason.BLOCKED_NAME,
        AiMaterialRejectReason.MODERATION,
        AiMaterialRejectReason.SAFETY_BLOCKED -> R.string.ai_material_error_blocked
        AiMaterialRejectReason.NO_CREDITS -> R.string.ai_material_error_no_credits
        AiMaterialRejectReason.FREE_DAILY_LIMIT -> R.string.ai_material_error_free_daily_limit
        AiMaterialRejectReason.TOTAL_DAILY_LIMIT -> R.string.ai_material_error_total_daily_limit
        AiMaterialRejectReason.GENERATION_FAILED -> R.string.ai_material_error_generation_failed
        AiMaterialRejectReason.DISABLED,
        AiMaterialRejectReason.UNAUTHENTICATED -> R.string.ai_material_error_unavailable
    }
    AiMaterialGeneration.Failed -> R.string.ai_material_error_failed
    AiMaterialGeneration.Idle,
    AiMaterialGeneration.Generating,
    is AiMaterialGeneration.Succeeded -> null
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenIdlePreview() {
    AiMaterialScreenPreview(AiMaterialUiState(prompt = "笑顔の星"))
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenGeneratingPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(prompt = "笑顔の星", generation = AiMaterialGeneration.Generating)
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenRejectedPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            generation = AiMaterialGeneration.Rejected(AiMaterialRejectReason.FREE_DAILY_LIMIT)
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenSucceededPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            generation = AiMaterialGeneration.Succeeded(
                AiMaterialImage(
                    uri = Uri.EMPTY,
                    backgroundRemoved = false,
                    payment = AiMaterialPayment.FREE,
                    ticketsRemaining = 0
                )
            )
        )
    )
}

@Composable
private fun AiMaterialScreenPreview(uiState: AiMaterialUiState) {
    FansaUchiwaTheme {
        AiMaterialScreenContent(
            uiState = uiState,
            onBack = {},
            onPromptChange = {},
            onPickSourceImage = {},
            onRemoveSourceImage = {},
            onGenerate = {},
            onDiscardResult = {},
            onConfirm = {}
        )
    }
}
