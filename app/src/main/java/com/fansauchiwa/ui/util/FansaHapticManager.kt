package com.fansauchiwa.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * アプリ内の触覚フィードバックを一元管理するクラス。
 *
 * [FansaHapticType] を受け取り、Compose標準の [HapticFeedbackType] にマッピングして実行する。
 * 将来的に独自のVibrator実装へ切り替える場合はこのクラスの内部実装を変更するだけでよい。
 *
 * インスタンスの取得には [rememberFansaHapticManager] を使用すること。
 */
class FansaHapticManager(private val hapticFeedback: HapticFeedback) {

    fun perform(type: FansaHapticType) {
        val hapticFeedbackType = when (type) {
            FansaHapticType.CONFIRM -> HapticFeedbackType.Confirm
            FansaHapticType.CONTEXT_CLICK -> HapticFeedbackType.ContextClick
            FansaHapticType.GESTURE_END -> HapticFeedbackType.GestureEnd
            FansaHapticType.GESTURE_THRESHOLD_ACTIVATE -> HapticFeedbackType.GestureThresholdActivate
            FansaHapticType.KEYBOARD_TAP -> HapticFeedbackType.KeyboardTap
            FansaHapticType.LONG_PRESS -> HapticFeedbackType.LongPress
            FansaHapticType.REJECT -> HapticFeedbackType.Reject
            FansaHapticType.SEGMENT_FREQUENT_TICK -> HapticFeedbackType.SegmentFrequentTick
            FansaHapticType.SEGMENT_TICK -> HapticFeedbackType.SegmentTick
            FansaHapticType.TEXT_HANDLE_MOVE -> HapticFeedbackType.TextHandleMove
            FansaHapticType.TOGGLE_OFF -> HapticFeedbackType.ToggleOff
            FansaHapticType.TOGGLE_ON -> HapticFeedbackType.ToggleOn
            FansaHapticType.VIRTUAL_KEY -> HapticFeedbackType.VirtualKey
        }
        hapticFeedback.performHapticFeedback(hapticFeedbackType)
    }
}

/**
 * [FansaHapticManager] のインスタンスを生成・保持して返すComposable関数。
 *
 * アプリ内で [LocalHapticFeedback] を直接呼び出すのはこの関数内のみとする。
 * Haptic Feedbackを使用する箇所では必ずこの関数でマネージャーを取得し、
 * [FansaHapticType] を引数に渡して実行すること。
 */
@Composable
fun rememberFansaHapticManager(): FansaHapticManager {
    val hapticFeedback = LocalHapticFeedback.current
    return remember(hapticFeedback) { FansaHapticManager(hapticFeedback) }
}
