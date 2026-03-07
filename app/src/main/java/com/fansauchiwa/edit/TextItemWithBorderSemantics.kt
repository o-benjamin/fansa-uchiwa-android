package com.fansauchiwa.edit

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.fansauchiwa.data.Decoration

/**
 * テスト用のComposable: TextItemのボーダー色をセマンティクスに公開する。
 *
 * 回転角度がスナップポイント（0, 90, 180, 270度）のとき `primary`、
 * それ以外のとき `tertiary` のボーダー色を使用する。
 */
@Composable
fun TextItemWithBorderSemantics(
    decoration: Decoration.Text,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isSelected) return

    val borderColor = getSelectionBorderColor(decoration.rotation)

    Box(
        modifier = modifier
            .testTag("TextItemBorder")
            .semantics { this.borderColor = borderColor }
            .border(1.dp, borderColor)
    )
}

/**
 * 回転角度に応じたボーダー色を返す。
 *
 * スナップポイント（0, 90, 180, 270度、および360度の倍数を考慮）のとき `primary`、
 * それ以外のとき `tertiary` を返す。
 *
 * @param rotation 回転角度（度数法）
 * @return ボーダーに使用する色
 */
@Composable
fun getSelectionBorderColor(rotation: Float): Color {
    val normalized = ((rotation % 360f) + 360f) % 360f
    val snapPoints = listOf(0f, 90f, 180f, 270f)
    val isSnapped = snapPoints.any { point ->
        kotlin.math.abs(normalized - point) < 0.01f
    }
    return if (isSnapped) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
}

