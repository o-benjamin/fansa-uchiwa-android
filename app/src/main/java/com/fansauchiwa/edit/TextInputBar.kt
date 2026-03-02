package com.fansauchiwa.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R
import com.fansauchiwa.data.DEFAULT_DECORATION_TEXT
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.theme.TextInputBarCornerRadius
import com.fansauchiwa.ui.theme.TextInputBarDoneButtonPadding
import com.fansauchiwa.ui.theme.TextInputBarHorizontalPadding
import com.fansauchiwa.ui.theme.TextInputBarInnerHorizontalPadding
import com.fansauchiwa.ui.theme.TextInputBarInnerVerticalPadding
import com.fansauchiwa.ui.theme.TextInputBarVerticalPadding

@Composable
fun TextInputBar(
    initialText: String,
    onTextChanged: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length)
            )
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        // デフォルトテキストの場合のみ、全テキストを選択する
        if (textFieldValue.text == DEFAULT_DECORATION_TEXT) {
            textFieldValue = textFieldValue.copy(
                selection = TextRange(0, textFieldValue.text.length)
            )
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.2f to MaterialTheme.colorScheme.secondaryContainer,
                )
            )
            .padding(top = 8.dp)
            .padding(
                horizontal = TextInputBarHorizontalPadding,
                vertical = TextInputBarVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onTextChanged(newValue.text)
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onDone()
                }
            ),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(TextInputBarCornerRadius)
                )
                .padding(
                    horizontal = TextInputBarInnerHorizontalPadding,
                    vertical = TextInputBarInnerVerticalPadding
                )
        )
        Button(
            shape = RoundedCornerShape(TextInputBarCornerRadius),
            onClick = {
                focusManager.clearFocus()
                onDone()
            },
            modifier = Modifier.padding(start = TextInputBarDoneButtonPadding)
        ) {
            Text(text = stringResource(R.string.decide))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TextInputBarPreview() {
    FansaUchiwaTheme {
        TextInputBar(
            initialText = "サンプルテキスト",
            onTextChanged = {},
            onDone = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextInputBarEmptyPreview() {
    FansaUchiwaTheme {
        TextInputBar(
            initialText = "",
            onTextChanged = {},
            onDone = {}
        )
    }
}

