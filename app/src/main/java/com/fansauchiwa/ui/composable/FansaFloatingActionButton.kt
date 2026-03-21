package com.fansauchiwa.ui.composable

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.util.FansaHapticType
import com.fansauchiwa.ui.util.rememberFansaHapticManager

/**
 * [FloatingActionButton] のラッパー共通コンポーザブル。
 *
 * [hapticFeedbackType] を指定することで、タップ時に指定した種類の触覚フィードバックを
 * [onClick] の前に実行する。Haptic Feedbackは UI層で完結させるため、
 * ViewModelには含めずこのコンポーネントで一括管理する。
 *
 * @param onClick タップ時のコールバック（Haptic後に呼び出される）
 * @param modifier Modifier
 * @param hapticFeedbackType タップ時に実行するHapticの種類。nullの場合はHapticを実行しない
 * @param containerColor FABの背景色
 * @param contentColor FABのコンテンツ色
 * @param content FABの内部コンテンツ
 */
@Composable
fun FansaFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hapticFeedbackType: FansaHapticType? = null,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable () -> Unit
) {
    val hapticManager = rememberFansaHapticManager()
    FloatingActionButton(
        onClick = {
            hapticFeedbackType?.let { hapticManager.perform(it) }
            onClick()
        },
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        content = content
    )
}

@Preview(showBackground = true)
@Composable
private fun FansaFloatingActionButtonPreview() {
    FansaUchiwaTheme {
        FansaFloatingActionButton(
            onClick = {},
            hapticFeedbackType = FansaHapticType.CONFIRM,
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ) {
            Text("削除")
        }
    }
}
