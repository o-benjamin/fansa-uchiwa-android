package com.fansauchiwa.preview

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import com.fansauchiwa.IMAGE_PATH_ARG
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.analytics.AnalyticsScreens
import com.fansauchiwa.analytics.FontSessionTracker
import com.fansauchiwa.analytics.ShareAnalyticsParams
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UchiwaPreviewShareTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var masterpieceRepository: MasterpieceRepository
    private lateinit var adMobRepository: AdMobRepository
    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var fontSessionTracker: FontSessionTracker
    private lateinit var inAppReviewRepository: InAppReviewRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        masterpieceRepository = mockk(relaxed = true)
        adMobRepository = mockk(relaxed = true)
        analyticsRepository = mockk(relaxed = true)
        fontSessionTracker = mockk(relaxed = true)
        inAppReviewRepository = mockk(relaxed = true)

        every { adMobRepository.isLoadingRewardedAd } returns MutableStateFlow(false)
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

    // region showRewardedAdAndShare - onUserEarnedReward → onAdDismissed

    @Test
    fun showRewardedAdAndShare_userEarnedRewardThenAdDismissed_shareImagePathIsSetToCurrentImagePath() =
        runTest {
            val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
            val viewModel = createViewModel(imagePath)
            val activity = mockk<Activity>()

            val onUserEarnedRewardSlot = slot<() -> Unit>()
            val onAdDismissedSlot = slot<() -> Unit>()
            every {
                adMobRepository.showRewardedAd(
                    activity = activity,
                    placement = AnalyticsScreens.PREVIEW_SCREEN,
                    waitForLoad = true,
                    onUserEarnedReward = capture(onUserEarnedRewardSlot),
                    onAdFailedOrSkipped = any(),
                    onAdDismissed = capture(onAdDismissedSlot)
                )
            } answers {
                onUserEarnedRewardSlot.captured.invoke()
                onAdDismissedSlot.captured.invoke()
            }

            assertEquals(false, viewModel.hasEarnedReward)

            viewModel.showRewardedAdAndShare(activity, ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON)
            advanceUntilIdle()

            assertEquals(true, viewModel.hasEarnedReward)
            assertEquals(imagePath, viewModel.uiState.value.shareImagePath)
        }

    @Test
    fun showRewardedAdAndShare_userEarnedRewardButAdNotYetDismissed_shareImagePathIsNull() =
        runTest {
            val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
            val viewModel = createViewModel(imagePath)
            val activity = mockk<Activity>()

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
                // onAdDismissed は呼ばない（広告がまだ表示中の状態）
                onUserEarnedRewardSlot.captured.invoke()
            }

            viewModel.showRewardedAdAndShare(activity, ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON)
            advanceUntilIdle()

            // 広告が閉じられるまで shareImagePath はセットされない
            assertNull(viewModel.uiState.value.shareImagePath)
        }

    // endregion

    // region showRewardedAdAndShare - onAdFailedOrSkipped

    @Test
    fun showRewardedAdAndShare_adFailedOrSkipped_shareImagePathIsSetToCurrentImagePath() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(imagePath)
        val activity = mockk<Activity>()

        val onAdFailedOrSkippedSlot = slot<() -> Unit>()
        every {
            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = any(),
                onAdFailedOrSkipped = capture(onAdFailedOrSkippedSlot),
                onAdDismissed = any()
            )
        } answers {
            onAdFailedOrSkippedSlot.captured.invoke()
        }

        viewModel.showRewardedAdAndShare(activity, ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON)
        advanceUntilIdle()

        assertEquals(imagePath, viewModel.uiState.value.shareImagePath)
    }

    // endregion

    // region showRewardedAdAndShare - imagePath is null

    @Test
    fun showRewardedAdAndShare_imagePathIsNull_shareImagePathRemainsNull() = runTest {
        val viewModel = createViewModel(imagePath = null)
        val activity = mockk<Activity>()

        val onUserEarnedRewardSlot = slot<() -> Unit>()
        val onAdDismissedSlot = slot<() -> Unit>()
        every {
            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = capture(onUserEarnedRewardSlot),
                onAdFailedOrSkipped = any(),
                onAdDismissed = capture(onAdDismissedSlot)
            )
        } answers {
            onUserEarnedRewardSlot.captured.invoke()
            onAdDismissedSlot.captured.invoke()
        }

        viewModel.showRewardedAdAndShare(activity, ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.shareImagePath)
    }

    // endregion

    // region showRewardedAdAndShare - 視聴済みスキップ

    @Test
    fun showRewardedAdAndShare_alreadyEarnedReward_shareImagePathIsSetWithoutShowingAd() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(imagePath)
        val activity = mockk<Activity>()

        // 1回目：広告を視聴済みにする
        val onUserEarnedRewardSlot = slot<() -> Unit>()
        val onAdDismissedSlot = slot<() -> Unit>()
        every {
            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = capture(onUserEarnedRewardSlot),
                onAdFailedOrSkipped = any(),
                onAdDismissed = capture(onAdDismissedSlot)
            )
        } answers {
            onUserEarnedRewardSlot.captured.invoke()
            onAdDismissedSlot.captured.invoke()
        }
        viewModel.showRewardedAdAndShare(activity, ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON)
        advanceUntilIdle()
        viewModel.clearShareImage()
        advanceUntilIdle()

        // 2回目：視聴済みのため広告なしで即座に shareImagePath がセットされる
        viewModel.showRewardedAdAndShare(activity, ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON)
        advanceUntilIdle()

        assertEquals(imagePath, viewModel.uiState.value.shareImagePath)
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

    // endregion

    // region clearShareImage

    @Test
    fun clearShareImage_afterShareImagePathIsSet_shareImagePathBecomesNull() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(imagePath)
        val activity = mockk<Activity>()

        val onUserEarnedRewardSlot = slot<() -> Unit>()
        val onAdDismissedSlot = slot<() -> Unit>()
        every {
            adMobRepository.showRewardedAd(
                activity = activity,
                placement = AnalyticsScreens.PREVIEW_SCREEN,
                waitForLoad = true,
                onUserEarnedReward = capture(onUserEarnedRewardSlot),
                onAdFailedOrSkipped = any(),
                onAdDismissed = capture(onAdDismissedSlot)
            )
        } answers {
            onUserEarnedRewardSlot.captured.invoke()
            onAdDismissedSlot.captured.invoke()
        }

        viewModel.showRewardedAdAndShare(activity, ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON)
        advanceUntilIdle()
        assertEquals(imagePath, viewModel.uiState.value.shareImagePath)

        viewModel.clearShareImage()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.shareImagePath)
    }

    @Test
    fun clearShareImage_whenShareImagePathIsAlreadyNull_shareImagePathRemainsNull() = runTest {
        val viewModel = createViewModel(imagePath = null)

        viewModel.clearShareImage()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.shareImagePath)
    }

    // endregion

    // region showRewardedAdAndShare - tap_preview_share の entry_point（#244）

    @Test
    fun showRewardedAdAndShare_fromPreviewButton_logsTapPreviewShareWithPreviewButtonEntryPoint() =
        runTest {
            val viewModel = createViewModel("/data/user/0/com.fansauchiwa/files/masterpiece.png")

            viewModel.showRewardedAdAndShare(
                mockk<Activity>(),
                ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON
            )
            advanceUntilIdle()

            coVerify(exactly = 1) {
                analyticsRepository.logEvent(
                    AnalyticsEvent(
                        AnalyticsActions.TAP_PREVIEW_SHARE,
                        mapOf(
                            ShareAnalyticsParams.PARAM_ENTRY_POINT to
                                ShareAnalyticsParams.ENTRY_POINT_PREVIEW_BUTTON
                        )
                    )
                )
            }
        }

    @Test
    fun showRewardedAdAndShare_fromSaveSuccessDialog_logsTapPreviewShareWithAfterSaveEntryPoint() =
        runTest {
            val viewModel = createViewModel("/data/user/0/com.fansauchiwa/files/masterpiece.png")

            viewModel.showRewardedAdAndShare(
                mockk<Activity>(),
                ShareAnalyticsParams.ENTRY_POINT_AFTER_SAVE
            )
            advanceUntilIdle()

            coVerify(exactly = 1) {
                analyticsRepository.logEvent(
                    AnalyticsEvent(
                        AnalyticsActions.TAP_PREVIEW_SHARE,
                        mapOf(
                            ShareAnalyticsParams.PARAM_ENTRY_POINT to
                                ShareAnalyticsParams.ENTRY_POINT_AFTER_SAVE
                        )
                    )
                )
            }
        }

    // endregion

    // region 保存の直後の共有（#244）

    @Test
    fun showRewardedAdAndShare_afterSaveEarnedReward_sharesWithoutShowingAdAgain() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(imagePath)
        val activity = mockk<Activity>()
        coEvery { masterpieceRepository.saveMasterpieceToGallery(imagePath) } returns true

        // 保存で広告を視聴して報酬を獲得する
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

        // 保存完了のダイアログから共有する
        viewModel.showRewardedAdAndShare(activity, ShareAnalyticsParams.ENTRY_POINT_AFTER_SAVE)
        advanceUntilIdle()

        assertEquals(imagePath, viewModel.uiState.value.shareImagePath)
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

    // endregion
}
