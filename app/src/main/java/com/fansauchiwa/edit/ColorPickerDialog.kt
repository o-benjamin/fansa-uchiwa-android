package com.fansauchiwa.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fansauchiwa.R
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@Composable
fun ColorPickerDialog(
    initialColor: Color = Color.White,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val viewModel = rememberColorPickerDialogViewModel(
        initialColor = initialColor,
        onDismiss = onDismiss,
        onColorSelected = onColorSelected
    )

    ColorPickerDialogContent(
        initialColor = initialColor,
        uiState = viewModel.uiState,
        onColorChanged = viewModel::onColorChanged,
        onDismiss = viewModel::onDismissRequested,
        onConfirm = viewModel::onConfirmRequested
    )
}

@Composable
private fun rememberColorPickerDialogViewModel(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
): ColorPickerDialogViewModel {
    val currentOnDismiss = rememberUpdatedState(onDismiss)
    val currentOnColorSelected = rememberUpdatedState(onColorSelected)

    return remember(initialColor) {
        ColorPickerDialogViewModel(
            initialColor = initialColor,
            onDismiss = { currentOnDismiss.value() },
            onColorSelected = { color -> currentOnColorSelected.value(color) }
        )
    }
}

@Composable
private fun ColorPickerDialogContent(
    initialColor: Color,
    uiState: ColorPickerDialogUiState,
    onColorChanged: (Color) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val controller = rememberColorPickerController()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    controller = controller,
                    onColorChanged = { colorEnvelope ->
                        onColorChanged(colorEnvelope.color)
                    },
                    initialColor = initialColor
                )
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    controller = controller,
                    initialColor = initialColor
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.selectedColorCode,
                    )
                    AlphaTile(
                        modifier = Modifier
                            .height(64.dp)
                            .width(104.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        selectedColor = uiState.selectedColor,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.decide))
                    }
                }
            }
        }
    }
}

@Immutable
private data class ColorPickerDialogUiState(
    val selectedColor: Color,
    val selectedColorCode: String
) {
    companion object {
        fun create(initialColor: Color): ColorPickerDialogUiState {
            return ColorPickerDialogUiState(
                selectedColor = initialColor,
                selectedColorCode = initialColor.toColorCode()
            )
        }
    }
}

private class ColorPickerDialogViewModel(
    initialColor: Color,
    private val onDismiss: () -> Unit,
    private val onColorSelected: (Color) -> Unit
) {
    var uiState by mutableStateOf(ColorPickerDialogUiState.create(initialColor))
        private set

    fun onColorChanged(color: Color) {
        uiState = uiState.copy(
            selectedColor = color,
            selectedColorCode = color.toColorCode()
        )
    }

    fun onDismissRequested() {
        onDismiss()
    }

    fun onConfirmRequested() {
        onColorSelected(uiState.selectedColor)
        onDismiss()
    }
}

private fun Color.toColorCode(): String {
    return String.format(
        "#%06X",
        0xFFFFFF and toArgb()
    )
}

@Preview(showBackground = true)
@Composable
fun ColorPickerDialogPreview() {
    FansaUchiwaTheme {
        ColorPickerDialog(
            initialColor = Color(0xFF5200EE),
            onDismiss = {},
            onColorSelected = {}
        )
    }
}
