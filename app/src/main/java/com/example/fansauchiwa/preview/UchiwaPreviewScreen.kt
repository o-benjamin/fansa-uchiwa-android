package com.example.fansauchiwa.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fansauchiwa.R

@Composable
fun UchiwaPreviewScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            // TODO: うちわのプレビュー内容を表示
        }

        TextButton(
            onClick = { /* TODO: 画像保存処理 */ }
        ) {
            Text(text = stringResource(R.string.save_as_image))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UchiwaPreviewScreenPreview() {
    UchiwaPreviewScreen()
}

