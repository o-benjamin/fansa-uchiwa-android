package com.fansauchiwa.inappmessage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fansauchiwa.data.InAppMessageTarget

/**
 * アプリ内メッセージ（Firebase In-App Messaging。#428）の受け口。画面には何も描かない。
 * ホームにだけ置き、ホームが前にいる間だけメッセージを出す。編集中に割り込まないためと、
 * ボタンの行き先のお題・AI素材をホームのカードと同じ道で開くため
 *
 * アプリを作り直した直後に編集画面から始まったとき（裏に回している間にプロセスが終わったときなど）は、
 * ホームが一度も前に出ていないので止められず、その起動のきっかけのメッセージが編集画面に出ることがある
 *
 * @param onOpenDailyPrompt ボタンの行き先が今日のお題のとき。お題のカードの「このお題で作る」と同じ
 * @param onOpenAiMaterial ボタンの行き先が AI素材のとき。お題のカードの「AIで素材を作る」と同じ
 */
@Composable
fun InAppMessageHost(
    onOpenDailyPrompt: () -> Unit,
    onOpenAiMaterial: () -> Unit,
    viewModel: InAppMessageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnOpenDailyPrompt by rememberUpdatedState(onOpenDailyPrompt)
    val currentOnOpenAiMaterial by rememberUpdatedState(onOpenAiMaterial)

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onHomeResumed()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.onHomePaused()
    }

    LaunchedEffect(uiState.pendingDestination) {
        when (uiState.pendingDestination ?: return@LaunchedEffect) {
            InAppMessageTarget.DAILY_PROMPT -> currentOnOpenDailyPrompt()
            InAppMessageTarget.AI_MATERIAL -> currentOnOpenAiMaterial()
            else -> Unit
        }
        viewModel.onDestinationOpened()
    }
}
