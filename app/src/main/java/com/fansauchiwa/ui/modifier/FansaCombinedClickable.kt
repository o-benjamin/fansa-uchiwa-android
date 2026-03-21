package com.fansauchiwa.ui.modifier

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.fansauchiwa.ui.util.FansaHapticType
import com.fansauchiwa.ui.util.rememberFansaHapticManager

/**
 * 長押し時に [FansaHapticType.LONG_PRESS] の触覚フィードバックを実行する
 * [combinedClickable] のラッパーModifier。
 *
 * Haptic Feedbackは UI層で完結させるため、コールバック内ではなくこのModifierで一括管理する。
 */
@Suppress("UnnecessaryComposedModifier")
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.fansaCombinedClickable(
    onClick: () -> Unit,
    onLongClick: () -> Unit
): Modifier = composed {
    val hapticManager = rememberFansaHapticManager()
    combinedClickable(
        onClick = onClick,
        onLongClick = {
            hapticManager.perform(FansaHapticType.LONG_PRESS)
            onLongClick()
        }
    )
}
