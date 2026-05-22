package com.fansauchiwa.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * 触覚フィードバックの有効/無効を Composition ツリー全体で共有するための CompositionLocal。
 * デフォルトは true（有効）。
 * アプリのルート（MainActivity 等）で [SettingsRepository] の値を提供すること。
 */
val LocalHapticFeedbackEnabled = compositionLocalOf { true }

/**
 * [FansaHapticType] を実行するための出力ポート。
 *
 * Presentation層の詳細は実装側に閉じ込め、呼び出し側はアプリ独自の型だけを扱う。
 */
private fun interface FansaHapticPerformer {
    fun perform(type: FansaHapticType)
}

/**
 * アプリ内の触覚フィードバックを一元管理するクラス。
 *
 * 有効/無効の判定とイベントディスパッチのみを担当し、
 * 実際のUIプラットフォーム依存処理は [FansaHapticPerformer] に委譲する。
 *
 * インスタンスの取得には [rememberFansaHapticManager] を使用すること。
 */
class FansaHapticManager(
    private val isEnabled: Boolean,
    private val performer: FansaHapticPerformer
) {
    fun perform(type: FansaHapticType) {
        if (!isEnabled) return
        performer.perform(type)
    }
}

private class ComposeFansaHapticPerformer(
    private val hapticFeedback: HapticFeedback
) : FansaHapticPerformer {
    override fun perform(type: FansaHapticType) {
        hapticFeedback.performHapticFeedback(type.toComposeHapticFeedbackType())
    }
}

private fun FansaHapticType.toComposeHapticFeedbackType(): HapticFeedbackType = when (this) {
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

/**
 * [FansaHapticManager] のインスタンスを生成・保持して返すComposable関数。
 *
 * アプリ内で [LocalHapticFeedback] を直接呼び出すのはこの関数内のみとする。
 * Haptic Feedbackを使用する箇所では必ずこの関数でマネージャーを取得し、
 * [FansaHapticType] を引数に渡して実行すること。
 *
 * 有効/無効は [LocalHapticFeedbackEnabled] から自動的に読み取るため、
 * 呼び出し側で isEnabled を意識する必要はない。
 */
@Composable
fun rememberFansaHapticManager(): FansaHapticManager {
    val hapticFeedback = LocalHapticFeedback.current
    val isEnabled = LocalHapticFeedbackEnabled.current
    return remember(hapticFeedback, isEnabled) {
        FansaHapticManager(
            isEnabled = isEnabled,
            performer = ComposeFansaHapticPerformer(hapticFeedback)
        )
    }
}
