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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fansauchiwa.R
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.theme.SettingsItemHorizontalPadding
import com.fansauchiwa.ui.theme.SettingsItemVerticalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        uiState = uiState,
        onBack = onBack,
        onToggleHapticFeedback = { viewModel.toggleHapticFeedback(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onToggleHapticFeedback: (Boolean) -> Unit
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
        when (uiState) {
            is SettingsUiState.Loading -> HapticFeedbackSettingRow(
                isEnabled = false,
                isLoading = true,
                onToggle = onToggleHapticFeedback,
                modifier = Modifier.padding(innerPadding)
            )
            is SettingsUiState.Success -> HapticFeedbackSettingRow(
                isEnabled = uiState.isHapticFeedbackEnabled,
                isLoading = false,
                onToggle = onToggleHapticFeedback,
                modifier = Modifier.padding(innerPadding)
            )
            is SettingsUiState.Error -> HapticFeedbackSettingRow(
                isEnabled = false,
                isLoading = false,
                onToggle = onToggleHapticFeedback,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun HapticFeedbackSettingRow(
    isEnabled: Boolean,
    isLoading: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = SettingsItemHorizontalPadding,
                vertical = SettingsItemVerticalPadding
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
            enabled = !isLoading
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview_HapticEnabled() {
    FansaUchiwaTheme {
        SettingsContent(
            uiState = SettingsUiState.Success(isHapticFeedbackEnabled = true),
            onBack = {},
            onToggleHapticFeedback = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview_HapticDisabled() {
    FansaUchiwaTheme {
        SettingsContent(
            uiState = SettingsUiState.Success(isHapticFeedbackEnabled = false),
            onBack = {},
            onToggleHapticFeedback = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview_Loading() {
    FansaUchiwaTheme {
        SettingsContent(
            uiState = SettingsUiState.Loading,
            onBack = {},
            onToggleHapticFeedback = {}
        )
    }
}

