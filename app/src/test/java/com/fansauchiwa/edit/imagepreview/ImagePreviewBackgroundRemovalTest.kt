package com.fansauchiwa.edit.imagepreview

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.fansauchiwa.IMAGE_URI_ARG
import com.fansauchiwa.data.BackgroundRemovalException
import com.fansauchiwa.data.BackgroundRemovalFailureReason
import com.fansauchiwa.data.analytics.AnalyticsActions
import com.fansauchiwa.data.analytics.AnalyticsEvent
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.AnalyticsRepository
import com.fansauchiwa.data.repository.ImageProcessingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        analyticsRepository = analyticsRepository
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
                    params = mapOf(AnalyticsActions.PARAM_REASON to expectedReason.analyticsValue)
                )
            )
        }
        verify(exactly = 0) { adMobRepository.loadInterstitialAd() }
    }

    private companion object {
        const val ORIGINAL_URI_STRING = "content://media/picker/0/1"
    }
}
