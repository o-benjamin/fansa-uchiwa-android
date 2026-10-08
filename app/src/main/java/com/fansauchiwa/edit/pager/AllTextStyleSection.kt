package com.fansauchiwa.edit.pager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
 * 文字ごとに値がちがう項目は、何も選ばれていない表示にする（枠線の太さはスライダーを左端に置く）
 */
@Composable
fun AllTextStyleSection(
    style: AllTextStyle,
    onFontSelected: (FontFamilies) -> Unit,
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
        if (style.hasMixedValues) {
            Text(
                text = stringResource(R.string.all_text_style_mixed_note),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        HeaderTitle(
            title = stringResource(R.string.all_text_color),
            modifier = Modifier.padding(top = 16.dp)
        )
        ColorPickerRow(
            currentColor = style.color,
            onColorSelected = onColorSelected,
            modifier = Modifier.padding(top = 8.dp)
        )

        ColorAndWeightControl(
            title = stringResource(R.string.stroke_color_and_weight),
            color = style.strokeColor,
            width = style.strokeWidth ?: TEXT_STROKE_WIDTH_RANGE.start,
            valueRange = TEXT_STROKE_WIDTH_RANGE,
            steps = TEXT_STROKE_WIDTH_STEPS,
            onColorSelected = onStrokeColorSelected,
            onWeightChanged = onStrokeWeightChanged,
            onWeightChangedFinished = onStrokeWeightChangedFinished
        )

        HeaderTitle(
            title = stringResource(R.string.all_text_font),
            modifier = Modifier.padding(top = 16.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        ) {
            FontFamilies.entries.forEach { fontFamily ->
                FontFamilyButton(
                    fontFamily = fontFamily,
                    isSelected = style.font == fontFamily,
                    onClick = { onFontSelected(fontFamily) },
                    testTag = TestTags.ALL_TEXT_FONT_BUTTON_PREFIX + fontFamily.name
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllTextStyleSectionSamePreview() {
    FansaUchiwaTheme {
        AllTextStyleSection(
            style = AllTextStyle(
                font = FontFamilies.HACHI_MARU_POP,
                color = DecorationColors.PINK.value,
                strokeColor = DecorationColors.WHITE.value,
                strokeWidth = 20f
            ),
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
fun AllTextStyleSectionMixedPreview() {
    FansaUchiwaTheme {
        AllTextStyleSection(
            style = AllTextStyle(
                font = null,
                color = null,
                strokeColor = null,
                strokeWidth = null
            ),
            onFontSelected = {},
            onColorSelected = {},
            onStrokeColorSelected = {},
            onStrokeWeightChanged = {},
            onStrokeWeightChangedFinished = {}
        )
    }
}
