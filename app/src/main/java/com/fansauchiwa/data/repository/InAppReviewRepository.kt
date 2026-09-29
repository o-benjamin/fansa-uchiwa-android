package com.fansauchiwa.data.repository

import android.app.Activity
import android.util.Log
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.data.infra.AppInstallDataSource
import com.fansauchiwa.data.infra.InAppReviewDataSource
import com.fansauchiwa.data.infra.InAppReviewHistoryDataSource
import com.fansauchiwa.inappreview.InAppReviewPolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * アプリ内レビュー依頼（#243）。うちわの保存が成功した直後に、条件（[InAppReviewPolicy]）を満たせば依頼を出す
 *
 * どのメソッドも例外を外に出さない。レビュー依頼や記録に失敗しても、保存の流れを止めないため
 */
interface InAppReviewRepository {
    /**
     * 保存の成功を記録する（条件の「保存成功の回数」に使う）
     */
    suspend fun recordSaveSuccess()

    /**
     * 条件を満たしていればレビュー依頼を出す。条件を満たさなければ何もしない
     *
     * @param activity 依頼の画面を出す Activity（広告などが前面にない状態で渡すこと）
     */
    suspend fun requestReviewIfEligible(activity: Activity)
}

class InAppReviewRepositoryImpl @Inject constructor(
    private val historyDataSource: InAppReviewHistoryDataSource,
    private val appInstallDataSource: AppInstallDataSource,
    private val inAppReviewDataSource: InAppReviewDataSource,
    private val analyticsRepository: AnalyticsRepository,
    private val crashReportingRepository: CrashReportingRepository
) : InAppReviewRepository {

    override suspend fun recordSaveSuccess() {
        runCatchingHistory { historyDataSource.incrementSaveSuccessCount() }
    }

    override suspend fun requestReviewIfEligible(activity: Activity) {
        val nowMillis = System.currentTimeMillis()
        val isEligible = runCatchingHistory { isEligible(nowMillis) } ?: return
        if (!isEligible) return

        // Play との通信に時間がかかり、その間に画面を閉じると処理ごと止まる。
        // そのとき「試みた」と記録すると30日出せなくなるため、日時の記録は依頼の直前まで遅らせる
        val reviewInfo = runCatchingPlay { inAppReviewDataSource.requestReviewInfo() } ?: return
        runCatchingHistory { historyDataSource.setLastRequestedAtMillis(nowMillis) } ?: return
        analyticsRepository.logEvent(AnalyticsEvent(AnalyticsActions.IN_APP_REVIEW_REQUEST))
        runCatchingPlay { inAppReviewDataSource.launchReviewFlow(activity, reviewInfo) }
    }

    private suspend fun isEligible(nowMillis: Long): Boolean = InAppReviewPolicy.shouldRequest(
        saveSuccessCount = historyDataSource.getSaveSuccessCountStream().first(),
        firstInstallTimeMillis = appInstallDataSource.getFirstInstallTimeMillisStream().first(),
        lastRequestedAtMillis = historyDataSource.getLastRequestedAtMillisStream().first(),
        nowMillis = nowMillis
    )

    /**
     * 記録（DataStore）の読み書き。失敗すると依頼がずっと出なくなるため、Crashlytics にも残す
     */
    private suspend fun <T> runCatchingHistory(block: suspend () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "レビュー依頼の記録の読み書きに失敗", e)
        crashReportingRepository.recordException(e)
        null
    }

    /**
     * Play への依頼。Play ストアがない端末などで失敗するのは想定内なので、ログだけ残す
     */
    private suspend fun <T> runCatchingPlay(block: suspend () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Play へのレビュー依頼に失敗", e)
        null
    }

    private companion object {
        const val TAG = "InAppReview"
    }
}
