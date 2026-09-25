package com.fansauchiwa.home.featuredoor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.featuredoor.FeatureDoor
import com.fansauchiwa.analytics.featuredoor.FeatureDoorAnalytics
import com.fansauchiwa.analytics.featuredoor.FeatureDoorAnswer
import com.fansauchiwa.analytics.featuredoor.FeatureDoorOrder
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

/**
 * ホームに置く「毎日開く機能」の入口と、押したときの準備中のダイアログ（#270・一時的）
 *
 * 押された数と答えを [onEvent] で送るだけで、ほかに何も保存しない。
 * 消すときは [FeatureDoorAnalytics] の KDoc を参照。
 *
 * @param doors 表示する入口と並び順（通常は [FeatureDoorOrder.forThisLaunch]）
 * @param onEvent GA4 に送るイベント
 */
@Composable
fun FeatureDoorSection(
    doors: List<FeatureDoor>,
    onEvent: (AnalyticsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var openedDoor by rememberSaveable { mutableStateOf<FeatureDoor?>(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.feature_door_section_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        doors.forEach { door ->
            FeatureDoorCard(
                door = door,
                onClick = {
                    onEvent(FeatureDoorAnalytics.tapEvent(door))
                    openedDoor = door
                }
            )
        }
    }

    openedDoor?.let { door ->
        FeatureDoorDialog(
            door = door,
            onAnswer = { answer ->
                onEvent(FeatureDoorAnalytics.answerEvent(door, answer))
                openedDoor = null
            },
            onDismiss = { openedDoor = null }
        )
    }
}

@Composable
private fun FeatureDoorCard(
    door: FeatureDoor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(door.titleResId),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(door.descriptionResId),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/** 準備中であることをはっきり書き、使いたいかを1問だけ聞く。答えずに閉じたときは何も送らない */
@Composable
private fun FeatureDoorDialog(
    door: FeatureDoor,
    onAnswer: (FeatureDoorAnswer) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.feature_door_dialog_title)) },
        text = {
            Text(
                text = stringResource(
                    R.string.feature_door_dialog_message,
                    stringResource(door.titleResId)
                )
            )
        },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
                FeatureDoorAnswer.entries.forEach { answer ->
                    TextButton(onClick = { onAnswer(answer) }) {
                        Text(text = stringResource(answer.labelResId))
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun FeatureDoorSectionPreview() {
    FansaUchiwaTheme {
        FeatureDoorSection(
            doors = FeatureDoor.entries,
            onEvent = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun FeatureDoorDialogPreview() {
    FansaUchiwaTheme {
        FeatureDoorDialog(
            door = FeatureDoor.COUNTDOWN,
            onAnswer = {},
            onDismiss = {}
        )
    }
}
