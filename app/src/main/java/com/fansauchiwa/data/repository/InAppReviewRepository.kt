package com.fansauchiwa.data.repository

import android.app.Activity
import android.util.Log
import com.fansauchiwa.data.infra.AppInstallDataSource
import com.fansauchiwa.data.infra.InAppReviewDataSource
import com.fansauchiwa.data.infra.InAppReviewHistoryDataSource
import com.fansauchiwa.review.InAppReviewPolicy
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
    private val inAppReviewDataSource: InAppReviewDataSource
) : InAppReviewRepository {

    override suspend fun recordSaveSuccess() {
        runCatchingExceptCancellation("保存成功の記録に失敗") {
            historyDataSource.incrementSaveSuccessCount()
        }
    }

    override suspend fun requestReviewIfEligible(activity: Activity) {
        runCatchingExceptCancellation("レビュー依頼に失敗") {
            val nowMillis = System.currentTimeMillis()
            val shouldRequest = InAppReviewPolicy.shouldRequest(
                saveSuccessCount = historyDataSource.getSaveSuccessCount(),
                firstInstallTimeMillis = appInstallDataSource.getFirstInstallTimeMillisStream().first(),
                lastRequestedAtMillis = historyDataSource.getLastRequestedAtMillis(),
                nowMillis = nowMillis
            )
            if (!shouldRequest) return@runCatchingExceptCancellation

            // 表示できたかは Play の仕様上わからないため、依頼を試みる前に日時を記録する
            historyDataSource.setLastRequestedAtMillis(nowMillis)
            Log.d(TAG, "レビュー依頼を出す")
            inAppReviewDataSource.launchReviewFlow(activity)
        }
    }

    private suspend fun runCatchingExceptCancellation(
        failureMessage: String,
        block: suspend () -> Unit
    ) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, failureMessage, e)
        }
    }

    private companion object {
        const val TAG = "InAppReview"
    }
}
