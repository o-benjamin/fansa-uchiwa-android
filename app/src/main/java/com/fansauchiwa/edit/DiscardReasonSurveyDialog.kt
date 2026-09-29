package com.fansauchiwa.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.fansauchiwa.R
import com.fansauchiwa.analytics.DiscardReason
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

/**
 * 編集を破棄した理由を選択肢で1問聞くダイアログ（#265・一時的な調査。消し方は DiscardReasonSurvey の KDoc を参照）。
 *
 * 選択肢を1つ押すとすぐに閉じる。「答えない」・ダイアログの外側・戻るキーで閉じたときは [DiscardReason.NO_ANSWER]。
 *
 * @param onAnswer 選んだ理由を受け取る。どの閉じ方でも1回だけ呼ばれる
 */
@Composable
fun DiscardReasonSurveyDialog(onAnswer: (DiscardReason) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAnswer(DiscardReason.NO_ANSWER) },
        title = {
            Text(text = stringResource(R.string.discard_reason_title))
        },
        text = {
            Column {
                DiscardReason.choices.forEach { reason ->
                    TextButton(
                        onClick = { onAnswer(reason) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(reason.labelRes),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAnswer(DiscardReason.NO_ANSWER) }) {
                Text(text = stringResource(DiscardReason.NO_ANSWER.labelRes))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DiscardReasonSurveyDialogPreview() {
    FansaUchiwaTheme {
        DiscardReasonSurveyDialog(onAnswer = {})
    }
}
