package com.fansauchiwa.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private object FansaThemeColorSchemes {
    val light = lightColorScheme(
        primary = PassionMagenta,
        onPrimary = OnPassionMagenta,
        primaryContainer = PassionMagentaContainer,
        onPrimaryContainer = OnPassionMagentaContainer,
        secondary = SecondaryMagenta,
        onSecondary = OnSecondaryMagenta,
        secondaryContainer = SecondaryMagentaContainer,
        onSecondaryContainer = OnSecondaryMagentaContainer,
        tertiary = TertiaryGold,
        onTertiary = OnTertiaryGold,
        tertiaryContainer = TertiaryGoldContainer,
        onTertiaryContainer = OnTertiaryGoldContainer,
        background = Color(0xFFFFFBFF),
        surface = Color(0xFFFFFBFF),
        error = ErrorRed,
        onError = OnErrorWhite,
        errorContainer = ErrorContainerLight,
        onErrorContainer = OnErrorContainerLight
    )

    val dark = darkColorScheme(
        primary = Color(0xFFFFB1C8),
        onPrimary = Color(0xFF650033),
        primaryContainer = Color(0xFF8E004A),
        onPrimaryContainer = Color(0xFFFFD8E4)
    )
}

private data class ThemeRequest(
    val darkTheme: Boolean,
    val dynamicColor: Boolean
)

private data class ThemeCapabilities(
    val supportsDynamicColor: Boolean
)

private fun interface DynamicColorSchemeProvider {
    fun provide(darkTheme: Boolean): ColorScheme
}

private object ThemeColorSchemeResolver {
    fun resolve(
        request: ThemeRequest,
        capabilities: ThemeCapabilities,
        dynamicColorSchemeProvider: DynamicColorSchemeProvider
    ): ColorScheme = when {
        request.dynamicColor && capabilities.supportsDynamicColor ->
            dynamicColorSchemeProvider.provide(request.darkTheme)
        request.darkTheme -> FansaThemeColorSchemes.dark
        else -> FansaThemeColorSchemes.light
    }
}

@Composable
private fun rememberFansaColorScheme(request: ThemeRequest): ColorScheme {
    val context = LocalContext.current
    val capabilities = remember {
        ThemeCapabilities(
            supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        )
    }

    return remember(context, request, capabilities) {
        ThemeColorSchemeResolver.resolve(
            request = request,
            capabilities = capabilities,
            dynamicColorSchemeProvider = DynamicColorSchemeProvider { isDarkTheme ->
                if (isDarkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
            }
        )
    }
}

@Composable
fun FansaUchiwaTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val request = ThemeRequest(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor
    )
    val colorScheme = rememberFansaColorScheme(request)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
