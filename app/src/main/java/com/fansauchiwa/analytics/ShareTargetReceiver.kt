package com.fansauchiwa.analytics

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.core.app.PendingIntentCompat
import androidx.core.content.IntentCompat
import com.fansauchiwa.data.repository.CrashReportingRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 共有の画面（chooser）で選ばれたアプリを受け取り、[AnalyticsActions.SELECT_SHARE_TARGET] を送る（#362）
 *
 * `Intent.createChooser` の3つ目の引数に [chooserCallback] を渡すと、アプリが選ばれたときにシステムがこのレシーバーを呼ぶ。
 * アプリを選ばずに閉じたときは呼ばれないので、何も送らない。送るのは選ばれたアプリのパッケージ名だけで、
 * 共有した画像や文は読まない。受け取りや送信に失敗しても、共有そのものは止めない（`.agents/analytics.md`）。
 *
 * 計測をやめるときに消すもの：
 * - このファイル、AndroidManifest.xml の receiver、AnalyticsActions の SELECT_SHARE_TARGET
 * - ShareAnalyticsParams の PARAM_TARGET と selectShareTargetParams、UchiwaPreviewScreen で [chooserCallback] を渡しているところ
 * - テスト：ShareAnalyticsParamsTest の selectShareTargetParams
 * - GA4：カスタムディメンション `target` は編集画面のイベントでも使っているので残す
 */
class ShareTargetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val chosenComponent = IntentCompat.getParcelableExtra(
            intent,
            Intent.EXTRA_CHOSEN_COMPONENT,
            ComponentName::class.java
        )
        val params = ShareAnalyticsParams.selectShareTargetParams(
            chosenPackage = chosenComponent?.packageName,
            entryPoint = intent.getStringExtra(EXTRA_ENTRY_POINT),
            isStartedFromDailyPrompt = intent.getBooleanExtra(EXTRA_STARTED_FROM_DAILY_PROMPT, false)
        ) ?: return

        val dependencies = dependencies(context)
        // logEvent は suspend なので、送り終えるまでレシーバーを生かしておく
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                dependencies.analyticsRepository()
                    .logEvent(AnalyticsEvent(AnalyticsActions.SELECT_SHARE_TARGET, params))
            } catch (e: Exception) {
                dependencies.crashReportingRepository().recordException(e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /** マニフェストから作られるレシーバーにはコンストラクタで渡せないので、Hilt から取り出す */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun analyticsRepository(): AnalyticsRepository
        fun crashReportingRepository(): CrashReportingRepository
    }

    companion object {
        private const val EXTRA_ENTRY_POINT = "com.fansauchiwa.analytics.extra.SHARE_ENTRY_POINT"
        private const val EXTRA_STARTED_FROM_DAILY_PROMPT =
            "com.fansauchiwa.analytics.extra.SHARE_STARTED_FROM_DAILY_PROMPT"

        // 共有の画面は同時に1つしか出ないので、1つの PendingIntent を FLAG_UPDATE_CURRENT で使い回す
        private const val REQUEST_CODE = 0

        private fun dependencies(context: Context): Dependencies =
            EntryPointAccessors.fromApplication(context.applicationContext, Dependencies::class.java)

        /**
         * `Intent.createChooser` の3つ目の引数に渡す [IntentSender]。作れなかったときは null を返す
         * （null を渡しても共有の画面はこれまでどおり出る）
         *
         * @param entryPoint tap_preview_share に付けたのと同じ導線（[ShareAnalyticsParams] の ENTRY_POINT_*）
         * @param isStartedFromDailyPrompt お題から作ったうちわか（#360）
         */
        fun chooserCallback(
            context: Context,
            entryPoint: String,
            isStartedFromDailyPrompt: Boolean
        ): IntentSender? = try {
            val intent = Intent(context, ShareTargetReceiver::class.java)
                .putExtra(EXTRA_ENTRY_POINT, entryPoint)
                .putExtra(EXTRA_STARTED_FROM_DAILY_PROMPT, isStartedFromDailyPrompt)
            // システムが選ばれたアプリ（EXTRA_CHOSEN_COMPONENT）を書き足すので、書き換えられる PendingIntent にする
            // （Android 12 以上は FLAG_MUTABLE の指定が要る。PendingIntentCompat が版ごとに付け分ける）
            PendingIntentCompat.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT,
                true
            )?.intentSender
        } catch (e: Exception) {
            dependencies(context).crashReportingRepository().recordException(e)
            null
        }
    }
}
