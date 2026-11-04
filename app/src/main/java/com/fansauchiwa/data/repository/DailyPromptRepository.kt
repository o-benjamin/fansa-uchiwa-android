package com.fansauchiwa.data.repository

import android.util.Log
import com.fansauchiwa.data.DailyPrompt
import com.fansauchiwa.data.DailyPromptSelector
import com.fansauchiwa.data.DailyPromptsParser
import com.fansauchiwa.data.TodayProvider
import com.fansauchiwa.data.infra.RemoteConfigDataSource
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first

/**
 * 今日のファンサお題（#358）
 *
 * お題は Remote Config で配り、今日の分がない・取得できないときは同梱の控えから選ぶ。null ならお題を出さない（Remote Config で止めている）
 */
interface DailyPromptRepository {
    fun getDailyPromptStream(): Flow<DailyPrompt?>

    /**
     * Remote Config を読み直し、端末の今日の日付でお題を決め直す。
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
    private var hasRecordedParseError = false

    override fun getDailyPromptStream(): Flow<DailyPrompt?> = dailyPrompt.asSharedFlow()

    override suspend fun fetchDailyPrompt() {
        val scheduled = readScheduledPrompts()
        dailyPrompt.emit(DailyPromptSelector.select(scheduled, todayProvider.today()))
    }

    /** @return Remote Config の日付ごとのお題。未設定・読めないときは null（控えを使う） */
    private suspend fun readScheduledPrompts(): Map<LocalDate, DailyPrompt>? =
        try {
            DailyPromptsParser.parse(remoteConfigDataSource.getDailyPromptsJsonStream().first())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Remote Config に置いた JSON の誤り。全員の端末で起きるので、Crashlytics で気づけるようにする
            Log.w(TAG, "daily_prompts を読めない", e)
            if (!hasRecordedParseError) {
                hasRecordedParseError = true
                crashReportingRepository.recordException(e)
            }
            null
        }

    private companion object {
        const val TAG = "DailyPrompt"
    }
}
