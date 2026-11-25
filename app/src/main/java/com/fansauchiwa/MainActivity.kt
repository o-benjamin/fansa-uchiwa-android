package com.fansauchiwa

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.analytics.EventAnalyticsParams
import com.fansauchiwa.analytics.PushAnalyticsParams
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.CrashReportingRepository
import com.fansauchiwa.data.repository.SettingsRepository
import com.fansauchiwa.ui.notification.EXTRA_REMINDER_DAYS_UNTIL
import com.fansauchiwa.ui.notification.PushNotificationExtras
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.theme.createFontFamilyResolverRecordingFailures
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

    @Inject
    lateinit var crashReportingRepository: CrashReportingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 画面の回転などで作り直されたときに、同じ通知タップを二重に数えない
        if (savedInstanceState == null) {
            logReminderTap()
            handlePushNotificationOpen(intent)
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
            val fontFamilyResolver = remember {
                createFontFamilyResolverRecordingFailures(this@MainActivity, crashReportingRepository)
            }

            FansaUchiwaTheme {
                CompositionLocalProvider(
                    LocalHapticFeedbackEnabled provides isHapticEnabled,
                    LocalFontFamilyResolver provides fontFamilyResolver
                ) {
                    FansaUchiwaNavGraph()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 通知から開くと、ふつうは Activity が作り直されて onCreate で数える。今の Activity にそのまま届いたときも数えるため、ここでも見る
        handlePushNotificationOpen(intent)
    }

    /**
     * お知らせのプッシュ通知（#429）を押して開いたときに、計測を送り、直後のインタースティシャルを出さないようにする。
     * 行き先はいつもホーム（起動したときと同じ）。お題のカードもホームにあるので、お題のお知らせもホームで足りる
     */
    private fun handlePushNotificationOpen(intent: Intent) {
        val extras = intent.extras ?: return
        if (!extras.containsKey(PushNotificationExtras.MESSAGE_ID)) return
        adMobRepository.skipNextInterstitialAd()
        lifecycleScope.launch {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    name = AnalyticsActions.OPEN_FROM_PUSH,
                    params = PushAnalyticsParams.params(extras.getString(PushNotificationExtras.CAMPAIGN))
                )
            )
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
