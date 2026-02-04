package com.fansauchiwa.edit.decorationitem

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.fansauchiwa.R
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.edit.TEXT_ITEM_PADDING

@Composable
fun TextItemContent(
    decoration: Decoration.Text,
    textSize: TextUnit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isEditing: Boolean = false,
    onTextChanged: (String) -> Unit = {},
    onFinishEditing: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = decoration.text,
                selection = TextRange(decoration.text.length)
            )
        )
    }
    val measurer = rememberTextMeasurer()
    val textColor = decoration.color
    val strokeColor = decoration.strokeColor
    val secondBorderColor = decoration.secondBorderColor
    val secondBorderWidth = decoration.secondBorderWidth

    BasicTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onTextChanged(it.text)
        },
        textStyle = LocalTextStyle.current.copy(
            fontSize = textSize,
            // カスタムの文字描画を上書きするため、元の描画は透明にする
            color = colorResource(R.color.transparent),
            textAlign = TextAlign.Start,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        readOnly = !isEditing,
        singleLine = true,
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onFinishEditing()
            }
        ),
        modifier = modifier
            .padding(TEXT_ITEM_PADDING)
            .focusRequester(focusRequester)
            .width(IntrinsicSize.Min)
            .drawWithContent {
                // 基本的な文字設定
                val layoutResult = measurer.measure(
                    text = AnnotatedString(decoration.text),
                    style = TextStyle(
                        fontFamily = decoration.font.value,
                        fontWeight = FontWeight(decoration.width),
                        color = textColor,
                        fontSize = textSize
                    )
                )
                // 二重枠線（最背面）: secondBorderColor で描画（太さ：borderWidth + secondBorderWidth）
                if (secondBorderWidth > 0f) {
                    drawText(
                        textLayoutResult = layoutResult,
                        drawStyle = Stroke(
                            width = decoration.strokeWidth + secondBorderWidth,
                            join = StrokeJoin.Round
                        ),
                        color = secondBorderColor,
                        blendMode = if (!isSelected) BlendMode.SrcIn else BlendMode.SrcOver
                    )
                }
                // 枠線（中間）: strokeColor で描画（太さ：borderWidth）
                drawText(
                    textLayoutResult = layoutResult,
                    drawStyle = Stroke(width = decoration.strokeWidth, join = StrokeJoin.Round),
                    color = strokeColor,
                    blendMode = if (!isSelected) BlendMode.SrcIn else BlendMode.SrcOver
                )
                // 塗りつぶし（最前面）: color で本体を描画
                drawText(
                    textLayoutResult = layoutResult,
                    drawStyle = Fill,
                    color = textColor,
                    blendMode = if (!isSelected) BlendMode.SrcIn else BlendMode.SrcOver
                )
                // 最後に描画しないと入力カーソルが埋もれて消えてしまうため、明示的に最後に描画
                drawContent()
            }
    )

    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
            onFinishEditing()
        }
    }
}
