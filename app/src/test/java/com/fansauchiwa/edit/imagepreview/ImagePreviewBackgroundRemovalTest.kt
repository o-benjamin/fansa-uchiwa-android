package com.fansauchiwa.edit.imagepreview

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.fansauchiwa.IMAGE_URI_ARG
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.analytics.BackgroundRemovalParams
import com.fansauchiwa.data.BackgroundRemovalException
import com.fansauchiwa.data.BackgroundRemovalFailureReason
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.CrashReportingRepository
import com.fansauchiwa.data.repository.ImageProcessingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImagePreviewBackgroundRemovalTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var imageProcessingRepository: ImageProcessingRepository
    private lateinit var adMobRepository: AdMobRepository
    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var crashReportingRepository: CrashReportingRepository

    private val originalUri: Uri = mockk()
    private val transparentUri: Uri = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Uri::class)
        every { Uri.parse(ORIGINAL_URI_STRING) } returns originalUri
        imageProcessingRepository = mockk()
        adMobRepository = mockk(relaxed = true)
        analyticsRepository = mockk(relaxed = true)
        crashReportingRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ImagePreviewViewModel(
        savedStateHandle = SavedStateHandle(mapOf(IMAGE_URI_ARG to ORIGINAL_URI_STRING)),
        imageProcessingRepository = imageProcessingRepository,
        adMobRepository = adMobRepository,
        analyticsRepository = analyticsRepository,
        crashReportingRepository = crashReportingRepository
    )

    @Test
    fun showTransparent_Success_ShowsTransparentImageAndLogsSuccess() = runTest(testDispatcher) {
        coEvery { imageProcessingRepository.removeBackground(originalUri) } returns
            Result.success(transparentUri)
        val viewModel = createViewModel()

        viewModel.showTransparent()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ImagePreviewUiState.Ready.ShowingTransparent.Success)
        assertEquals(
            transparentUri,
            (state as ImagePreviewUiState.Ready.ShowingTransparent.Success).transparentUri
        )
        coVerify {
            analyticsRepository.logEvent(AnalyticsEvent(AnalyticsActions.BACKGROUND_REMOVAL_SUCCESS))
        }
        verify { adMobRepository.loadInterstitialAd() }
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun showTransparent_ModuleTimeout_EmitsReasonAndLogsFailure() = runTest(testDispatcher) {
        assertFailureHandled(
            error = BackgroundRemovalException(BackgroundRemovalFailureReason.MODULE_TIMEOUT),
            expectedReason = BackgroundRemovalFailureReason.MODULE_TIMEOUT
        )
    }

    @Test
    fun showTransparent_NoSubject_EmitsReasonAndLogsFailure() = runTest(testDispatcher) {
        assertFailureHandled(
            error = BackgroundRemovalException(BackgroundRemovalFailureReason.NO_SUBJECT),
            expectedReason = BackgroundRemovalFailureReason.NO_SUBJECT
        )
    }

    @Test
    fun showTransparent_UnknownException_TreatedAsProcessFailed() = runTest(testDispatcher) {
        assertFailureHandled(
            error = IllegalStateException("unexpected"),
            expectedReason = BackgroundRemovalFailureReason.PROCESS_FAILED
        )
    }

    @Test
    fun showOriginal_WhileRemoving_KeepsOriginalAfterSuccess() = runTest(testDispatcher) {
        val removal = CompletableDeferred<Result<Uri>>()
        coEvery { imageProcessingRepository.removeBackground(originalUri) } coAnswers { removal.await() }
        val viewModel = createViewModel()

        viewModel.showTransparent()
        advanceUntilIdle()
        viewModel.showOriginal()
        removal.complete(Result.success(transparentUri))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ImagePreviewUiState.Ready.ShowingOriginal)

        // 結果はキャッシュされ、もう一度押すとすぐに表示される
        viewModel.showTransparent()
        assertTrue(viewModel.uiState.value is ImagePreviewUiState.Ready.ShowingTransparent.Success)
    }

    @Test
    fun showOriginal_WhileRemoving_KeepsOriginalAndEmitsErrorAfterFailure() = runTest(testDispatcher) {
        val removal = CompletableDeferred<Result<Uri>>()
        coEvery { imageProcessingRepository.removeBackground(originalUri) } coAnswers { removal.await() }
        val viewModel = createViewModel()
        var emittedReason: BackgroundRemovalFailureReason? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            emittedReason = viewModel.errorEvent.first()
        }

        viewModel.showTransparent()
        advanceUntilIdle()
        viewModel.showOriginal()
        removal.complete(
            Result.failure(BackgroundRemovalException(BackgroundRemovalFailureReason.NO_SUBJECT))
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ImagePreviewUiState.Ready.ShowingOriginal)
        assertEquals(BackgroundRemovalFailureReason.NO_SUBJECT, emittedReason)
    }

    @Test
    fun showTransparent_TappedAgainWhileErrorIsDelivered_StartsNewRemoval() = runTest(testDispatcher) {
        val unavailable = Result.failure<Uri>(
            BackgroundRemovalException(BackgroundRemovalFailureReason.MODULE_UNAVAILABLE)
        )
        coEvery { imageProcessingRepository.removeBackground(originalUri) } returnsMany listOf(
            unavailable,
            unavailable,
            Result.success(transparentUri)
        )
        val viewModel = createViewModel()
        // 画面がスナックバーを表示している間を再現するため、受け取ったまま戻らない受け手にする
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.errorEvent.collect { awaitCancellation() }
        }

        // 1回目の失敗：受け手が止まる
        viewModel.showTransparent()
        advanceUntilIdle()
        // 2回目の失敗：受け手が止まっているため、エラー通知で待ったままになる
        viewModel.showTransparent()
        advanceUntilIdle()
        // 3回目：2回目の処理がエラー通知で待っていても、新しく始まる
        viewModel.showTransparent()
        advanceUntilIdle()

        coVerify(exactly = 3) { imageProcessingRepository.removeBackground(originalUri) }
        assertTrue(viewModel.uiState.value is ImagePreviewUiState.Ready.ShowingTransparent.Success)
    }

    @Test
    fun showTransparent_TappedTwiceWhileRemoving_StartsRemovalOnce() = runTest(testDispatcher) {
        val removal = CompletableDeferred<Result<Uri>>()
        coEvery { imageProcessingRepository.removeBackground(originalUri) } coAnswers { removal.await() }
        val viewModel = createViewModel()

        viewModel.showTransparent()
        advanceUntilIdle()
        viewModel.showOriginal()
        viewModel.showTransparent()
        removal.complete(Result.success(transparentUri))
        advanceUntilIdle()

        coVerify(exactly = 1) { imageProcessingRepository.removeBackground(originalUri) }
        assertTrue(viewModel.uiState.value is ImagePreviewUiState.Ready.ShowingTransparent.Success)
    }

    @Test
    fun completeManualCorrection_Failure_RecordsExceptionAndEmitsProcessFailed() = runTest(testDispatcher) {
        coEvery { imageProcessingRepository.removeBackground(originalUri) } returns
            Result.success(transparentUri)
        val correctionError = IllegalStateException("manual correction failed")
        coEvery {
            imageProcessingRepository.applyManualCorrection(any(), any(), any(), any())
        } returns Result.failure(correctionError)
        val viewModel = createViewModel()
        var emittedReason: BackgroundRemovalFailureReason? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            emittedReason = viewModel.errorEvent.first()
        }

        viewModel.showTransparent()
        advanceUntilIdle()
        viewModel.startManualCorrection()
        viewModel.addPath(mockk(relaxed = true), 1f)
        viewModel.completeManualCorrection(containerWidth = 100, containerHeight = 100)
        advanceUntilIdle()

        assertEquals(BackgroundRemovalFailureReason.PROCESS_FAILED, emittedReason)
        assertTrue(
            viewModel.uiState.value is ImagePreviewUiState.Ready.ShowingTransparent.ManualCorrection
        )
        verify { crashReportingRepository.recordException(correctionError) }
    }

    private fun TestScope.assertFailureHandled(
        error: Throwable,
        expectedReason: BackgroundRemovalFailureReason
    ) {
        coEvery { imageProcessingRepository.removeBackground(originalUri) } returns
            Result.failure(error)
        val viewModel = createViewModel()
        var emittedReason: BackgroundRemovalFailureReason? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            emittedReason = viewModel.errorEvent.first()
        }

        viewModel.showTransparent()
        advanceUntilIdle()

        assertEquals(expectedReason, emittedReason)
        assertTrue(viewModel.uiState.value is ImagePreviewUiState.Ready.ShowingOriginal)
        coVerify {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    name = AnalyticsActions.BACKGROUND_REMOVAL_FAILURE,
                    params = mapOf(BackgroundRemovalParams.PARAM_REASON to expectedReason.analyticsValue)
                )
            )
        }
        verify(exactly = 0) { adMobRepository.loadInterstitialAd() }
        verify { crashReportingRepository.recordException(error) }
    }

    private companion object {
        const val ORIGINAL_URI_STRING = "content://media/picker/0/1"
    }
}
