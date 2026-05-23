package com.fansauchiwa.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fansauchiwa.R
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

private const val APACHE_LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0"

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
        onToggleHapticFeedback = { viewModel.toggleHapticFeedback(it) },
        onShowLicenseDialog = { viewModel.showLicenseDialog() },
        onDismissLicenseDialog = { viewModel.dismissLicenseDialog() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onToggleHapticFeedback: (Boolean) -> Unit,
    onShowLicenseDialog: () -> Unit,
    onDismissLicenseDialog: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val isHapticFeedbackEnabled = (uiState as? SettingsUiState.Success)?.isHapticFeedbackEnabled ?: false
    val isLoading = uiState is SettingsUiState.Loading

    if (uiState.showLicenseDialog) {
        AlertDialog(
            onDismissRequest = onDismissLicenseDialog,
            title = {
                Text(text = stringResource(R.string.settings_license_title))
            },
            text = {
                Text(text = stringResource(R.string.settings_license_message))
            },
            confirmButton = {
                TextButton(onClick = { uriHandler.openUri(APACHE_LICENSE_URL) }) {
                    Text(text = stringResource(R.string.settings_license_open_in_browser))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissLicenseDialog) {
                    Text(text = stringResource(R.string.settings_license_close))
                }
            }
        )
    }

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
        Column(modifier = Modifier.padding(innerPadding)) {
            HapticFeedbackSettingRow(
                isEnabled = isHapticFeedbackEnabled,
                isLoading = isLoading,
                onToggle = onToggleHapticFeedback
            )
            ListItem(
                headlineContent = {
                    Text(text = stringResource(R.string.settings_license_title))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onShowLicenseDialog)
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
            onToggleHapticFeedback = {},
            onShowLicenseDialog = {},
            onDismissLicenseDialog = {}
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
            onToggleHapticFeedback = {},
            onShowLicenseDialog = {},
            onDismissLicenseDialog = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview_Loading() {
    FansaUchiwaTheme {
        SettingsContent(
            uiState = SettingsUiState.Loading(),
            onBack = {},
            onToggleHapticFeedback = {},
            onShowLicenseDialog = {},
            onDismissLicenseDialog = {}
        )
    }
}
