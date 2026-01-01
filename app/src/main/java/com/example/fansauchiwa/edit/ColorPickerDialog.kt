package com.example.fansauchiwa.edit

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.fansauchiwa.R
import com.example.fansauchiwa.ui.theme.FansaUchiwaTheme
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
    val controller = rememberColorPickerController()
    var selectedColor by remember { mutableStateOf(initialColor) }
    var selectedColorCode by remember {
        mutableStateOf(
            String.format(
                "#%06X",
                0xFFFFFF and initialColor.toArgb()
            )
        )
    }


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
                        selectedColor = colorEnvelope.color
                        selectedColorCode = String.format(
                            "#%06X",
                            0xFFFFFF and colorEnvelope.color.toArgb()
                        )

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
                        text = selectedColorCode,
                    )
                    AlphaTile(
                        modifier = Modifier
                            .height(64.dp)
                            .width(104.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        selectedColor = selectedColor,
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
                        onClick = {
                            onColorSelected(selectedColor)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.decide))
                    }
                }
            }
        }
    }
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

