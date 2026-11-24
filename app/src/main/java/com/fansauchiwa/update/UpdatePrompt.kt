package com.fansauchiwa.update

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fansauchiwa.R
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

/**
 * 新しい版への更新の案内（#428）。編集中に割り込まないよう、ホームにだけ置く
 *
 * @param snackbarHostState 柔軟な更新のダウンロードが終わったときの「再起動」を出す先（ホームのもの）
 */
@Composable
fun UpdatePrompt(
    snackbarHostState: SnackbarHostState,
    viewModel: UpdatePromptViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val flexibleUpdateLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onFlexibleUpdateResult(result.resultCode)
    }
    val immediateUpdateLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onImmediateUpdateResult(result.resultCode)
    }
    val downloadedMessage = stringResource(R.string.update_downloaded_message)
    val restartLabel = stringResource(R.string.update_downloaded_restart)

    LaunchedEffect(Unit) {
        viewModel.checkForUpdate()
    }

    // 柔軟な更新の Play の画面は、ホームが出ている間にここから出す（決まるまでに編集画面へ移っていたら、戻ったときに出る）
    LaunchedEffect(uiState.isFlexibleUpdatePending) {
        if (uiState.isFlexibleUpdatePending) {
            viewModel.startFlexibleUpdate(flexibleUpdateLauncher)
        }
    }

    // アプリが裏にいる間や編集画面にいる間にダウンロードが終わった分も、ホームに戻ったときに拾う
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.fetchUpdateDownloaded()
    }

    LaunchedEffect(uiState.isUpdateDownloaded) {
        if (!uiState.isUpdateDownloaded) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = downloadedMessage,
            actionLabel = restartLabel,
            withDismissAction = true,
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.completeUpdate()
        }
    }

    uiState.dialog?.let { type ->
        UpdateDialog(
            type = type,
            onUpdate = { viewModel.onUpdateTap(immediateUpdateLauncher) },
            onDismiss = viewModel::dismissDialog
        )
    }
}

/**
 * 更新のダイアログ。[UpdatePromptType.FORCE] なら「あとで」を出さず、外のタップと戻る操作でも閉じない。
 * [UpdatePromptType.FORCE_FALLBACK] は閉じられるが、文言は [UpdatePromptType.FORCE] と同じ
 */
@Composable
fun UpdateDialog(
    type: UpdatePromptType,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    val isForced = type == UpdatePromptType.FORCE
    val messageRes = when (type) {
        UpdatePromptType.FORCE_FALLBACK, UpdatePromptType.FORCE -> R.string.update_dialog_message_critical
        else -> R.string.update_dialog_message
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isForced,
            dismissOnClickOutside = !isForced
        ),
        title = {
            Text(text = stringResource(R.string.update_dialog_title))
        },
        text = {
            Text(text = stringResource(messageRes))
        },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text(text = stringResource(R.string.update_dialog_update))
            }
        },
        dismissButton = if (isForced) {
            null
        } else {
            {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.update_dialog_later))
                }
            }
        }
    )
}

@Preview
@Composable
private fun UpdateDialogPreview() {
    FansaUchiwaTheme {
        UpdateDialog(type = UpdatePromptType.MIN, onUpdate = {}, onDismiss = {})
    }
}

@Preview
@Composable
private fun UpdateDialogForceFallbackPreview() {
    FansaUchiwaTheme {
        UpdateDialog(type = UpdatePromptType.FORCE_FALLBACK, onUpdate = {}, onDismiss = {})
    }
}

@Preview
@Composable
private fun UpdateDialogForcedPreview() {
    FansaUchiwaTheme {
        UpdateDialog(type = UpdatePromptType.FORCE, onUpdate = {}, onDismiss = {})
    }
}
