package com.fansauchiwa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.analytics.EventAnalyticsParams
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.SettingsRepository
import com.fansauchiwa.ui.notification.EXTRA_REMINDER_DAYS_UNTIL
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.util.LocalHapticFeedbackEnabled
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var adMobRepository: AdMobRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 画面の回転などで作り直されたときに、同じ通知タップを二重に数えない
        if (savedInstanceState == null) {
            logReminderTap()
        }

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
            val isHapticEnabled by settingsRepository.getHapticFeedbackEnabledStream()
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

    private fun logReminderTap() {
        val daysUntil = intent.getIntExtra(EXTRA_REMINDER_DAYS_UNTIL, -1)
            .takeIf { it >= 0 } ?: return
        lifecycleScope.launch {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    name = AnalyticsActions.REMINDER_TAP,
                    params = mapOf(EventAnalyticsParams.DAYS_UNTIL to daysUntil)
                )
            )
        }
    }
}
