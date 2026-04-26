package com.fansauchiwa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Dimens(
    val spacing4: Dp,
    val spacing8: Dp,
    val spacing12: Dp,
    val spacing16: Dp,
    val spacing24: Dp,
    val spacing32: Dp,
    val spacing40: Dp,
    val spacing48: Dp,
    val spacing64: Dp
)

internal val DefaultDimens = Dimens(
    spacing4 = 4.dp,
    spacing8 = 8.dp,
    spacing12 = 12.dp,
    spacing16 = 16.dp,
    spacing24 = 24.dp,
    spacing32 = 32.dp,
    spacing40 = 40.dp,
    spacing48 = 48.dp,
    spacing64 = 64.dp
)

internal val LocalDimens = staticCompositionLocalOf { DefaultDimens }

val MaterialTheme.dimens: Dimens
    @Composable
    get() = LocalDimens.current
