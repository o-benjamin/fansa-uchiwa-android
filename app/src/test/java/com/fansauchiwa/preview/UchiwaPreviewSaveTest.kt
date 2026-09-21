package com.fansauchiwa.preview

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import com.fansauchiwa.EDIT_START_TIME_ARG
import com.fansauchiwa.FINAL_FONT_NAME_ARG
import com.fansauchiwa.FONT_SWITCH_COUNT_ARG
import com.fansauchiwa.IMAGE_PATH_ARG
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.analytics.AnalyticsEvent
import com.fansauchiwa.data.analytics.AnalyticsScreens
import com.fansauchiwa.data.analytics.FontSessionAnalyticsParams
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.AnalyticsRepository
import com.fansauchiwa.data.repository.MasterpieceRepository
import com.fansauchiwa.data.repository.SettingsRepository
import com.fansauchiwa.edit.FontFamilies
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
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        masterpieceRepository = mockk(relaxed = true)
        adMobRepository = mockk(relaxed = true)
        analyticsRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)

        every { adMobRepository.isLoadingRewardedAd } returns MutableStateFlow(false)
        coEvery { settingsRepository.getLastSavedFontName() } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        imagePath: String?,
        fontSwitchCount: Int = 0,
        finalFontName: String? = null,
        editStartTimeMillis: Long = 0L
    ): UchiwaPreviewViewModel {
        val savedStateHandle = SavedStateHandle().apply {
            if (imagePath != null) {
                val encoded = URLEncoder.encode(imagePath, "UTF-8")
                set(IMAGE_PATH_ARG, encoded)
            }
            set(FONT_SWITCH_COUNT_ARG, fontSwitchCount)
            set(FINAL_FONT_NAME_ARG, finalFontName)
            set(EDIT_START_TIME_ARG, editStartTimeMillis)
        }
        return UchiwaPreviewViewModel(
            masterpieceRepository = masterpieceRepository,
            adMobRepository = adMobRepository,
            analyticsRepository = analyticsRepository,
            settingsRepository = settingsRepository,
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

    // region フォント計測（#242）

    @Test
    fun showRewardedAdAndSave_hasFontData_logsFontSwitchAndRankAndSameAsLastBuckets() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(
            imagePath = imagePath,
            fontSwitchCount = 12,
            finalFontName = FontFamilies.KEI_FONT.name, // ordinal 0 → rank 1 → "1-5"
            editStartTimeMillis = 0L
        )
        val activity = mockk<Activity>()
        coEvery { settingsRepository.getLastSavedFontName() } returns FontFamilies.KEI_FONT.name

        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        coVerify {
            analyticsRepository.logEvent(
                match<AnalyticsEvent> {
                    it.name == AnalyticsActions.TAP_PREVIEW_EXPORT &&
                        it.params[FontSessionAnalyticsParams.FONT_SWITCH_BUCKET] == "11-20" &&
                        it.params[FontSessionAnalyticsParams.FINAL_FONT_RANK_BUCKET] == "1-5" &&
                        it.params[FontSessionAnalyticsParams.FONT_SAME_AS_LAST] == "true"
                }
            )
        }
        coVerify { settingsRepository.setLastSavedFontName(FontFamilies.KEI_FONT.name) }
    }

    @Test
    fun showRewardedAdAndSave_noTextDecoration_logsOnlySwitchAndDurationBuckets() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(
            imagePath = imagePath,
            fontSwitchCount = 0,
            finalFontName = null,
            editStartTimeMillis = 0L
        )
        val activity = mockk<Activity>()

        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        coVerify {
            analyticsRepository.logEvent(
                match<AnalyticsEvent> {
                    it.name == AnalyticsActions.TAP_PREVIEW_EXPORT &&
                        it.params[FontSessionAnalyticsParams.FONT_SWITCH_BUCKET] == "0" &&
                        !it.params.containsKey(FontSessionAnalyticsParams.FINAL_FONT_RANK_BUCKET) &&
                        !it.params.containsKey(FontSessionAnalyticsParams.FONT_SAME_AS_LAST)
                }
            )
        }
        coVerify(exactly = 0) { settingsRepository.setLastSavedFontName(any()) }
    }

    // endregion
}
