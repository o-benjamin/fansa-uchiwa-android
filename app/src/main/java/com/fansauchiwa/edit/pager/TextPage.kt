package com.fansauchiwa.edit.pager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fansauchiwa.R
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.edit.ColorAndWeightControl
import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.edit.ItemBadge
import com.fansauchiwa.edit.TestTags
import com.fansauchiwa.edit.fontRankIndexMap
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

/** 文字の枠線（1つめ・2つめの縁）の太さのスライダーの範囲。文字タブと「全体」タブの「すべての文字」で共通 */
internal val TEXT_STROKE_WIDTH_RANGE = 0f..90f
internal const val TEXT_STROKE_WIDTH_STEPS = 17

/** フォントのボタンを並べるグリッドの列の最小幅。文字タブと「全体」タブで共通 */
internal val FONT_BUTTON_MIN_WIDTH = 88.dp

@Composable
fun TextPage(
    onAddText: (FontFamilies) -> Unit,
    onFontChanged: (FontFamilies) -> Unit,
    onColorSelected: (Color) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onTextWeightChangedFinished: () -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onStrokeWeightChangedFinished: () -> Unit,
    onSecondBorderColorSelected: (Color) -> Unit,
    onSecondBorderWeightChanged: (Float) -> Unit,
    onSecondBorderWeightChangedFinished: () -> Unit,
    selectedTextDecoration: Decoration.Text? = null
) {
    FontFamilySelectionGrid(
        onAddText = onAddText,
        onFontChanged = onFontChanged,
        onColorSelected = onColorSelected,
        onTextWeightChanged = onTextWeightChanged,
        onTextWeightChangedFinished = onTextWeightChangedFinished,
        onStrokeColorSelected = onStrokeColorSelected,
        onStrokeWeightChanged = onStrokeWeightChanged,
        onStrokeWeightChangedFinished = onStrokeWeightChangedFinished,
        onSecondBorderColorSelected = onSecondBorderColorSelected,
        onSecondBorderWeightChanged = onSecondBorderWeightChanged,
        onSecondBorderWeightChangedFinished = onSecondBorderWeightChangedFinished,
        selectedTextDecoration = selectedTextDecoration,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun TextDecorationControls(
    onColorSelected: (Color) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onTextWeightChangedFinished: () -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onStrokeWeightChangedFinished: () -> Unit,
    onSecondBorderColorSelected: (Color) -> Unit,
    onSecondBorderWeightChanged: (Float) -> Unit,
    onSecondBorderWeightChangedFinished: () -> Unit,
    textColor: Color,
    textWidth: Int,
    strokeColor: Color,
    strokeWidth: Float,
    secondBorderColor: Color,
    secondBorderWidth: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ColorAndWeightControl(
            title = stringResource(R.string.text_color_and_weight),
            color = textColor,
            width = textWidth.toFloat(),
            valueRange = 100f..900f,
            steps = 15,
            onColorSelected = onColorSelected,
            onWeightChanged = { newValue ->
                onTextWeightChanged(newValue.toInt())
            },
            onWeightChangedFinished = onTextWeightChangedFinished
        )

        ColorAndWeightControl(
            title = stringResource(R.string.stroke_color_and_weight),
            color = strokeColor,
            width = strokeWidth,
            valueRange = TEXT_STROKE_WIDTH_RANGE,
            steps = TEXT_STROKE_WIDTH_STEPS,
            onColorSelected = onStrokeColorSelected,
            onWeightChanged = onStrokeWeightChanged,
            onWeightChangedFinished = onStrokeWeightChangedFinished
        )

        ColorAndWeightControl(
            title = stringResource(R.string.second_stroke_color_and_weight),
            color = secondBorderColor,
            width = secondBorderWidth,
            valueRange = TEXT_STROKE_WIDTH_RANGE,
            steps = TEXT_STROKE_WIDTH_STEPS,
            onColorSelected = onSecondBorderColorSelected,
            onWeightChanged = onSecondBorderWeightChanged,
            onWeightChangedFinished = onSecondBorderWeightChangedFinished
        )
    }
}

@Composable
fun FontFamilySelectionGrid(
    onAddText: (FontFamilies) -> Unit,
    onFontChanged: (FontFamilies) -> Unit,
    onColorSelected: (Color) -> Unit,
    onTextWeightChanged: (Int) -> Unit,
    onTextWeightChangedFinished: () -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onStrokeWeightChangedFinished: () -> Unit,
    onSecondBorderColorSelected: (Color) -> Unit,
    onSecondBorderWeightChanged: (Float) -> Unit,
    onSecondBorderWeightChangedFinished: () -> Unit,
    selectedTextDecoration: Decoration.Text?,
    modifier: Modifier = Modifier
) {
    val spacing = 8.dp

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = FONT_BUTTON_MIN_WIDTH),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(start = 32.dp, end = 32.dp, bottom = 32.dp),
        modifier = modifier.testTag(TestTags.FONT_FAMILY_GRID)
    ) {
        if (selectedTextDecoration != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TextDecorationControls(
                    onColorSelected = onColorSelected,
                    onTextWeightChanged = onTextWeightChanged,
                    onTextWeightChangedFinished = onTextWeightChangedFinished,
                    onStrokeColorSelected = onStrokeColorSelected,
                    onStrokeWeightChanged = onStrokeWeightChanged,
                    onStrokeWeightChangedFinished = onStrokeWeightChangedFinished,
                    onSecondBorderColorSelected = onSecondBorderColorSelected,
                    onSecondBorderWeightChanged = onSecondBorderWeightChanged,
                    onSecondBorderWeightChangedFinished = onSecondBorderWeightChangedFinished,
                    textColor = selectedTextDecoration.color,
                    strokeColor = selectedTextDecoration.strokeColor,
                    textWidth = selectedTextDecoration.width,
                    strokeWidth = selectedTextDecoration.strokeWidth,
                    secondBorderColor = selectedTextDecoration.secondBorderColor,
                    secondBorderWidth = selectedTextDecoration.secondBorderWidth
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(FontFamilies.entries.toList()) { fontFamily ->
            FontFamilyButton(
                fontFamily = fontFamily,
                isSelected = selectedTextDecoration?.font == fontFamily,
                onClick = {
                    if (selectedTextDecoration != null) {
                        onFontChanged(fontFamily)
                    } else {
                        onAddText(fontFamily)
                    }
                },
                testTag = TestTags.FONT_BUTTON_PREFIX + fontFamily.name
            )
        }
    }
}

/**
 * フォントを選ぶボタン。順位・NEW のバッジを左上に重ねる。
 * 文字タブのフォントの一覧と、「全体」タブの「すべての文字」（#308）で共通
 *
 * @param testTag ボタンに付ける testTag。一覧ごとに別の接頭辞にする
 */
@Composable
fun FontFamilyButton(
    fontFamily: FontFamilies,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        FilledTonalButton(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            border = if (isSelected) BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            ) else null,
            modifier = Modifier
                .height(54.dp)
                .testTag(testTag)
                .semantics { selected = isSelected }
        ) {
            val density = LocalDensity.current
            Text(
                text = "あA!",
                fontSize = (20.dp.value / density.fontScale).sp,
                fontFamily = fontFamily.value
            )
        }
        val rankIndex = fontRankIndexMap[fontFamily]
        ItemBadge(
            rankIndex = rankIndex,
            isNew = fontFamily.isNew,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TextPagePreview() {
    FansaUchiwaTheme {
        TextPage(
            onAddText = {},
            onFontChanged = {},
            onColorSelected = {},
            onTextWeightChanged = {},
            onTextWeightChangedFinished = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onStrokeWeightChangedFinished = {},
            onSecondBorderColorSelected = {},
            onSecondBorderWeightChanged = {},
            onSecondBorderWeightChangedFinished = {},
            selectedTextDecoration = Decoration.Text(
                id = "preview-id",
                font = FontFamilies.HACHI_MARU_POP,
                text = "プレビュー",
                color = Color(0xFF000000),
                strokeColor = Color(0xFFFFFFFF),
                width = 700,
                strokeWidth = 2.5f
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 240)
@Composable
fun FontFamilySelectionGridNarrowPreview() {
    FansaUchiwaTheme {
        FontFamilySelectionGrid(
            onAddText = {},
            onFontChanged = {},
            onColorSelected = {},
            onTextWeightChanged = {},
            onTextWeightChangedFinished = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onStrokeWeightChangedFinished = {},
            onSecondBorderColorSelected = {},
            onSecondBorderWeightChanged = {},
            onSecondBorderWeightChangedFinished = {},
            selectedTextDecoration = null
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun FontFamilySelectionGridMediumPreview() {
    FansaUchiwaTheme {
        FontFamilySelectionGrid(
            onAddText = {},
            onFontChanged = {},
            onColorSelected = {},
            onTextWeightChanged = {},
            onTextWeightChangedFinished = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onStrokeWeightChangedFinished = {},
            onSecondBorderColorSelected = {},
            onSecondBorderWeightChanged = {},
            onSecondBorderWeightChangedFinished = {},
            selectedTextDecoration = null
        )
    }
}

@Preview(showBackground = true, widthDp = 480)
@Composable
fun FontFamilySelectionGridWidePreview() {
    FansaUchiwaTheme {
        FontFamilySelectionGrid(
            onAddText = {},
            onFontChanged = {},
            onColorSelected = {},
            onTextWeightChanged = {},
            onTextWeightChangedFinished = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onStrokeWeightChangedFinished = {},
            onSecondBorderColorSelected = {},
            onSecondBorderWeightChanged = {},
            onSecondBorderWeightChangedFinished = {},
            selectedTextDecoration = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FontFamilyButtonSelectedPreview() {
    FansaUchiwaTheme {
        FontFamilyButton(
            fontFamily = FontFamilies.HACHI_MARU_POP,
            isSelected = true,
            onClick = {},
            testTag = ""
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FontFamilyButtonNotSelectedPreview() {
    FansaUchiwaTheme {
        FontFamilyButton(
            fontFamily = FontFamilies.HACHI_MARU_POP,
            isSelected = false,
            onClick = {},
            testTag = ""
        )
    }
}
