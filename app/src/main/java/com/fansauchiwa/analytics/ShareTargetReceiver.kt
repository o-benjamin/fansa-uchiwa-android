package com.fansauchiwa.analytics

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.core.app.PendingIntentCompat
import androidx.core.content.IntentCompat
import dagger.hilt.android.EntryPointAccessors
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
 * 使い道は #358 の判定（お題から X に共有した数）と共有先の内訳。続けるかは #358 の判定のときに決める。
 *
 * 計測をやめるときに消すもの：
 * - このファイル、ShareTargetReceiverEntryPoint.kt、AndroidManifest.xml の receiver、AnalyticsActions の SELECT_SHARE_TARGET
 * - ShareAnalyticsParams の PARAM_TARGET と selectShareTargetParams（KDoc の見出しの SELECT_SHARE_TARGET も）、
 *   UchiwaPreviewScreen で [chooserCallback] を渡しているところと pendingShareEntryPoint のコメントの #362
 * - DailyPromptAnalyticsParams の「計測をやめるときに消すもの」の selectShareTargetParams と ShareAnalyticsParamsTest の行
 * - テスト：ShareAnalyticsParamsTest（ファイルごと）、androidTest の ShareTargetReceiverTest（ファイルごと）
 * - GA4：カスタムディメンション `target` は編集画面のイベントで、`entry_point` は tap_preview_share で使っているので残す
 */
class ShareTargetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val params = paramsFrom(intent)
        val appContext = context.applicationContext
        // logEvent は suspend なので、送り終えるまでレシーバーを生かしておく
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                entryPoint(appContext).analyticsRepository()
                    .logEvent(AnalyticsEvent(AnalyticsActions.SELECT_SHARE_TARGET, params))
            } catch (e: Exception) {
                recordException(appContext, e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val EXTRA_ENTRY_POINT = "com.fansauchiwa.analytics.extra.SHARE_ENTRY_POINT"
        private const val EXTRA_STARTED_FROM_DAILY_PROMPT =
            "com.fansauchiwa.analytics.extra.SHARE_STARTED_FROM_DAILY_PROMPT"

        // 共有の画面は同時に1つしか出ないので、1つの PendingIntent を FLAG_UPDATE_CURRENT で使い回す
        private const val REQUEST_CODE = 0

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
            // システムが選ばれたアプリ（EXTRA_CHOSEN_COMPONENT）を書き足すので、書き換えられる PendingIntent にする
            // （Android 12 以上は FLAG_MUTABLE の指定が要る。PendingIntentCompat が版ごとに付け分ける）。
            // 宛先をクラスで指定した明示的な Intent なので、Android 14 以上の「書き換えられる暗黙の PendingIntent」の禁止には
            // 当たらない（action で呼ぶ形に変えない）
            PendingIntentCompat.getBroadcast(
                context,
                REQUEST_CODE,
                callbackIntent(context, entryPoint, isStartedFromDailyPrompt),
                PendingIntent.FLAG_UPDATE_CURRENT,
                /* isMutable = */ true
            )?.intentSender
        } catch (e: Exception) {
            recordException(context, e)
            null
        }

        /** [chooserCallback] の PendingIntent が包む Intent。導線とお題かどうかを extras で載せる */
        internal fun callbackIntent(
            context: Context,
            entryPoint: String,
            isStartedFromDailyPrompt: Boolean
        ): Intent = Intent(context, ShareTargetReceiver::class.java)
            .putExtra(EXTRA_ENTRY_POINT, entryPoint)
            .putExtra(EXTRA_STARTED_FROM_DAILY_PROMPT, isStartedFromDailyPrompt)

        /** 届いた Intent（[callbackIntent] にシステムが選ばれたアプリを書き足したもの）から送るパラメータを作る */
        internal fun paramsFrom(intent: Intent): Map<String, Any> {
            val chosenComponent = IntentCompat.getParcelableExtra(
                intent,
                Intent.EXTRA_CHOSEN_COMPONENT,
                ComponentName::class.java
            )
            return ShareAnalyticsParams.selectShareTargetParams(
                chosenPackage = chosenComponent?.packageName,
                entryPoint = intent.getStringExtra(EXTRA_ENTRY_POINT),
                isStartedFromDailyPrompt = intent.getBooleanExtra(EXTRA_STARTED_FROM_DAILY_PROMPT, false)
            )
        }

        private fun entryPoint(context: Context): ShareTargetReceiverEntryPoint =
            EntryPointAccessors.fromApplication(context.applicationContext, ShareTargetReceiverEntryPoint::class.java)

        // 記録そのもの（Hilt から取り出すところを含む）が失敗しても、共有とアプリは止めない
        private fun recordException(context: Context, e: Exception) {
            runCatching { entryPoint(context).crashReportingRepository().recordException(e) }
        }
    }
}
