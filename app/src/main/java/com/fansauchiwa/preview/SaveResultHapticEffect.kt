package com.fansauchiwa.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.withResumed
import com.fansauchiwa.ui.util.FansaHapticType
import com.fansauchiwa.ui.util.rememberFansaHapticManager

/**
 * 保存の結果を振動で1回だけ伝える（#454。決まりは `.agents/ui.md` の「振動の決まり」）。
 * 成功（保存完了のダイアログ）で [FansaHapticType.CONFIRM]、失敗（Snackbar）で [FansaHapticType.REJECT]。
 *
 * - 報酬獲得時の保存は広告の表示中に終わるため、広告の上では鳴らさず、画面が前面（RESUMED）に戻ってから鳴らす
 * - 鳴らしたかは画面が作り直されても残るよう保存し、ダークモードの切り替えなどで作り直されたときに同じ結果でもう一度鳴らさない
 * - 結果を閉じた（[saveSuccess] が null に戻った）ら、次の保存で鳴らせるように戻す。前面に戻る前に null に戻すと鳴らない
 *
 * @param saveSuccess 保存の結果（[UchiwaPreviewUiState.saveSuccess]）。true は成功、false は失敗、null は結果を出していない
 */
@Composable
fun SaveResultHapticEffect(saveSuccess: Boolean?) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val hapticManager = rememberFansaHapticManager()
    var hasPerformed by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(saveSuccess) {
        if (saveSuccess == null) {
            // 結果を閉じたら、次の保存で鳴らせるように戻す
            hasPerformed = false
            return@LaunchedEffect
        }
        if (hasPerformed) return@LaunchedEffect
        val hapticType = if (saveSuccess) FansaHapticType.CONFIRM else FansaHapticType.REJECT
        lifecycleOwner.lifecycle.withResumed {
            hapticManager.perform(hapticType)
            hasPerformed = true
        }
    }
}
