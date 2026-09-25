package com.fansauchiwa.edit.pager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

private const val UnsupportedPuffyControlAlpha = 0.38f

/**
 * ぷくぷくのオン・オフを切り替える、ラベル付きのスイッチの行。
 *
 * [isEnabled] は端末がぷくぷくの描画（AGSL）に対応しているかどうか。対応していないときは
 * スイッチを押せなくして薄く表示し、行を押すと [onUnsupportedClick] を呼ぶ。
 * [isChecked] はスイッチのオン・オフの表示。
 */
@Composable
fun PuffyEffectToggleRow(
    label: String,
    isEnabled: Boolean,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onUnsupportedClick: () -> Unit,
    modifier: Modifier = Modifier,
    switchModifier: Modifier = Modifier
) {
    val puffyModifier = modifier
        .fillMaxWidth()
        .padding(top = 16.dp)
        .alpha(if (isEnabled) 1f else UnsupportedPuffyControlAlpha)
    Row(
        modifier = puffyModifier.run {
            if (isEnabled) this else clickable(onClick = onUnsupportedClick)
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
        )
        Switch(
            checked = isChecked,
            onCheckedChange = if (isEnabled) onCheckedChange else null,
            enabled = isEnabled,
            modifier = switchModifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PuffyEffectToggleRowOnPreview() {
    FansaUchiwaTheme {
        PuffyEffectToggleRow(
            label = stringResource(R.string.puffy_enabled),
            isEnabled = true,
            isChecked = true,
            onCheckedChange = {},
            onUnsupportedClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PuffyEffectToggleRowUnsupportedPreview() {
    FansaUchiwaTheme {
        PuffyEffectToggleRow(
            label = stringResource(R.string.puffy_enabled),
            isEnabled = false,
            isChecked = false,
            onCheckedChange = {},
            onUnsupportedClick = {}
        )
    }
}
