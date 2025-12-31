package com.example.fansauchiwa.data

import android.app.Activity
import android.content.Context
import com.example.fansauchiwa.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AdMobのリワード広告を管理するRepository
 * Google AdMob Best Practicesに従い、広告のロードと表示ロジックをカプセル化
 */
interface AdMobRepository {
    /**
     * リワード広告を事前にロードする（非同期）
     */
    fun loadRewardedAd()

    /**
     * リワード広告を表示する
     * @param activity 広告を表示するActivity
     * @param onUserEarnedReward ユーザーが報酬を獲得した際のコールバック
     * @param onAdFailedOrSkipped 広告の表示に失敗した、または広告がロードされていない場合のコールバック
     */
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdFailedOrSkipped: () -> Unit
    )
}

@Singleton
class AdMobRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AdMobRepository {
    private val adUnitId = BuildConfig.REWARDED_AD_UNIT_ID
    private var rewardedAd: RewardedAd? = null
    private var isLoadingAd = false

    override fun loadRewardedAd() {
        // 既にロード中または既にロード済みの場合はスキップ
        if (isLoadingAd || rewardedAd != null) {
            return
        }

        isLoadingAd = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadingAd = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isLoadingAd = false
                    loadRewardedAd()
                }
            }
        )
    }

    override fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdFailedOrSkipped: () -> Unit
    ) {
        val ad = rewardedAd

        // 広告がロードされていない場合は、即座に処理をスキップ（UX低下を防ぐため）
        if (ad == null) {
            onAdFailedOrSkipped()
            loadRewardedAd()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // 広告を閉じた後、次回のために新しい広告をロード
                rewardedAd = null
                loadRewardedAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // 広告表示に失敗した場合もスキップして処理を継続
                rewardedAd = null
                loadRewardedAd()
                onAdFailedOrSkipped()
            }
        }

        ad.show(activity) { _ ->
            onUserEarnedReward()
        }
    }
}

