package com.fansauchiwa.preview

import com.fansauchiwa.analytics.AnalyticsScreens
import com.fansauchiwa.data.repository.AdMobRepository
import io.mockk.every
import io.mockk.slot

/**
 * プレビュー画面の [AdMobRepository.showRewardedAd] を差し替え、広告を見せた場合として
 * 受け取ったコールバック（報酬獲得・閉じた）を [invokeCallback] に渡す。保存と共有のテストで使う
 */
fun AdMobRepository.stubShowRewardedAd(
    invokeCallback: (onUserEarnedReward: () -> Unit, onAdDismissed: () -> Unit) -> Unit
) {
    val onUserEarnedRewardSlot = slot<() -> Unit>()
    val onAdDismissedSlot = slot<() -> Unit>()
    every {
        this@stubShowRewardedAd.showRewardedAd(
            activity = any(),
            placement = AnalyticsScreens.PREVIEW_SCREEN,
            waitForLoad = true,
            onUserEarnedReward = capture(onUserEarnedRewardSlot),
            onAdFailedOrSkipped = any(),
            onAdDismissed = capture(onAdDismissedSlot)
        )
    } answers {
        invokeCallback(onUserEarnedRewardSlot.captured, onAdDismissedSlot.captured)
    }
}

/**
 * プレビュー画面の [AdMobRepository.showRewardedAd] を、広告を出せなかった場合（読み込めていない・表示に失敗した）に差し替える。
 * AdMobRepositoryImpl と同じく、onAdFailedOrSkipped のあとに onAdDismissed を呼ぶ
 */
fun AdMobRepository.stubShowRewardedAdFailedOrSkipped() {
    val onAdFailedOrSkippedSlot = slot<() -> Unit>()
    val onAdDismissedSlot = slot<() -> Unit>()
    every {
        this@stubShowRewardedAdFailedOrSkipped.showRewardedAd(
            activity = any(),
            placement = AnalyticsScreens.PREVIEW_SCREEN,
            waitForLoad = true,
            onUserEarnedReward = any(),
            onAdFailedOrSkipped = capture(onAdFailedOrSkippedSlot),
            onAdDismissed = capture(onAdDismissedSlot)
        )
    } answers {
        onAdFailedOrSkippedSlot.captured.invoke()
        onAdDismissedSlot.captured.invoke()
    }
}
