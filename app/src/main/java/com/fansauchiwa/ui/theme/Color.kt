package com.fansauchiwa.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
private data class ThemeRoleColors(
    val color: Color,
    val onColor: Color,
    val containerColor: Color,
    val onContainerColor: Color
)

@Immutable
private data class ThemeSemanticPalette(
    val primary: ThemeRoleColors,
    val secondary: ThemeRoleColors,
    val tertiary: ThemeRoleColors,
    val error: ThemeRoleColors
)

private object FansaThemeColorSource {
    val lightPalette = ThemeSemanticPalette(
        primary = ThemeRoleColors(
            color = Color(0xFFE91E63),
            onColor = Color(0xFFFFFFFF),
            containerColor = Color(0xFFFFD8E4),
            onContainerColor = Color(0xFF3D0017)
        ),
        secondary = ThemeRoleColors(
            color = Color(0xFF74565F),
            onColor = Color(0xFFFFFFFF),
            containerColor = Color(0xFFFFD9E2),
            onContainerColor = Color(0xFF2B151C)
        ),
        tertiary = ThemeRoleColors(
            color = Color(0xFF7C5635),
            onColor = Color(0xFFFFFFFF),
            containerColor = Color(0xFFFFDDBB),
            onContainerColor = Color(0xFF2A1800)
        ),
        error = ThemeRoleColors(
            color = Color(0xFFB3261E),
            onColor = Color(0xFFFFFFFF),
            containerColor = Color(0xFFF9DEDC),
            onContainerColor = Color(0xFF410E0B)
        )
    )
}

private val lightPalette = FansaThemeColorSource.lightPalette

val PassionMagenta = lightPalette.primary.color
val OnPassionMagenta = lightPalette.primary.onColor
val PassionMagentaContainer = lightPalette.primary.containerColor
val OnPassionMagentaContainer = lightPalette.primary.onContainerColor

val SecondaryMagenta = lightPalette.secondary.color
val OnSecondaryMagenta = lightPalette.secondary.onColor
val SecondaryMagentaContainer = lightPalette.secondary.containerColor
val OnSecondaryMagentaContainer = lightPalette.secondary.onContainerColor

val TertiaryGold = lightPalette.tertiary.color
val OnTertiaryGold = lightPalette.tertiary.onColor
val TertiaryGoldContainer = lightPalette.tertiary.containerColor
val OnTertiaryGoldContainer = lightPalette.tertiary.onContainerColor

val ErrorRed = lightPalette.error.color
val OnErrorWhite = lightPalette.error.onColor
val ErrorContainerLight = lightPalette.error.containerColor
val OnErrorContainerLight = lightPalette.error.onContainerColor
