package com.fansauchiwa.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
internal fun TimelineUchiwaThumbnail(
    imagePath: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        UchiwaImage(
            imagePath = imagePath,
            modifier = Modifier.fillMaxSize()
        )
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(32.dp),
            color = Color.Black.copy(alpha = 0.45f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
