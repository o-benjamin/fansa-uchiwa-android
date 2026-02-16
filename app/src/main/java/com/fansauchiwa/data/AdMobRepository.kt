package com.fansauchiwa.data

import android.app.Activity
import android.content.Context
import com.fansauchiwa.BuildConfig
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.analytics.AnalyticsEvent
import com.fansauchiwa.data.infra.AnalyticsDataSource
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AdMobのリワード広告とインタースティシャル広告を管理するRepository
 * Google AdMob Best Practicesに従い、広告のロードと表示ロジックをカプセル化
 */
interface AdMobRepository {
    /**
     * リワード広告のロード状態
     */
    val isLoadingRewardedAd: StateFlow<Boolean>

    /**
     * リワード広告を事前にロードする（非同期）
     */
    fun loadRewardedAd()

    /**
     * リワード広告を表示する
     * @param activity 広告を表示するActivity
     * @param placement 広告の表示場所（Analytics計測用）
     * @param waitForLoad trueの場合、ロード中の広告のロードが完了するまで待つ。falseの場合、ロード中であれば即座にスキップ
     * @param onUserEarnedReward ユーザーが報酬を獲得した際のコールバック
     * @param onAdFailedOrSkipped 広告の表示に失敗した、または広告がロードされていない場合のコールバック
     */
    fun showRewardedAd(
        activity: Activity,
        placement: String,
        waitForLoad: Boolean,
        onUserEarnedReward: () -> Unit,
        onAdFailedOrSkipped: () -> Unit
    )

    /**
     * インタースティシャル広告を事前にロードする（非同期）
     */
    fun loadInterstitialAd()

    /**
     * インタースティシャル広告を表示する
     * @param activity 広告を表示するActivity
     * @param onAdClosed 広告が閉じられた際のコールバック
     */
    fun showInterstitialAd(
        activity: Activity,
        onAdClosed: () -> Unit
    )
}

@Singleton
class AdMobRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsDataSource: AnalyticsDataSource
) : AdMobRepository {
    private val adUnitId = BuildConfig.REWARDED_AD_UNIT_ID
    private var rewardedAd: RewardedAd? = null

    private val _isLoadingRewardedAd = MutableStateFlow(false)
    override val isLoadingRewardedAd: StateFlow<Boolean> = _isLoadingRewardedAd.asStateFlow()

    private val interstitialAdUnitId = BuildConfig.INTERSTITIAL_AD_UNIT_ID
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitialAd = false

    // Analytics計測用のCoroutineScope（コールバック内で使用）
    private val analyticsScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun loadRewardedAd() {
        // 既にロード中または既にロード済みの場合はスキップ
        if (_isLoadingRewardedAd.value || rewardedAd != null) {
            return
        }

        _isLoadingRewardedAd.value = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _isLoadingRewardedAd.value = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    _isLoadingRewardedAd.value = false
                    loadRewardedAd()
                }
            }
        )
    }

    override fun showRewardedAd(
        activity: Activity,
        placement: String,
        waitForLoad: Boolean,
        onUserEarnedReward: () -> Unit,
        onAdFailedOrSkipped: () -> Unit
    ) {
        val ad = rewardedAd

        // 広告がロードされていない場合
        if (ad == null) {
            // ロード中で、かつwaitForLoad=trueの場合は、ロード完了を待つ
            if (_isLoadingRewardedAd.value && waitForLoad) {
                waitForRewardedAdLoad(activity, placement, onUserEarnedReward, onAdFailedOrSkipped)
                return
            }
            // それ以外の場合は即座に処理をスキップ（UX低下を防ぐため）
            onAdFailedOrSkipped()
            loadRewardedAd()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                // 広告表示開始時のAnalytics計測
                analyticsScope.launch {
                    analyticsDataSource.logEvent(
                        AnalyticsEvent(
                            name = AnalyticsActions.AD_REWARD_SHOW,
                            params = mapOf("placement" to placement)
                        )
                    )
                }
            }

            override fun onAdDismissedFullScreenContent() {
                // 広告を閉じた時のAnalytics計測
                analyticsScope.launch {
                    analyticsDataSource.logEvent(
                        AnalyticsEvent(
                            name = AnalyticsActions.AD_REWARD_DISMISSED,
                            params = mapOf("placement" to placement)
                        )
                    )
                }
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
            // 報酬獲得時のAnalytics計測
            analyticsScope.launch {
                analyticsDataSource.logEvent(
                    AnalyticsEvent(
                        name = AnalyticsActions.AD_REWARD_COMPLETE,
                        params = mapOf("placement" to placement)
                    )
                )
            }
            onUserEarnedReward()
        }
    }

    /**
     * リワード広告のロード完了を待って表示する
     * 一定時間経過してもロードが完了しない場合はスキップ
     */
    private fun waitForRewardedAdLoad(
        activity: Activity,
        placement: String,
        onUserEarnedReward: () -> Unit,
        onAdFailedOrSkipped: () -> Unit
    ) {
        analyticsScope.launch {
            val startTime = System.currentTimeMillis()
            val timeoutMillis = 5000L // 最大5秒待つ

            while (_isLoadingRewardedAd.value && System.currentTimeMillis() - startTime < timeoutMillis) {
                kotlinx.coroutines.delay(100)
            }

            // メインスレッドで広告を表示
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                if (rewardedAd != null) {
                    // ロードが完了したので再度showRewardedAdを呼び出す（waitForLoad=falseで無限ループ防止）
                    showRewardedAd(
                        activity,
                        placement,
                        false,
                        onUserEarnedReward,
                        onAdFailedOrSkipped
                    )
                } else {
                    // タイムアウトまたはロード失敗
                    onAdFailedOrSkipped()
                    loadRewardedAd()
                }
            }
        }
    }

    override fun loadInterstitialAd() {
        // 既にロード中または既にロード済みの場合はスキップ
        if (isLoadingInterstitialAd || interstitialAd != null) {
            return
        }

        isLoadingInterstitialAd = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingInterstitialAd = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isLoadingInterstitialAd = false
                }
            }
        )
    }

    override fun showInterstitialAd(
        activity: Activity,
        onAdClosed: () -> Unit
    ) {
        val ad = interstitialAd

        // 広告がロードされていない場合は、即座に処理をスキップ（UX低下を防ぐため）
        if (ad == null) {
            loadInterstitialAd()
            onAdClosed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                // 広告表示開始時のAnalytics計測
                analyticsScope.launch {
                    analyticsDataSource.logEvent(
                        AnalyticsEvent(
                            name = AnalyticsActions.AD_INTERSTITIAL_SHOW
                        )
                    )
                }
            }

            override fun onAdDismissedFullScreenContent() {
                // 広告を閉じた時のAnalytics計測
                analyticsScope.launch {
                    analyticsDataSource.logEvent(
                        AnalyticsEvent(
                            name = AnalyticsActions.AD_INTERSTITIAL_DISMISSED
                        )
                    )
                }
                // 広告を閉じた後、次回のために新しい広告をロード
                interstitialAd = null
                loadInterstitialAd()
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // 広告表示に失敗した場合もスキップして処理を継続
                interstitialAd = null
                loadInterstitialAd()
                onAdClosed()
            }
        }

        ad.show(activity)
    }
}

