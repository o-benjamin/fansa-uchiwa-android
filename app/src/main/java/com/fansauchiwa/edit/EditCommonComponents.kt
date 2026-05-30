package com.fansauchiwa.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R
import com.fansauchiwa.ui.composable.ColorPickerRow
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.util.FansaHapticType
import com.fansauchiwa.ui.util.rememberFansaHapticManager

@Composable
fun HeaderTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun ColorAndWeightControl(
    title: String,
    color: Color,
    width: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier,
    onColorSelected: (Color) -> Unit = {},
    onWeightChanged: (Float) -> Unit = {},
) {
    val isColorPickerOpen = remember { mutableStateOf(false) }
    val hapticManager = rememberFansaHapticManager()

    Column(modifier = modifier.padding(top = 16.dp)) {
        HeaderTitle(title)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(
                    onClick = {
                        isColorPickerOpen.value = false
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandLess,
                        contentDescription = "Color picker toggle"
                    )
                }
                this@Column.AnimatedVisibility(
                    visible = !isColorPickerOpen.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.dp, colorResource(R.color.gray), CircleShape)
                            .background(color = color)
                            .clickable {
                                isColorPickerOpen.value = true
                            }
                    )
                }
            }
            Slider(
                value = width,
                onValueChange = { newValue ->
                    onWeightChanged(newValue)
                    isColorPickerOpen.value = false
                    hapticManager.perform(FansaHapticType.SEGMENT_FREQUENT_TICK)
                },
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedVisibility(isColorPickerOpen.value) {
            ColorPickerRow(
                onColorSelected = onColorSelected,
                modifier = Modifier.padding(top = 8.dp),
                currentColor = color
            )
        }
    }
}

/**
 * ランキングバッジまたは New バッジを表示する共通コンポーザブル。
 * BoxScope 内で使用することを想定。
 *
 * @param rankIndex isNew でないエントリの通し番号（0始まり）。isNew の場合は null。
 * @param isNew 新規アイテムかどうか
 * @param modifier 配置用の Modifier（例: Modifier.align(Alignment.TopStart)）
 * @param topRankCount ランキングバッジを表示する上位件数（デフォルト 5）
 */
@Composable
fun ItemBadge(
    rankIndex: Int?,
    isNew: Boolean,
    modifier: Modifier = Modifier,
    topRankCount: Int = 5
) {
    if (rankIndex != null && rankIndex < topRankCount) {
        val containerColor = when (rankIndex) {
            0 -> colorResource(R.color.rank_1_gold)
            1 -> colorResource(R.color.rank_2_silver)
            2 -> colorResource(R.color.rank_3_bronze)
            else -> colorResource(R.color.rank_4_5_gray)
        }
        val contentColor = colorResource(R.color.white)
        Badge(
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor
        ) {
            Text(text = stringResource(R.string.badge_ranking, rankIndex + 1))
        }
    } else if (isNew) {
        Badge(modifier = modifier) {
            Text(text = stringResource(R.string.badge_new))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderTitlePreview() {
    FansaUchiwaTheme {
        HeaderTitle(title = "サンプルタイトル")
    }
}

@Preview(showBackground = true)
@Composable
fun ColorPickerRowPreview() {
    FansaUchiwaTheme {
        ColorPickerRow(
            onColorSelected = {},
            currentColor = Color(0xFFFF0000)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ColorAndWeightControlPreview() {
    FansaUchiwaTheme {
        ColorAndWeightControl(
            title = "枠線",
            color = Color(0xFFFF0000),
            width = 5f,
            valueRange = 1f..10f,
            steps = 8,
            onColorSelected = {},
            onWeightChanged = {}
        )
    }
}
