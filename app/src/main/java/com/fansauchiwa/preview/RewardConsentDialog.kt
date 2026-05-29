package com.fansauchiwa.preview

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.fansauchiwa.R

@Composable
internal fun RewardConsentDialog(
    action: RewardConsentAction,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(action.titleResId)) },
        text = { Text(text = stringResource(action.messageResId)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(action.confirmResId))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
