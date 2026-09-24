package com.fansauchiwa.preview

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import com.fansauchiwa.IMAGE_PATH_ARG
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.analytics.AnalyticsScreens
import com.fansauchiwa.analytics.FontSessionAnalyticsParams
import com.fansauchiwa.analytics.FontSessionTracker
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.InAppReviewRepository
import com.fansauchiwa.data.repository.MasterpieceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UchiwaPreviewSaveTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var masterpieceRepository: MasterpieceRepository
    private lateinit var adMobRepository: AdMobRepository
    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var inAppReviewRepository: InAppReviewRepository
    private lateinit var fontSessionTracker: FontSessionTracker

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        masterpieceRepository = mockk(relaxed = true)
        adMobRepository = mockk(relaxed = true)
        analyticsRepository = mockk(relaxed = true)
        inAppReviewRepository = mockk(relaxed = true)
        fontSessionTracker = mockk(relaxed = true)

        every { adMobRepository.isLoadingRewardedAd } returns MutableStateFlow(false)
        coEvery { fontSessionTracker.exportParams(any()) } returns emptyMap()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(imagePath: String?): UchiwaPreviewViewModel {
        val savedStateHandle = SavedStateHandle().apply {
            if (imagePath != null) {
                val encoded = URLEncoder.encode(imagePath, "UTF-8")
                set(IMAGE_PATH_ARG, encoded)
            }
        }
        return UchiwaPreviewViewModel(
            masterpieceRepository = masterpieceRepository,
            adMobRepository = adMobRepository,
            analyticsRepository = analyticsRepository,
            inAppReviewRepository = inAppReviewRepository,
            fontSessionTracker = fontSessionTracker,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun showRewardedAdAndSave_userEarnedReward_marksRewardAsEarnedAndSavesImage() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(imagePath)
        val activity = mockk<Activity>()

        every { masterpieceRepository.saveMasterpieceToGallery(imagePath) } returns true

        val onUserEarnedRewardSlot = slot<() -> Unit>()
        every {
            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = capture(onUserEarnedRewardSlot),
                onAdFailedOrSkipped = any(),
                onAdDismissed = any()
            )
        } answers {
            onUserEarnedRewardSlot.captured.invoke()
        }

        assertEquals(false, viewModel.hasEarnedReward)

        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        assertEquals(true, viewModel.hasEarnedReward)
        assertEquals(true, viewModel.uiState.value.saveSuccess)
        verify(exactly = 1) { masterpieceRepository.saveMasterpieceToGallery(imagePath) }
    }

    @Test
    fun showRewardedAdAndSave_alreadyEarnedReward_skipsAdAndSavesImmediately() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(imagePath)
        val activity = mockk<Activity>()

        every { masterpieceRepository.saveMasterpieceToGallery(imagePath) } returns true

        val onUserEarnedRewardSlot = slot<() -> Unit>()
        every {
            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = capture(onUserEarnedRewardSlot),
                onAdFailedOrSkipped = any(),
                onAdDismissed = any()
            )
        } answers {
            onUserEarnedRewardSlot.captured.invoke()
        }

        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        viewModel.clearSaveStatus()
        advanceUntilIdle()

        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        verify(exactly = 1) {
            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = any(),
                onAdFailedOrSkipped = any(),
                onAdDismissed = any()
            )
        }
        verify(exactly = 2) { masterpieceRepository.saveMasterpieceToGallery(imagePath) }
    }

    @Test
    fun showRewardedAdAndSave_trackerHasFontData_logsExportEventWithTrackerParams() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece/uchiwa-1.png"
        val viewModel = createViewModel(imagePath)
        val activity = mockk<Activity>()
        val fontParams = mapOf<String, Any>(
            FontSessionAnalyticsParams.FONT_SWITCH_BUCKET to "11-20",
            FontSessionAnalyticsParams.EDIT_DURATION_BUCKET to "1-3m",
            FontSessionAnalyticsParams.FINAL_FONT_RANK_BUCKET to "1-5",
            FontSessionAnalyticsParams.FONT_SAME_AS_LAST to "true"
        )
        coEvery { fontSessionTracker.exportParams("uchiwa-1") } returns fontParams

        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            analyticsRepository.logEvent(AnalyticsEvent(AnalyticsActions.TAP_PREVIEW_EXPORT, fontParams))
        }
    }

    @Test
    fun showRewardedAdAndSave_trackerHasNoSession_logsExportEventWithoutParams() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece/uchiwa-1.png"
        val viewModel = createViewModel(imagePath)
        val activity = mockk<Activity>()

        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            analyticsRepository.logEvent(AnalyticsEvent(AnalyticsActions.TAP_PREVIEW_EXPORT, emptyMap()))
        }
        coVerify(exactly = 0) { inAppReviewRepository.recordSaveSuccess() }
    }

    @Test
    fun requestInAppReviewIfEligible_called_delegatesToRepositoryWithActivity() = runTest {
        val viewModel = createViewModel("/data/user/0/com.fansauchiwa/files/masterpiece.png")
        val activity = mockk<Activity>()

        viewModel.requestInAppReviewIfEligible(activity)
        advanceUntilIdle()

        coVerify(exactly = 1) { inAppReviewRepository.requestReviewIfEligible(activity) }
    }

    @Test
    fun showRewardedAdAndSave_gallerySaveSucceeds_recordsSaveSuccess() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(imagePath)
        val activity = mockk<Activity>()
        every { masterpieceRepository.saveMasterpieceToGallery(imagePath) } returns true

        val onUserEarnedRewardSlot = slot<() -> Unit>()
        every {
            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = capture(onUserEarnedRewardSlot),
                onAdFailedOrSkipped = any(),
                onAdDismissed = any()
            )
        } answers {
            onUserEarnedRewardSlot.captured.invoke()
        }

        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        // レビュー依頼（#243）の条件に使う保存成功の回数も数える
        coVerify(exactly = 1) { inAppReviewRepository.recordSaveSuccess() }
    }

    @Test
    fun showRewardedAdAndSave_gallerySaveFails_doesNotRecordSaveSuccess() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(imagePath)
        val activity = mockk<Activity>()
        every { masterpieceRepository.saveMasterpieceToGallery(imagePath) } returns false

        val onUserEarnedRewardSlot = slot<() -> Unit>()
        every {
            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = capture(onUserEarnedRewardSlot),
                onAdFailedOrSkipped = any(),
                onAdDismissed = any()
            )
        } answers {
            onUserEarnedRewardSlot.captured.invoke()
        }

        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        // ギャラリーへの保存が失敗した場合は、保存したことにしない
        coVerify(exactly = 0) { inAppReviewRepository.recordSaveSuccess() }
    }

    @Test
    fun showRewardedAdAndSave_calledAgainWhileSavePending_ignoresSecondCall() = runTest {
        // 1回目の保存処理が終わる前（isSaveButtonPressed=trueのまま）に連打されても、
        // 多重に実行されないことを確かめる
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(imagePath)
        val activity = mockk<Activity>()
        // adMobRepositoryはrelaxedモックなので、showRewardedAdはスタブしなければ何もしない
        // （広告のコールバックが呼ばれず、保存処理が保留中の状態を維持する）

        viewModel.showRewardedAdAndSave(activity)
        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        verify(exactly = 1) {
            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = any(),
                onAdFailedOrSkipped = any(),
                onAdDismissed = any()
            )
        }
    }

    @Test
    fun showRewardedAdAndSave_adDismissedWithoutReward_reEnablesSaveButtonForRetry() = runTest {
        // 広告を最後まで見ずに閉じた場合（報酬未獲得）は保存処理が実行されないため、
        // 連打防止用のフラグを戻して再タップできるようにする
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(imagePath)
        val activity = mockk<Activity>()
        every { masterpieceRepository.saveMasterpieceToGallery(imagePath) } returns true

        val onAdDismissedSlot = slot<() -> Unit>()
        every {
            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = any(),
                onAdFailedOrSkipped = any(),
                onAdDismissed = capture(onAdDismissedSlot)
            )
        } answers {
            // 報酬を獲得せずに広告が閉じられた状況を再現する
            onAdDismissedSlot.captured.invoke()
        }

        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isSaveButtonPressed)

        // 再タップできること（連打防止のガードで弾かれていないこと）
        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        verify(exactly = 2) {
            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = any(),
                onAdFailedOrSkipped = any(),
                onAdDismissed = any()
            )
        }
    }

    @Test
    fun showRewardedAdAndSave_adFailedAndDismissedFireTogether_stillSavesExactlyOnce() = runTest {
        // AdMobRepositoryの実装では、広告の表示に失敗した場合 onAdFailedOrSkipped と onAdDismissed の
        // 両方が呼ばれる。この場合に保存処理が重複したり、状態が壊れたりしないことを確かめる
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(imagePath)
        val activity = mockk<Activity>()
        every { masterpieceRepository.saveMasterpieceToGallery(imagePath) } returns true

        val onAdFailedOrSkippedSlot = slot<() -> Unit>()
        val onAdDismissedSlot = slot<() -> Unit>()
        every {
            adMobRepository.showRewardedAd(
                activity = activity,
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

        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isSaveButtonPressed)
        assertEquals(true, viewModel.uiState.value.saveSuccess)
        verify(exactly = 1) { masterpieceRepository.saveMasterpieceToGallery(imagePath) }
    }

}
