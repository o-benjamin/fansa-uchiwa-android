package com.fansauchiwa.review

import java.util.concurrent.TimeUnit

/**
 * アプリ内レビュー依頼（#243）を出してよいかを決める。しつこくしないための条件をまとめている
 *
 * - 保存の成功が通算 [MIN_SAVE_SUCCESS_COUNT] 回以上（1回目の保存では出さない）
 * - インストールから [MIN_DAYS_SINCE_INSTALL] 日以上（インストール直後は出さない）
 * - 前回依頼を試みてから [MIN_DAYS_BETWEEN_REQUESTS] 日以上
 *
 * Play の launchReviewFlow は実際に表示されなくても成功で返るため、「表示できたか」ではなく
 * 「依頼を試みた日時」で間隔を判定する。
 */
object InAppReviewPolicy {
    const val MIN_SAVE_SUCCESS_COUNT = 2
    const val MIN_DAYS_SINCE_INSTALL = 3L
    const val MIN_DAYS_BETWEEN_REQUESTS = 30L

    /**
     * @param saveSuccessCount 今回の保存を含めた、保存成功の通算回数
     * @param firstInstallTimeMillis インストール日時。取得できない場合は null（出さない側に倒す）
     * @param lastRequestedAtMillis 前回依頼を試みた日時。一度も試みていない場合は null
     * @param nowMillis 現在時刻
     */
    fun shouldRequest(
        saveSuccessCount: Int,
        firstInstallTimeMillis: Long?,
        lastRequestedAtMillis: Long?,
        nowMillis: Long
    ): Boolean {
        if (saveSuccessCount < MIN_SAVE_SUCCESS_COUNT) return false
        if (firstInstallTimeMillis == null) return false
        if (nowMillis - firstInstallTimeMillis < TimeUnit.DAYS.toMillis(MIN_DAYS_SINCE_INSTALL)) {
            return false
        }
        if (lastRequestedAtMillis == null) return true
        return nowMillis - lastRequestedAtMillis >= TimeUnit.DAYS.toMillis(MIN_DAYS_BETWEEN_REQUESTS)
    }
}
