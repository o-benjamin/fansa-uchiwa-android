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
import io.mockk.coVerifyOrder
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
    }

    @Test
    fun showRewardedAdAndSave_firstSaveEver_logsFontSameAsLastFalse() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(
            imagePath = imagePath,
            fontSwitchCount = 0,
            finalFontName = FontFamilies.KEI_FONT.name,
            editStartTimeMillis = 0L
        )
        val activity = mockk<Activity>()
        // 前回保存したフォントがまだ無い（初回保存）
        coEvery { settingsRepository.getLastSavedFontName() } returns null

        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        coVerify {
            analyticsRepository.logEvent(
                match<AnalyticsEvent> {
                    it.name == AnalyticsActions.TAP_PREVIEW_EXPORT &&
                        it.params[FontSessionAnalyticsParams.FONT_SAME_AS_LAST] == "false"
                }
            )
        }
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

    @Test
    fun showRewardedAdAndSave_gallerySaveSucceeds_savesFinalFontAsLastSavedFontName() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(
            imagePath = imagePath,
            fontSwitchCount = 0,
            finalFontName = FontFamilies.KEI_FONT.name,
            editStartTimeMillis = 0L
        )
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

        // ギャラリーへの保存が成功したときだけ、次回のfont_same_as_last比較用に上書きされる
        coVerify(exactly = 1) { settingsRepository.setLastSavedFontName(FontFamilies.KEI_FONT.name) }
    }

    @Test
    fun showRewardedAdAndSave_gallerySaveFails_doesNotOverwriteLastSavedFontName() = runTest {
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(
            imagePath = imagePath,
            fontSwitchCount = 0,
            finalFontName = FontFamilies.KEI_FONT.name,
            editStartTimeMillis = 0L
        )
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
                onAdDismissed = null
            )
        } answers {
            onUserEarnedRewardSlot.captured.invoke()
        }

        viewModel.showRewardedAdAndSave(activity)
        advanceUntilIdle()

        // ギャラリーへの保存が失敗した場合は、保存したことにしない
        coVerify(exactly = 0) { settingsRepository.setLastSavedFontName(any()) }
    }

    @Test
    fun showRewardedAdAndSave_alreadyEarnedReward_readsLastSavedFontNameBeforeOverwritingIt() =
        runTest {
            // 広告を視聴済みの状態で連続してエクスポートしたとき、font_same_as_last の比較用の読み取りが
            // 保存処理による上書きより先に行われること（読み取り/上書きの競合が無いこと）を確かめる
            val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
            val viewModel = createViewModel(
                imagePath = imagePath,
                fontSwitchCount = 0,
                finalFontName = FontFamilies.KEI_FONT.name,
                editStartTimeMillis = 0L
            )
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

            // 1回目：広告を視聴して保存（広告視聴済みフラグが立つ）
            viewModel.showRewardedAdAndSave(activity)
            advanceUntilIdle()

            // 2回目：広告をスキップしてすぐ保存される。読み取りが先に終わっていなければ
            // 1回目の書き込みと競合し、自分自身と比較してしまう
            viewModel.showRewardedAdAndSave(activity)
            advanceUntilIdle()

            coVerifyOrder {
                settingsRepository.getLastSavedFontName()
                settingsRepository.setLastSavedFontName(FontFamilies.KEI_FONT.name)
                settingsRepository.getLastSavedFontName()
                settingsRepository.setLastSavedFontName(FontFamilies.KEI_FONT.name)
            }
        }

    @Test
    fun showRewardedAdAndSave_calledAgainWhileSavePending_ignoresSecondCall() = runTest {
        // 1回目の保存処理が終わる前（isSaveButtonPressed=trueのまま）に連打されても、
        // 多重に実行されない（font_same_as_lastの読み取り/上書きが重ならない）ことを確かめる
        val imagePath = "/data/user/0/com.fansauchiwa/files/masterpiece.png"
        val viewModel = createViewModel(
            imagePath = imagePath,
            fontSwitchCount = 0,
            finalFontName = FontFamilies.KEI_FONT.name,
            editStartTimeMillis = 0L
        )
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
                onAdDismissed = null
            )
        }
    }

    // endregion
}
