package com.fansauchiwa.edit.aimaterial

import android.net.Uri
import com.fansauchiwa.analytics.AiMaterialParams
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.data.AiMaterialImage
import com.fansauchiwa.data.AiMaterialPayment
import com.fansauchiwa.data.AiMaterialRejectReason
import com.fansauchiwa.data.AiMaterialRejectedException
import com.fansauchiwa.data.repository.AiMaterialRepository
import com.fansauchiwa.data.repository.CrashReportingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiMaterialViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: AiMaterialRepository
    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var crashReportingRepository: CrashReportingRepository

    private val resultUri: Uri = mockk()
    private val image by lazy {
        AiMaterialImage(
            uri = resultUri,
            backgroundRemoved = true,
            payment = AiMaterialPayment.TICKET,
            ticketsRemaining = 4
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        analyticsRepository = mockk(relaxed = true)
        crashReportingRepository = mockk(relaxed = true)
        every { resultUri.toString() } returns RESULT_URI
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AiMaterialViewModel(repository, analyticsRepository, crashReportingRepository)

    private fun stubGenerate(result: Result<AiMaterialImage>) {
        coEvery { repository.generate(any(), any()) } returns result
    }

    @Test
    fun `成功すると結果を持ち、成功を計測する`() = runTest(testDispatcher) {
        stubGenerate(Result.success(image))
        val viewModel = createViewModel()

        viewModel.updatePrompt("  ピンクのハート  ")
        viewModel.generate()
        advanceUntilIdle()

        assertEquals(AiMaterialGeneration.Succeeded(image), viewModel.uiState.value.generation)
        coVerify { repository.generate("ピンクのハート", null) }
        coVerify {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.GENERATE_AI_MATERIAL,
                    mapOf(AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_SUCCESS)
                )
            )
        }
    }

    @Test
    fun `断られると理由を持ち、理由を計測して、例外は記録しない`() = runTest(testDispatcher) {
        stubGenerate(Result.failure(AiMaterialRejectedException(AiMaterialRejectReason.MODERATION)))
        val viewModel = createViewModel()

        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()

        assertEquals(
            AiMaterialGeneration.Rejected(AiMaterialRejectReason.MODERATION),
            viewModel.uiState.value.generation
        )
        coVerify {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.GENERATE_AI_MATERIAL,
                    mapOf(
                        AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_REJECTED,
                        AiMaterialParams.PARAM_REASON to "moderation"
                    )
                )
            )
        }
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun `理由のない失敗は例外を記録して、失敗を計測する`() = runTest(testDispatcher) {
        val error = IllegalStateException("通信できない")
        stubGenerate(Result.failure(error))
        val viewModel = createViewModel()

        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()

        assertEquals(AiMaterialGeneration.Failed, viewModel.uiState.value.generation)
        verify { crashReportingRepository.recordException(error) }
        coVerify {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.GENERATE_AI_MATERIAL,
                    mapOf(AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_FAILED)
                )
            )
        }
    }

    @Test
    fun `空白だけの文章と上限を超えた文章では送らない`() = runTest(testDispatcher) {
        stubGenerate(Result.success(image))
        val viewModel = createViewModel()

        viewModel.updatePrompt("   ")
        viewModel.generate()
        viewModel.updatePrompt("あ".repeat(AI_MATERIAL_MAX_PROMPT_LENGTH + 1))
        viewModel.generate()
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.generate(any(), any()) }
    }

    @Test
    fun `絵文字は1文字で数える`() {
        val state = AiMaterialUiState(prompt = "💖".repeat(AI_MATERIAL_MAX_PROMPT_LENGTH))

        assertTrue(state.canGenerate)
    }

    @Test
    fun `待っている間は二度押しで送らず、入力も変わらない`() = runTest(testDispatcher) {
        val pending = CompletableDeferred<Result<AiMaterialImage>>()
        coEvery { repository.generate(any(), any()) } coAnswers { pending.await() }
        val viewModel = createViewModel()

        viewModel.updatePrompt("ハート")
        viewModel.generate()
        viewModel.generate()
        viewModel.updatePrompt("星")
        advanceUntilIdle()

        assertEquals(AiMaterialGeneration.Generating, viewModel.uiState.value.generation)
        assertEquals("ハート", viewModel.uiState.value.prompt)
        coVerify(exactly = 1) { repository.generate(any(), any()) }

        pending.complete(Result.success(image))
        advanceUntilIdle()
    }

    @Test
    fun `結果を捨てるまでは、もう一度押しても回数を使わない`() = runTest(testDispatcher) {
        stubGenerate(Result.success(image))
        val viewModel = createViewModel()

        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()
        viewModel.generate()
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.generate(any(), any()) }

        viewModel.discardResult()
        viewModel.generate()
        advanceUntilIdle()
        coVerify(exactly = 2) { repository.generate(any(), any()) }
    }

    @Test
    fun `断られたあとに文章を直すと、断りの表示が消える`() = runTest(testDispatcher) {
        stubGenerate(Result.failure(AiMaterialRejectedException(AiMaterialRejectReason.BLOCKED_NAME)))
        val viewModel = createViewModel()

        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()
        viewModel.updatePrompt("星")

        assertEquals(AiMaterialGeneration.Idle, viewModel.uiState.value.generation)
    }

    @Test
    fun `置くと支払いの種類を計測して、二度押しでも1回だけ渡す`() = runTest(testDispatcher) {
        stubGenerate(Result.success(image))
        val viewModel = createViewModel()
        val confirmed = mutableListOf<String>()
        backgroundScope.launch { viewModel.confirmEvent.toList(confirmed) }

        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()
        viewModel.confirm()
        viewModel.confirm()
        advanceUntilIdle()
        // backgroundScope の収集は advanceUntilIdle では待たれない
        runCurrent()

        assertEquals(listOf(RESULT_URI), confirmed)
        coVerify(exactly = 1) {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.SAVE_AI_MATERIAL,
                    mapOf(AiMaterialParams.PARAM_PAYMENT to "ticket")
                )
            )
        }
    }

    @Test
    fun `購入画面へつなぐのは回数がないときと無料の非常ブレーキだけ`() {
        val offering = AiMaterialRejectReason.entries.filter {
            AiMaterialGeneration.Rejected(it).offersPurchase
        }

        assertEquals(
            listOf(AiMaterialRejectReason.NO_CREDITS, AiMaterialRejectReason.FREE_DAILY_LIMIT),
            offering
        )
        assertFalse(AiMaterialGeneration.Rejected(AiMaterialRejectReason.MODERATION).offersPurchase)
    }

    private companion object {
        const val RESULT_URI = "file:///cache/material.png"
    }
}
