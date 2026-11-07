package com.fansauchiwa.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R
import com.fansauchiwa.data.DailyPrompt
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

/**
 * ホームのいちばん上に出す「今日のお題」のカード（#360）
 *
 * 「このお題で作る」で、白紙のうちわにお題の文字だけを置いた編集画面を開く。×で閉じると、その日はもう出ない。
 * カードのすぐ横・上下には広告を置かない（#358）。AI素材の入口（#340）は、あとでこのカードの副ボタンとして置く。
 */
@Composable
internal fun DailyPromptCard(
    prompt: DailyPrompt,
    onShown: () -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHelpShown by rememberSaveable { mutableStateOf(false) }

    // カードが出るたびに呼ぶ。同じお題を2回送らない判定は DailyPromptViewTracker が持つ
    LaunchedEffect(prompt.id) {
        onShown()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        // ×の IconButton（タッチ領域 48dp）を右端に寄せるため end は 4dp にし、お題の文字は 12dp 足して見た目の余白を 16dp にする
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.daily_prompt_card_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { isHelpShown = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = stringResource(R.string.daily_prompt_help_description)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.daily_prompt_close_description)
                    )
                }
            }
            Text(
                text = prompt.text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp)
            )
            Button(
                onClick = onCreate,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(text = stringResource(R.string.daily_prompt_create_button))
            }
        }
    }

    if (isHelpShown) {
        AlertDialog(
            onDismissRequest = { isHelpShown = false },
            text = {
                Text(text = stringResource(R.string.daily_prompt_help_message))
            },
            confirmButton = {
                TextButton(onClick = { isHelpShown = false }) {
                    Text(text = stringResource(R.string.daily_prompt_help_close))
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "今日のお題のカード")
@Composable
private fun DailyPromptCardPreview() {
    FansaUchiwaTheme {
        DailyPromptCard(
            prompt = DailyPrompt(id = "default_peace", text = "ピースして"),
            onShown = {},
            onCreate = {},
            onDismiss = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "今日のお題のカード（16文字）")
@Composable
private fun DailyPromptCardLongTextPreview() {
    FansaUchiwaTheme {
        DailyPromptCard(
            prompt = DailyPrompt(id = "preview_long", text = "ハロウィンの仮装を見せてください"),
            onShown = {},
            onCreate = {},
            onDismiss = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
