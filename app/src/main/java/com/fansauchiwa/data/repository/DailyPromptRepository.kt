package com.fansauchiwa.data.repository

import android.util.Log
import com.fansauchiwa.data.DailyPrompt
import com.fansauchiwa.data.DailyPromptSelector
import com.fansauchiwa.data.DailyPromptsConfig
import com.fansauchiwa.data.DailyPromptsParser
import com.fansauchiwa.data.TodayProvider
import com.fansauchiwa.data.infra.RemoteConfigDataSource
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 今日のファンサお題（#358）
 *
 * お題は Remote Config で配り、今日の分がない・取得できないときは同梱の控えから選ぶ。null ならお題を出さない（Remote Config で止めている）
 */
interface DailyPromptRepository {
    fun getDailyPromptStream(): Flow<DailyPrompt?>

    /**
     * Remote Config を読み直し、端末の今日の日付でお題を決め直す。
     * 端末に残っている値で先に1回流し、取得できたらもう1回流す。
     * 例外は外に出さず、Remote Config が読めなければ控えから選ぶ（ホームの表示を止めないため）
     */
    suspend fun fetchDailyPrompt()
}

class DailyPromptRepositoryImpl @Inject constructor(
    private val remoteConfigDataSource: RemoteConfigDataSource,
    private val crashReportingRepository: CrashReportingRepository,
    private val todayProvider: TodayProvider
) : DailyPromptRepository {

    private val dailyPrompt = MutableSharedFlow<DailyPrompt?>(replay = 1)

    // ホームを開くたびに読むので、同じ誤りを全員が毎回送らないよう1プロセスに1回だけ記録する
    private var hasRecordedError = false

    override fun getDailyPromptStream(): Flow<DailyPrompt?> = dailyPrompt.asSharedFlow()

    override suspend fun fetchDailyPrompt() {
        var hasEmitted = false
        try {
            remoteConfigDataSource.getDailyPromptsJsonStream().collect { json ->
                emitPromptFor(readConfig(json))
                hasEmitted = true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 取得の処理そのものの失敗（オフラインなどは DataSource の中で握るので、ふつうは来ない）
            Log.w(TAG, "daily_prompts の取得に失敗", e)
            recordOnce(e)
            // 端末に残っていた値ですでに流していれば、それを残す
            if (!hasEmitted) emitPromptFor(DailyPromptsConfig.Unset)
        }
    }

    private suspend fun emitPromptFor(config: DailyPromptsConfig) {
        dailyPrompt.emit(DailyPromptSelector.select(config, todayProvider.today()))
    }

    /** 設定の誤りは控えを使う（[DailyPromptsConfig.Unset]）。一部の項目だけ捨てたときは、ログにだけ残す */
    private fun readConfig(json: String): DailyPromptsConfig =
        try {
            DailyPromptsParser.parse(json).also(::logDroppedItems)
        } catch (e: IllegalArgumentException) {
            // Remote Config に置いた JSON の誤り。全員の端末で起きるので、Crashlytics で気づけるようにする
            Log.w(TAG, "daily_prompts を読めない", e)
            recordOnce(e)
            DailyPromptsConfig.Unset
        }

    private fun logDroppedItems(config: DailyPromptsConfig) {
        if (config !is DailyPromptsConfig.Scheduled || config.droppedPositions.isEmpty()) return
        // その日だけ黙って控えが出る原因になる。Crashlytics には送らない（使える項目は届いているため）
        Log.w(TAG, "daily_prompts の prompts の ${config.droppedPositions.joinToString("・")} 件目を捨てた（date・id・text を確かめる）")
    }

    private fun recordOnce(e: Exception) {
        if (hasRecordedError) return
        hasRecordedError = true
        crashReportingRepository.recordException(e)
    }

    private companion object {
        const val TAG = "DailyPrompt"
    }
}
