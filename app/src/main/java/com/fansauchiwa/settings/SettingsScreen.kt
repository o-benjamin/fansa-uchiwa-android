package com.fansauchiwa.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fansauchiwa.R
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState = remember(uiState) { uiState.toScreenState() }

    SettingsContent(
        screenState = screenState,
        onBack = onBack,
        onAction = viewModel::onAction
    )
}

private fun SettingsUiState.toScreenState(): SettingsScreenState = when (this) {
    SettingsUiState.Loading -> SettingsScreenState(
        isHapticFeedbackEnabled = false,
        isHapticFeedbackSwitchEnabled = false
    )
    is SettingsUiState.Success -> SettingsScreenState(
        isHapticFeedbackEnabled = isHapticFeedbackEnabled,
        isHapticFeedbackSwitchEnabled = true
    )
    is SettingsUiState.Error -> SettingsScreenState(
        isHapticFeedbackEnabled = false,
        isHapticFeedbackSwitchEnabled = true
    )
}

private class SettingsScreenState(
    val isHapticFeedbackEnabled: Boolean,
    val isHapticFeedbackSwitchEnabled: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    screenState: SettingsScreenState,
    onBack: () -> Unit,
    onAction: (SettingsAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title)
                    )
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
        }
    ) { innerPadding ->
        HapticFeedbackSettingRow(
            isEnabled = screenState.isHapticFeedbackEnabled,
            isSwitchEnabled = screenState.isHapticFeedbackSwitchEnabled,
            onToggle = { enabled -> onAction(SettingsAction.ToggleHapticFeedback(enabled)) },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun HapticFeedbackSettingRow(
    isEnabled: Boolean,
    isSwitchEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.settings_haptic_feedback),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            enabled = isSwitchEnabled
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview_HapticEnabled() {
    FansaUchiwaTheme {
        SettingsContent(
            screenState = SettingsScreenState(
                isHapticFeedbackEnabled = true,
                isHapticFeedbackSwitchEnabled = true
            ),
            onBack = {},
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview_HapticDisabled() {
    FansaUchiwaTheme {
        SettingsContent(
            screenState = SettingsScreenState(
                isHapticFeedbackEnabled = false,
                isHapticFeedbackSwitchEnabled = true
            ),
            onBack = {},
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview_Loading() {
    FansaUchiwaTheme {
        SettingsContent(
            screenState = SettingsScreenState(
                isHapticFeedbackEnabled = false,
                isHapticFeedbackSwitchEnabled = false
            ),
            onBack = {},
            onAction = {}
        )
    }
}
