package com.fansauchiwa.inappmessage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

/**
 * アプリ内メッセージ（Firebase In-App Messaging。#428）の受け口。画面には何も描かない。
 * ナビゲーションの外（`FansaUchiwaNavGraph`）に1つだけ置き、Activity の [viewModel] を渡す。
 * ボタンの行き先のお題・AI素材は、[InAppMessageUiState.pendingDestination] をホームが受け取って開く
 *
 * メッセージはホームが今の画面のときだけ出す。アプリを裏に回しただけでは止めない。止めたまま前に戻ると、
 * 出していたメッセージを Firebase In-App Messaging が描き直せず、そのメッセージが残ったまま以後のメッセージも出なくなるため
 *
 * @param isHomeShown 今の画面がホームか。ナビゲーションの準備前（今の画面がまだない）は null で、何も変えない
 */
@Composable
fun InAppMessageHost(
    isHomeShown: Boolean?,
    viewModel: InAppMessageViewModel
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, lifecycleOwner) {
        // Firebase In-App Messaging は Activity が一時停止するたびにアプリのリスナーをすべて外す
        // （onActivityPaused → removeAllListeners）ので、前にいる間だけ受け取り、前に戻るたびに付け直す
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.observeMessages()
        }
    }

    LaunchedEffect(isHomeShown) {
        isHomeShown?.let(viewModel::onHomeShownChanged)
    }
}
