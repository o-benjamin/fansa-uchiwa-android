package com.fansauchiwa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fansauchiwa.data.AdMobRepository
import com.fansauchiwa.data.repository.SettingsRepository
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.util.LocalHapticFeedbackEnabled
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var adMobRepository: AdMobRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            val isHapticEnabled by settingsRepository.isHapticFeedbackEnabled
                .collectAsStateWithLifecycle(initialValue = true)

            FansaUchiwaTheme {
                CompositionLocalProvider(
                    LocalHapticFeedbackEnabled provides isHapticEnabled
                ) {
                    FansaUchiwaNavGraph()
                }
            }
        }
    }
}