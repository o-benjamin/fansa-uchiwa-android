package com.fansauchiwa.edit.pager

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R
import com.fansauchiwa.data.DecorationColors
import com.fansauchiwa.edit.AllTextStyle
import com.fansauchiwa.edit.ColorAndWeightControl
import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.edit.HeaderTitle
import com.fansauchiwa.edit.TestTags
import com.fansauchiwa.ui.composable.ColorPickerRow
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

/**
 * 「全体」タブの「すべての文字」の欄（#308）。うちわの中の文字すべての文字色・枠線の色と太さ・フォントをまとめて変える。
 * 文字ごとに値がちがう項目は、見出しに「（文字ごとにちがう）」を付け、何も選ばれていない表示にする（枠線の太さはスライダーを左端に置く）。
 *
 * フォントはダウンロード式なので、ボタンをグリッドの項目にして、見えている分だけ読み込む（文字タブと同じ）。
 * そのため「全体」タブは [LazyVerticalGrid] で、この欄はその中に項目として並べる
 */
fun LazyGridScope.allTextStyleItems(
    style: AllTextStyle,
    onFontSelected: (FontFamilies) -> Unit,
    onColorSelected: (Color) -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onStrokeWeightChangedFinished: () -> Unit
) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        AllTextStyleControls(
            style = style,
            onColorSelected = onColorSelected,
            onStrokeColorSelected = onStrokeColorSelected,
            onStrokeWeightChanged = onStrokeWeightChanged,
            onStrokeWeightChangedFinished = onStrokeWeightChangedFinished,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
    items(FontFamilies.entries.toList()) { fontFamily ->
        FontFamilyButton(
            fontFamily = fontFamily,
            isSelected = style.font == fontFamily,
            onClick = { onFontSelected(fontFamily) },
            testTag = TestTags.ALL_TEXT_FONT_BUTTON_PREFIX + fontFamily.name
        )
    }
}

/** 欄の見出しから、フォントの一覧の見出しまで。フォントのボタンは [allTextStyleItems] がグリッドに並べる */
@Composable
private fun AllTextStyleControls(
    style: AllTextStyle,
    onColorSelected: (Color) -> Unit,
    onStrokeColorSelected: (Color) -> Unit,
    onStrokeWeightChanged: (Float) -> Unit,
    onStrokeWeightChangedFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TestTags.ALL_TEXT_STYLE_SECTION)
    ) {
        Text(
            text = stringResource(R.string.all_text_style_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        HeaderTitle(
            title = allTextItemTitle(R.string.all_text_color, isMixed = style.color == null),
            modifier = Modifier.padding(top = 16.dp)
        )
        ColorPickerRow(
            currentColor = style.color,
            onColorSelected = onColorSelected,
            modifier = Modifier.padding(top = 8.dp)
        )

        ColorAndWeightControl(
            title = allTextItemTitle(
                R.string.stroke_color_and_weight,
                isMixed = style.strokeColor == null || style.strokeWidth == null
            ),
            color = style.strokeColor,
            width = style.strokeWidth ?: TEXT_STROKE_WIDTH_RANGE.start,
            valueRange = TEXT_STROKE_WIDTH_RANGE,
            steps = TEXT_STROKE_WIDTH_STEPS,
            onColorSelected = onStrokeColorSelected,
            onWeightChanged = onStrokeWeightChanged,
            onWeightChangedFinished = onStrokeWeightChangedFinished
        )

        HeaderTitle(
            title = allTextItemTitle(R.string.all_text_font, isMixed = style.font == null),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

/** 項目の見出し。文字ごとに値がちがう項目には「（文字ごとにちがう）」を付ける */
@Composable
private fun allTextItemTitle(@StringRes titleRes: Int, isMixed: Boolean): String {
    val title = stringResource(titleRes)
    return if (isMixed) stringResource(R.string.all_text_style_mixed_title, title) else title
}

@Composable
private fun AllTextStyleItemsPreviewGrid(style: AllTextStyle) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = FONT_BUTTON_MIN_WIDTH),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        allTextStyleItems(
            style = style,
            onFontSelected = {},
            onColorSelected = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onStrokeWeightChangedFinished = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AllTextStyleItemsSamePreview() {
    FansaUchiwaTheme {
        AllTextStyleItemsPreviewGrid(
            style = AllTextStyle(
                font = FontFamilies.HACHI_MARU_POP,
                color = DecorationColors.PINK.value,
                strokeColor = DecorationColors.WHITE.value,
                strokeWidth = 20f
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AllTextStyleItemsMixedPreview() {
    FansaUchiwaTheme {
        AllTextStyleItemsPreviewGrid(
            style = AllTextStyle(
                font = null,
                color = null,
                strokeColor = null,
                strokeWidth = null
            )
        )
    }
}
