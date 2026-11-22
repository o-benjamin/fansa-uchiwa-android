package com.fansauchiwa.settings

import android.content.ClipData
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fansauchiwa.R
import com.fansauchiwa.data.AppInfo
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import kotlinx.coroutines.launch

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
    val snackbarHostState = remember { SnackbarHostState() }
    val copyInstallationId = rememberCopyInstallationId(snackbarHostState)

    if (uiState.showLicenseDialog) {
        AlertDialog(
            onDismissRequest = onDismissLicenseDialog,
            title = {
                Text(text = stringResource(R.string.settings_license_title))
            },
            text = {
                // MIT License の全文が入り、小さい画面では収まらないためスクロールさせる
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = stringResource(R.string.settings_license_message))
                    Text(text = stringResource(R.string.settings_license_phosphor_intro))
                    Text(
                        text = stringResource(R.string.settings_license_phosphor_notice),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
            uiState.appInfo?.let { appInfo ->
                AppInfoSection(
                    appInfo = appInfo,
                    onCopyInstallationId = copyInstallationId
                )
            }
        }
    }
}

/**
 * インストール ID をクリップボードに写す処理を返す。
 * Android 13 以上は OS がコピーしたことを知らせるので、Android 12 以下だけスナックバーを出す。
 * クリップボードへの書き込みとスナックバーは UI の副作用でロジックを持たないので、
 * 触覚フィードバック（`.agents/ui.md`）と同じく ViewModel を通さない
 */
@Composable
private fun rememberCopyInstallationId(snackbarHostState: SnackbarHostState): (String) -> Unit {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val clipLabel = stringResource(R.string.settings_installation_id_clip_label)
    val copiedMessage = stringResource(R.string.settings_installation_id_copied)
    return remember(clipboard, coroutineScope, snackbarHostState, clipLabel, copiedMessage) {
        { installationId: String ->
            coroutineScope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(clipLabel, installationId)))
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    // 続けてタップしても、同じ知らせを順番待ちで何度も出さない
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(copiedMessage)
                }
            }
        }
    }
}

/**
 * 画面のいちばん下に、アプリのバージョンとインストール ID を小さく出す（使い道は [AppInfo]）。
 * ID が取れなかったときは ID の行を出さない
 */
@Composable
internal fun AppInfoSection(
    appInfo: AppInfo,
    onCopyInstallationId: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_app_version, appInfo.versionName),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        appInfo.installationId?.let { installationId ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCopyInstallationId(installationId) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_installation_id),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = installationId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview_LicenseDialog() {
    FansaUchiwaTheme {
        SettingsContent(
            uiState = SettingsUiState.Success(isHapticFeedbackEnabled = true, showLicenseDialog = true),
            onBack = {},
            onToggleHapticFeedback = {},
            onShowLicenseDialog = {},
            onDismissLicenseDialog = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview_AppInfo() {
    FansaUchiwaTheme {
        SettingsContent(
            uiState = SettingsUiState.Success(
                isHapticFeedbackEnabled = true,
                appInfo = AppInfo(versionName = "2.13.0", installationId = "cXyZ0123456789abcdefgh")
            ),
            onBack = {},
            onToggleHapticFeedback = {},
            onShowLicenseDialog = {},
            onDismissLicenseDialog = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview_AppInfoWithoutInstallationId() {
    FansaUchiwaTheme {
        SettingsContent(
            uiState = SettingsUiState.Success(
                isHapticFeedbackEnabled = true,
                appInfo = AppInfo(versionName = "2.13.0", installationId = null)
            ),
            onBack = {},
            onToggleHapticFeedback = {},
            onShowLicenseDialog = {},
            onDismissLicenseDialog = {}
        )
    }
}
