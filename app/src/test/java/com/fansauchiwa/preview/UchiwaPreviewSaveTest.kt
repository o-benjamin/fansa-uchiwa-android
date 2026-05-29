package com.fansauchiwa.preview

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import com.fansauchiwa.IMAGE_PATH_ARG
import com.fansauchiwa.data.AdMobRepository
import com.fansauchiwa.data.MasterpieceRepository
import com.fansauchiwa.data.analytics.AnalyticsScreens
import com.fansauchiwa.data.repository.AnalyticsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
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
import java.net.URLEncoder

@OptIn(ExperimentalCoroutinesApi::class)
class UchiwaPreviewSaveTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var masterpieceRepository: MasterpieceRepository
    private lateinit var adMobRepository: AdMobRepository
    private lateinit var analyticsRepository: AnalyticsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        masterpieceRepository = mockk(relaxed = true)
        adMobRepository = mockk(relaxed = true)
        analyticsRepository = mockk(relaxed = true)

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
                onAdDismissed = null
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
                onAdDismissed = null
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
                onAdDismissed = null
            )
        }
        verify(exactly = 2) { masterpieceRepository.saveMasterpieceToGallery(imagePath) }
    }
}
