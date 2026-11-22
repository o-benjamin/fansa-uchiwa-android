package com.fansauchiwa.edit.aimaterial

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.fansauchiwa.analytics.AiMaterialParams
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.analytics.AnalyticsScreens
import com.fansauchiwa.data.AiMaterialImage
import com.fansauchiwa.data.AiMaterialPayment
import com.fansauchiwa.data.AiMaterialRejectReason
import com.fansauchiwa.data.AiMaterialRejectedException
import com.fansauchiwa.data.AiMaterialReportLimitException
import com.fansauchiwa.data.AiMaterialReportReason
import com.fansauchiwa.data.AiMaterialUnconfirmedException
import com.fansauchiwa.data.UuidProvider
import com.fansauchiwa.data.GrantedTickets
import com.fansauchiwa.data.PlayBillingException
import com.fansauchiwa.data.TicketProduct
import com.fansauchiwa.data.TicketProductUnavailableException
import com.fansauchiwa.data.TicketPurchaseOutcome
import com.fansauchiwa.data.TicketPurchaseRejectReason
import com.fansauchiwa.data.TicketPurchaseRejectedException
import com.fansauchiwa.data.repository.AiMaterialRepository
import com.fansauchiwa.data.repository.CrashReportingRepository
import com.fansauchiwa.data.repository.TicketPurchaseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AiMaterialViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: AiMaterialRepository
    private lateinit var ticketPurchaseRepository: TicketPurchaseRepository
    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var crashReportingRepository: CrashReportingRepository
    private lateinit var uuidProvider: UuidProvider

    private val resultUri: Uri = mockk()
    private val image by lazy {
        AiMaterialImage(
            uri = resultUri,
            backgroundRemoved = true,
            payment = AiMaterialPayment.TICKET,
            ticketsRemaining = 4,
            model = MODEL
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        coEvery { repository.isOnDevice(any()) } returns true
        ticketPurchaseRepository = mockk()
        coEvery { ticketPurchaseRepository.grantUnconsumedPurchases() } returns Result.success(emptyList())
        coEvery { ticketPurchaseRepository.getTicketProduct() } returns Result.success(PRODUCT)
        analyticsRepository = mockk(relaxed = true)
        crashReportingRepository = mockk(relaxed = true)
        uuidProvider = mockk()
        every { uuidProvider.generate() } returnsMany List(10) { "request-${it + 1}" }
        every { resultUri.toString() } returns RESULT_URI
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        AiMaterialViewModel(
            repository,
            ticketPurchaseRepository,
            analyticsRepository,
            crashReportingRepository,
            uuidProvider,
            savedStateHandle
        )

    // プロセスが止められて作り直されたときの状態。作り直した ViewModel が同じ savedStateHandle に書き戻さないよう、すぐ止める
    private fun restoreUiState(savedStateHandle: SavedStateHandle): AiMaterialUiState =
        createViewModel(savedStateHandle).also { it.viewModelScope.cancel() }.uiState.value

    private fun stubGenerate(result: Result<AiMaterialImage>) {
        coEvery { repository.generate(any(), any(), any()) } returns result
    }

    @Test
    fun `画面の表示を計測する`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.logScreenView()
        advanceUntilIdle()

        coVerify { analyticsRepository.logScreenView(AnalyticsScreens.AI_MATERIAL_SCREEN) }
    }

    @Test
    fun `成功すると結果を持ち、成功と切り抜けたかを計測する`() = runTest(testDispatcher) {
        stubGenerate(Result.success(image))
        val viewModel = createViewModel()

        viewModel.updatePrompt("  ピンクのハート  ")
        viewModel.generate()
        advanceUntilIdle()

        assertEquals(AiMaterialGeneration.Succeeded(image), viewModel.uiState.value.generation)
        coVerify { repository.generate("ピンクのハート", null, "request-1") }
        coVerify {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.GENERATE_AI_MATERIAL,
                    mapOf(
                        AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_SUCCESS,
                        AiMaterialParams.PARAM_BACKGROUND_REMOVED to "true"
                    )
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
    fun `送る前の理由のない失敗は例外を記録して、失敗を計測する`() = runTest(testDispatcher) {
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
    fun `送ったあとの理由のない失敗は作れたか分からないとして、例外を記録して計測し、残りを出さない`() = runTest(testDispatcher) {
        // 残りが分かっている状態（4回）から始めて、作れたか分からないと 0 に戻ることを見る
        stubGenerate(Result.success(image))
        val viewModel = createViewModel()
        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()
        viewModel.discardResult()
        val error = AiMaterialUnconfirmedException(IOException("切れた"))
        stubGenerate(Result.failure(error))

        viewModel.generate()
        advanceUntilIdle()

        assertEquals(AiMaterialGeneration.Unconfirmed, viewModel.uiState.value.generation)
        assertEquals(0, viewModel.uiState.value.ticketsRemaining)
        assertTrue(viewModel.uiState.value.canGenerate)
        verify { crashReportingRepository.recordException(error) }
        coVerify {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.GENERATE_AI_MATERIAL,
                    mapOf(AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_UNCONFIRMED)
                )
            )
        }
    }

    @Test
    fun `作れたか分からないあとにもう一度作ると、同じ頼みの番号で頼む`() = runTest(testDispatcher) {
        stubGenerate(Result.failure(AiMaterialUnconfirmedException(IOException("切れた"))))
        val viewModel = createViewModel()
        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()
        stubGenerate(Result.failure(AiMaterialRejectedException(AiMaterialRejectReason.IN_PROGRESS)))
        viewModel.generate()
        advanceUntilIdle()
        stubGenerate(Result.success(image))

        viewModel.generate()
        advanceUntilIdle()

        assertEquals(AiMaterialGeneration.Succeeded(image), viewModel.uiState.value.generation)
        coVerify(exactly = 3) { repository.generate("ハート", null, "request-1") }
    }

    @Test
    fun `前の頼みをまだ作っていると断られたら、その文言を出し、購入の案内は開かない`() = runTest(testDispatcher) {
        stubGenerate(Result.failure(AiMaterialRejectedException(AiMaterialRejectReason.IN_PROGRESS)))
        val viewModel = createViewModel()
        viewModel.updatePrompt("ハート")

        viewModel.generate()
        advanceUntilIdle()

        assertEquals(
            AiMaterialGeneration.Rejected(AiMaterialRejectReason.IN_PROGRESS),
            viewModel.uiState.value.generation
        )
        assertEquals(AiMaterialPurchase.Closed, viewModel.uiState.value.purchase)
        assertTrue(viewModel.uiState.value.canGenerate)
    }

    @Test
    fun `作った素材を捨ててからもう一度作ると、新しい頼みの番号で頼む`() = runTest(testDispatcher) {
        stubGenerate(Result.success(image))
        val viewModel = createViewModel()
        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()
        viewModel.discardResult()

        viewModel.generate()
        advanceUntilIdle()

        coVerifyOrder {
            repository.generate("ハート", null, "request-1")
            repository.generate("ハート", null, "request-2")
        }
    }

    @Test
    fun `作っている途中でプロセスが止められたあとにもう一度作ると、止められる前と同じ頼みの番号で頼む`() = runTest(testDispatcher) {
        coEvery { repository.generate(any(), any(), any()) } coAnswers { CompletableDeferred<Result<AiMaterialImage>>().await() }
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()
        viewModel.viewModelScope.cancel()
        stubGenerate(Result.success(image))

        val restored = createViewModel(savedStateHandle)
        restored.generate()
        advanceUntilIdle()

        coVerify(exactly = 2) { repository.generate("ハート", null, "request-1") }
        assertEquals(AiMaterialGeneration.Succeeded(image), restored.uiState.value.generation)
    }

    @Test
    fun `戻した結果のファイルが消えていたあとにもう一度作ると、作ったときと同じ頼みの番号で頼む`() = runTest(testDispatcher) {
        stubGenerate(Result.success(image))
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()
        viewModel.viewModelScope.cancel()
        coEvery { repository.isOnDevice(image) } returns false

        val restored = createViewModel(savedStateHandle)
        advanceUntilIdle()
        restored.generate()
        advanceUntilIdle()

        coVerify(exactly = 2) { repository.generate("ハート", null, "request-1") }
    }

    @Test
    fun `作っている途中でプロセスが止められると、文章を戻して作れたか分からないと出す`() = runTest(testDispatcher) {
        coEvery { repository.generate(any(), any(), any()) } coAnswers { CompletableDeferred<Result<AiMaterialImage>>().await() }
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()
        viewModel.viewModelScope.cancel()

        val restored = restoreUiState(savedStateHandle)

        assertEquals(AiMaterialGeneration.Unconfirmed, restored.generation)
        assertEquals("ハート", restored.prompt)
    }

    @Test
    fun `結果を見ている間にプロセスが止められると、結果と文章と残りを戻す`() = runTest(testDispatcher) {
        stubGenerate(Result.success(image))
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()

        val restored = restoreUiState(savedStateHandle)

        assertEquals(AiMaterialGeneration.Succeeded(image), restored.generation)
        assertEquals("ハート", restored.prompt)
        assertEquals(image.ticketsRemaining, restored.ticketsRemaining)
        assertTrue(restored.canReport)
    }

    @Test
    fun `戻した結果のファイルが消えていたら、作れたか分からないと出して残りはそのまま出す`() = runTest(testDispatcher) {
        stubGenerate(Result.success(image))
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()
        viewModel.viewModelScope.cancel()
        coEvery { repository.isOnDevice(image) } returns false

        val restored = createViewModel(savedStateHandle)
        advanceUntilIdle()

        assertEquals(AiMaterialGeneration.Unconfirmed, restored.uiState.value.generation)
        assertEquals(image.ticketsRemaining, restored.uiState.value.ticketsRemaining)
    }

    @Test
    fun `結果を捨てたあとにプロセスが止められると、入力に戻り残りは出す`() = runTest(testDispatcher) {
        stubGenerate(Result.success(image))
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()
        viewModel.discardResult()
        advanceUntilIdle()

        val restored = restoreUiState(savedStateHandle)

        assertEquals(AiMaterialGeneration.Idle, restored.generation)
        assertEquals("ハート", restored.prompt)
        assertEquals(image.ticketsRemaining, restored.ticketsRemaining)
    }

    @Test
    fun `作れたか分からないあとに文章を直してからプロセスが止められると、入力に戻る`() = runTest(testDispatcher) {
        stubGenerate(Result.failure(AiMaterialUnconfirmedException(IOException("切れた"))))
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()
        viewModel.updatePrompt("星")
        advanceUntilIdle()

        val restored = restoreUiState(savedStateHandle)

        assertEquals(AiMaterialGeneration.Idle, restored.generation)
        assertEquals("星", restored.prompt)
    }

    @Test
    fun `保存したものがなければ、空の入力から始める`() = runTest(testDispatcher) {
        assertEquals(AiMaterialUiState(), createViewModel().uiState.value)
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

        coVerify(exactly = 0) { repository.generate(any(), any(), any()) }
    }

    @Test
    fun `絵文字は1文字で数える`() {
        val state = AiMaterialUiState(prompt = "💖".repeat(AI_MATERIAL_MAX_PROMPT_LENGTH))

        assertTrue(state.canGenerate)
    }

    @Test
    fun `文字数は前後の空白を除いて数え、上限を超えたら超えたと出す`() {
        val atLimit = AiMaterialUiState(prompt = "  " + "あ".repeat(AI_MATERIAL_MAX_PROMPT_LENGTH) + "  ")
        val overLimit = AiMaterialUiState(prompt = "あ".repeat(AI_MATERIAL_MAX_PROMPT_LENGTH + 1))

        assertEquals(AI_MATERIAL_MAX_PROMPT_LENGTH, atLimit.promptLength)
        assertFalse(atLimit.isPromptTooLong)
        assertTrue(overLimit.isPromptTooLong)
    }

    @Test
    fun `待っている間は二度押しで送らず、入力も変わらない`() = runTest(testDispatcher) {
        val pending = CompletableDeferred<Result<AiMaterialImage>>()
        coEvery { repository.generate(any(), any(), any()) } coAnswers { pending.await() }
        val viewModel = createViewModel()

        viewModel.updatePrompt("ハート")
        viewModel.generate()
        viewModel.generate()
        viewModel.updatePrompt("星")
        advanceUntilIdle()

        assertEquals(AiMaterialGeneration.Generating, viewModel.uiState.value.generation)
        assertEquals("ハート", viewModel.uiState.value.prompt)
        coVerify(exactly = 1) { repository.generate(any(), any(), any()) }

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
        coVerify(exactly = 1) { repository.generate(any(), any(), any()) }

        viewModel.discardResult()
        viewModel.generate()
        advanceUntilIdle()
        coVerify(exactly = 2) { repository.generate(any(), any(), any()) }
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

    @Test
    fun `報告すると文章と理由とモデル名を送り、送れたことを持って計測する`() = runTest(testDispatcher) {
        stubGenerate(Result.success(image))
        coEvery { repository.report(any(), any(), any()) } returns Result.success(Unit)
        val viewModel = generatedViewModel("  ピンクのハート  ")

        viewModel.report(AiMaterialReportReason.REAL_PERSON)
        advanceUntilIdle()

        assertEquals(AiMaterialReportStatus.SENT, viewModel.uiState.value.reportStatus)
        assertFalse(viewModel.uiState.value.canReport)
        coVerify { repository.report("ピンクのハート", AiMaterialReportReason.REAL_PERSON, MODEL) }
        coVerify {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.REPORT_AI_MATERIAL,
                    mapOf(
                        AiMaterialParams.PARAM_REASON to "real_person",
                        AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_SUCCESS
                    )
                )
            )
        }
    }

    @Test
    fun `報告を送れなければ例外を記録し、もう一度送れるようにする`() = runTest(testDispatcher) {
        val error = IllegalStateException("通信できない")
        stubGenerate(Result.success(image))
        coEvery { repository.report(any(), any(), any()) } returns Result.failure(error)
        val viewModel = generatedViewModel("ハート")

        viewModel.report(AiMaterialReportReason.OTHER)
        advanceUntilIdle()

        assertEquals(AiMaterialReportStatus.FAILED, viewModel.uiState.value.reportStatus)
        assertTrue(viewModel.uiState.value.canReport)
        verify { crashReportingRepository.recordException(error) }
        coVerify {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.REPORT_AI_MATERIAL,
                    mapOf(
                        AiMaterialParams.PARAM_REASON to "other",
                        AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_FAILED
                    )
                )
            )
        }
    }

    @Test
    fun `1人1日の上限で断られたら、押せなくして上限と計測し、例外は記録しない`() = runTest(testDispatcher) {
        stubGenerate(Result.success(image))
        coEvery { repository.report(any(), any(), any()) } returns
            Result.failure(AiMaterialReportLimitException())
        val viewModel = generatedViewModel("ハート")

        viewModel.report(AiMaterialReportReason.INAPPROPRIATE)
        advanceUntilIdle()

        assertEquals(AiMaterialReportStatus.LIMIT_REACHED, viewModel.uiState.value.reportStatus)
        assertFalse(viewModel.uiState.value.canReport)
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
        coVerify {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.REPORT_AI_MATERIAL,
                    mapOf(
                        AiMaterialParams.PARAM_REASON to "inappropriate",
                        AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_REJECTED
                    )
                )
            )
        }
    }

    @Test
    fun `送っている間に画面を閉じても、報告を送り終えて計測する`() = runTest(testDispatcher) {
        val pending = CompletableDeferred<Result<Unit>>()
        stubGenerate(Result.success(image))
        coEvery { repository.report(any(), any(), any()) } coAnswers { pending.await() }
        val viewModel = generatedViewModel("ハート")

        viewModel.report(AiMaterialReportReason.OTHER)
        advanceUntilIdle()
        // 画面を閉じると ViewModel が片づけられ、viewModelScope が取り消される
        viewModel.viewModelScope.cancel()
        pending.complete(Result.success(Unit))
        advanceUntilIdle()

        coVerify {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.REPORT_AI_MATERIAL,
                    mapOf(
                        AiMaterialParams.PARAM_REASON to "other",
                        AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_SUCCESS
                    )
                )
            )
        }
    }

    @Test
    fun `送っている間と送ったあとは、同じ素材をもう一度報告しない`() = runTest(testDispatcher) {
        val pending = CompletableDeferred<Result<Unit>>()
        stubGenerate(Result.success(image))
        coEvery { repository.report(any(), any(), any()) } coAnswers { pending.await() }
        val viewModel = generatedViewModel("ハート")

        viewModel.report(AiMaterialReportReason.OTHER)
        viewModel.report(AiMaterialReportReason.OTHER)
        advanceUntilIdle()
        assertEquals(AiMaterialReportStatus.SENDING, viewModel.uiState.value.reportStatus)

        pending.complete(Result.success(Unit))
        advanceUntilIdle()
        viewModel.report(AiMaterialReportReason.OTHER)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.report(any(), any(), any()) }
    }

    @Test
    fun `結果がないときは報告しない`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.updatePrompt("ハート")
        viewModel.report(AiMaterialReportReason.OTHER)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.report(any(), any(), any()) }
        assertEquals(AiMaterialReportStatus.NOT_SENT, viewModel.uiState.value.reportStatus)
    }

    @Test
    fun `送っている間に捨てたら、次の素材に報告の結果を持ち越さない`() = runTest(testDispatcher) {
        val pending = CompletableDeferred<Result<Unit>>()
        stubGenerate(Result.success(image))
        coEvery { repository.report(any(), any(), any()) } coAnswers { pending.await() }
        val viewModel = generatedViewModel("ハート")

        viewModel.report(AiMaterialReportReason.OTHER)
        advanceUntilIdle()
        viewModel.discardResult()
        assertEquals(AiMaterialReportStatus.NOT_SENT, viewModel.uiState.value.reportStatus)

        viewModel.generate()
        advanceUntilIdle()
        pending.complete(Result.success(Unit))
        advanceUntilIdle()

        assertEquals(AiMaterialReportStatus.NOT_SENT, viewModel.uiState.value.reportStatus)
        assertTrue(viewModel.uiState.value.canReport)
    }

    @Test
    fun `回数がないと断られたら購入の案内を開き、値段を出して計測する`() = runTest(testDispatcher) {
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        assertEquals(AiMaterialPurchase.Ready(PRODUCT, problem = null), viewModel.uiState.value.purchase)
        assertEquals(0, viewModel.uiState.value.ticketsRemaining)
        coVerify {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.VIEW_AI_MATERIAL_PAYWALL,
                    mapOf(
                        AiMaterialParams.PARAM_REASON to "no_credits",
                        AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_SUCCESS
                    )
                )
            )
        }
    }

    @Test
    fun `回数と関係のない断りでは購入の案内を開かない`() = runTest(testDispatcher) {
        val viewModel = rejectedViewModel(AiMaterialRejectReason.MODERATION)

        assertEquals(AiMaterialPurchase.Closed, viewModel.uiState.value.purchase)
        coVerify(exactly = 0) { ticketPurchaseRepository.getTicketProduct() }
    }

    @Test
    fun `案内を開いたときに足せていない購入があれば、それを足して買わせない`() = runTest(testDispatcher) {
        coEvery { ticketPurchaseRepository.grantUnconsumedPurchases() } returns
            Result.success(listOf(grantedTickets(alreadyGranted = false)))

        val viewModel = rejectedViewModel(AiMaterialRejectReason.FREE_DAILY_LIMIT)

        val state = viewModel.uiState.value
        assertEquals(AiMaterialPurchase.Closed, state.purchase)
        assertEquals(grantedTickets(alreadyGranted = false), state.addedTickets)
        assertEquals(TICKETS_REMAINING, state.ticketsRemaining)
        coVerify(exactly = 0) { ticketPurchaseRepository.getTicketProduct() }
        verifyPurchaseLogged(mapOf(AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_SUCCESS))
        coVerify(exactly = 0) {
            analyticsRepository.logEvent(match { it.name == AnalyticsActions.VIEW_AI_MATERIAL_PAYWALL })
        }
    }

    @Test
    fun `作っている間に画面に戻ったときの送り直しで足していたら、回数がないと断られても案内を開かず残りを持つ`() =
        runTest(testDispatcher) {
            val generation = CompletableDeferred<Result<AiMaterialImage>>()
            coEvery { repository.generate(any(), any(), any()) } coAnswers { generation.await() }
            coEvery { ticketPurchaseRepository.grantUnconsumedPurchases() } returnsMany listOf(
                Result.success(listOf(grantedTickets(alreadyGranted = false))),
                Result.success(emptyList())
            )
            val viewModel = createViewModel()
            viewModel.updatePrompt("ハート")
            viewModel.generate()
            runCurrent()

            viewModel.grantUnconsumedPurchasesOnResume()
            runCurrent()
            generation.complete(Result.failure(AiMaterialRejectedException(AiMaterialRejectReason.NO_CREDITS)))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(AiMaterialPurchase.Closed, state.purchase)
            assertEquals(grantedTickets(alreadyGranted = false), state.addedTickets)
            assertEquals(TICKETS_REMAINING, state.ticketsRemaining)
            coVerify(exactly = 0) { ticketPurchaseRepository.getTicketProduct() }
        }

    @Test
    fun `値段が取れなければ今は買えないと出し、例外を記録して、値段を出せなかったと計測する`() = runTest(testDispatcher) {
        val error = TicketProductUnavailableException()
        coEvery { ticketPurchaseRepository.getTicketProduct() } returns Result.failure(error)

        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        assertEquals(AiMaterialPurchase.Unavailable, viewModel.uiState.value.purchase)
        verify { crashReportingRepository.recordException(error) }
        coVerify {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.VIEW_AI_MATERIAL_PAYWALL,
                    mapOf(
                        AiMaterialParams.PARAM_REASON to "no_credits",
                        AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_FAILED
                    )
                )
            )
        }
    }

    @Test
    fun `案内は文章を直しても閉じない`() = runTest(testDispatcher) {
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        viewModel.updatePrompt("星")

        assertEquals(AiMaterialGeneration.Idle, viewModel.uiState.value.generation)
        assertEquals(AiMaterialPurchase.Ready(PRODUCT, problem = null), viewModel.uiState.value.purchase)
    }

    @Test
    fun `買えたら案内を閉じて足した回数と残りを出し、成功を計測する`() = runTest(testDispatcher) {
        stubPurchase(Result.success(TicketPurchaseOutcome.Granted(grantedTickets(alreadyGranted = false))))
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        viewModel.purchase(activity)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AiMaterialPurchase.Closed, state.purchase)
        assertEquals(grantedTickets(alreadyGranted = false), state.addedTickets)
        assertEquals(TICKETS_REMAINING, state.ticketsRemaining)
        // 作ると回数を使うので、買えても自動では作り直さない
        coVerify(exactly = 1) { repository.generate(any(), any(), any()) }
        verifyPurchaseLogged(mapOf(AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_SUCCESS))
    }

    @Test
    fun `前の購入が足し済みで残っていて買えなかったら、知らせずに理由を出して失敗と計測する`() = runTest(testDispatcher) {
        stubPurchase(Result.success(TicketPurchaseOutcome.AlreadyGranted(ticketsRemaining = 0)))
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        viewModel.purchase(activity)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(
            AiMaterialPurchase.Ready(PRODUCT, AiMaterialPurchaseProblem.PREVIOUS_PURCHASE_REMAINING),
            state.purchase
        )
        assertNull(state.addedTickets)
        assertEquals(0, state.ticketsRemaining)
        verifyPurchaseLogged(mapOf(AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_FAILED))
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun `買っている間は二度押しで2回買わない`() = runTest(testDispatcher) {
        val pending = CompletableDeferred<Result<TicketPurchaseOutcome>>()
        coEvery { ticketPurchaseRepository.purchase(any()) } coAnswers { pending.await() }
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        viewModel.purchase(activity)
        runCurrent()
        viewModel.purchase(activity)
        runCurrent()

        assertEquals(AiMaterialPurchase.Purchasing(PRODUCT), viewModel.uiState.value.purchase)
        coVerify(exactly = 1) { ticketPurchaseRepository.purchase(activity) }
        pending.complete(Result.success(TicketPurchaseOutcome.Cancelled))
        advanceUntilIdle()
    }

    @Test
    fun `支払いが済んでいなければ、済んだら開き直すよう出して計測する`() = runTest(testDispatcher) {
        stubPurchase(Result.success(TicketPurchaseOutcome.Pending))
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        viewModel.purchase(activity)
        advanceUntilIdle()

        assertEquals(
            AiMaterialPurchase.Ready(PRODUCT, AiMaterialPurchaseProblem.PENDING),
            viewModel.uiState.value.purchase
        )
        assertNull(viewModel.uiState.value.addedTickets)
        verifyPurchaseLogged(mapOf(AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_PENDING))
    }

    @Test
    fun `Play の購入の画面を閉じたら何も出さずに買えるままにして計測する`() = runTest(testDispatcher) {
        stubPurchase(Result.success(TicketPurchaseOutcome.Cancelled))
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        viewModel.purchase(activity)
        advanceUntilIdle()

        assertEquals(AiMaterialPurchase.Ready(PRODUCT, problem = null), viewModel.uiState.value.purchase)
        verifyPurchaseLogged(mapOf(AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_CANCELLED))
    }

    @Test
    fun `サーバーが断ったら理由を計測し、例外は記録しない`() = runTest(testDispatcher) {
        stubPurchase(Result.failure(TicketPurchaseRejectedException(TicketPurchaseRejectReason.OWNED_BY_OTHER)))
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        viewModel.purchase(activity)
        advanceUntilIdle()

        assertEquals(
            AiMaterialPurchase.Ready(PRODUCT, AiMaterialPurchaseProblem.REJECTED),
            viewModel.uiState.value.purchase
        )
        verifyPurchaseLogged(
            mapOf(
                AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_REJECTED,
                AiMaterialParams.PARAM_REASON to "owned_by_other"
            )
        )
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun `サーバーが未払いで断ったら、支払いが済んでいないと出す`() = runTest(testDispatcher) {
        stubPurchase(Result.failure(TicketPurchaseRejectedException(TicketPurchaseRejectReason.PENDING)))
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        viewModel.purchase(activity)
        advanceUntilIdle()

        assertEquals(
            AiMaterialPurchase.Ready(PRODUCT, AiMaterialPurchaseProblem.PENDING),
            viewModel.uiState.value.purchase
        )
    }

    @Test
    fun `サーバーが Play に聞けずに断ったら、開き直すと足されると出す`() = runTest(testDispatcher) {
        stubPurchase(Result.failure(TicketPurchaseRejectedException(TicketPurchaseRejectReason.VERIFICATION_FAILED)))
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        viewModel.purchase(activity)
        advanceUntilIdle()

        assertEquals(
            AiMaterialPurchase.Ready(PRODUCT, AiMaterialPurchaseProblem.FAILED),
            viewModel.uiState.value.purchase
        )
    }

    @Test
    fun `設定や実装の誤りで起きる Play のエラーは例外を記録する`() = runTest(testDispatcher) {
        val error = PlayBillingException(responseCode = BillingResponseCode.DEVELOPER_ERROR, debugMessage = "developer error")
        stubPurchase(Result.failure(error))
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        viewModel.purchase(activity)
        advanceUntilIdle()

        assertEquals(
            AiMaterialPurchase.Ready(PRODUCT, AiMaterialPurchaseProblem.PLAY_ERROR),
            viewModel.uiState.value.purchase
        )
        verify(exactly = 1) { crashReportingRepository.recordException(error) }
    }

    @Test
    fun `端末や通信の都合による Play のエラーは失敗と計測し、例外は記録しない`() = runTest(testDispatcher) {
        stubPurchase(Result.failure(PlayBillingException(responseCode = BillingResponseCode.SERVICE_UNAVAILABLE, debugMessage = "service unavailable")))
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        viewModel.purchase(activity)
        advanceUntilIdle()

        assertEquals(
            AiMaterialPurchase.Ready(PRODUCT, AiMaterialPurchaseProblem.PLAY_ERROR),
            viewModel.uiState.value.purchase
        )
        verifyPurchaseLogged(mapOf(AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_FAILED))
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun `理由のない失敗は例外を記録して、開き直すと足されると出す`() = runTest(testDispatcher) {
        val error = IOException("timeout")
        stubPurchase(Result.failure(error))
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)

        viewModel.purchase(activity)
        advanceUntilIdle()

        assertEquals(
            AiMaterialPurchase.Ready(PRODUCT, AiMaterialPurchaseProblem.FAILED),
            viewModel.uiState.value.purchase
        )
        verifyPurchaseLogged(mapOf(AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_FAILED))
        verify(exactly = 1) { crashReportingRepository.recordException(error) }
    }

    @Test
    fun `案内を開いていないときは買わない`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.purchase(activity)
        advanceUntilIdle()

        coVerify(exactly = 0) { ticketPurchaseRepository.purchase(any()) }
    }

    @Test
    fun `画面に戻るたびに送り直し、新しく足したときだけ知らせて成功を計測する`() = runTest(testDispatcher) {
        coEvery { ticketPurchaseRepository.grantUnconsumedPurchases() } returnsMany listOf(
            Result.success(listOf(grantedTickets(alreadyGranted = false))),
            Result.success(emptyList())
        )
        val viewModel = createViewModel()

        viewModel.grantUnconsumedPurchasesOnResume()
        advanceUntilIdle()
        viewModel.grantUnconsumedPurchasesOnResume()
        advanceUntilIdle()

        assertEquals(grantedTickets(alreadyGranted = false), viewModel.uiState.value.addedTickets)
        assertEquals(TICKETS_REMAINING, viewModel.uiState.value.ticketsRemaining)
        coVerify(exactly = 2) { ticketPurchaseRepository.grantUnconsumedPurchases() }
        coVerify(exactly = 1) {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    AnalyticsActions.PURCHASE_AI_MATERIAL,
                    mapOf(AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_SUCCESS)
                )
            )
        }
    }

    @Test
    fun `支払い待ちのあとに戻ってきて足せたら、購入の案内を閉じる`() = runTest(testDispatcher) {
        stubPurchase(Result.success(TicketPurchaseOutcome.Pending))
        val viewModel = rejectedViewModel(AiMaterialRejectReason.NO_CREDITS)
        viewModel.purchase(activity)
        advanceUntilIdle()
        assertEquals(
            AiMaterialPurchase.Ready(PRODUCT, AiMaterialPurchaseProblem.PENDING),
            viewModel.uiState.value.purchase
        )
        coEvery { ticketPurchaseRepository.grantUnconsumedPurchases() } returns
            Result.success(listOf(grantedTickets(alreadyGranted = false)))

        viewModel.grantUnconsumedPurchasesOnResume()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AiMaterialPurchase.Closed, state.purchase)
        assertEquals(grantedTickets(alreadyGranted = false), state.addedTickets)
        assertEquals(TICKETS_REMAINING, state.ticketsRemaining)
    }

    @Test
    fun `足し済みだった購入は残りだけ直して知らせない`() = runTest(testDispatcher) {
        coEvery { ticketPurchaseRepository.grantUnconsumedPurchases() } returns
            Result.success(listOf(grantedTickets(alreadyGranted = true)))
        val viewModel = createViewModel()

        viewModel.grantUnconsumedPurchasesOnResume()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.addedTickets)
        assertEquals(TICKETS_REMAINING, viewModel.uiState.value.ticketsRemaining)
        coVerify(exactly = 0) {
            analyticsRepository.logEvent(match { it.name == AnalyticsActions.PURCHASE_AI_MATERIAL })
        }
    }

    @Test
    fun `画面に戻ったときの送り直しが Play のエラーなら、記録せずに何も出さない`() = runTest(testDispatcher) {
        coEvery { ticketPurchaseRepository.grantUnconsumedPurchases() } returns
            Result.failure(PlayBillingException(responseCode = BillingResponseCode.BILLING_UNAVAILABLE, debugMessage = "billing unavailable"))
        val viewModel = createViewModel()

        viewModel.grantUnconsumedPurchasesOnResume()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.addedTickets)
        assertEquals(0, viewModel.uiState.value.ticketsRemaining)
        verify(exactly = 0) { crashReportingRepository.recordException(any()) }
    }

    @Test
    fun `作れたら残りの回数を持ち、足した知らせは次に作るときに消す`() = runTest(testDispatcher) {
        coEvery { ticketPurchaseRepository.grantUnconsumedPurchases() } returns
            Result.success(listOf(grantedTickets(alreadyGranted = false)))
        stubGenerate(Result.success(image))
        val viewModel = createViewModel()
        viewModel.grantUnconsumedPurchasesOnResume()
        advanceUntilIdle()

        viewModel.updatePrompt("ハート")
        viewModel.generate()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.addedTickets)
        assertEquals(image.ticketsRemaining, viewModel.uiState.value.ticketsRemaining)
    }

    private fun stubPurchase(result: Result<TicketPurchaseOutcome>) {
        coEvery { ticketPurchaseRepository.purchase(any()) } returns result
    }

    private fun verifyPurchaseLogged(params: Map<String, Any>) {
        coVerify {
            analyticsRepository.logEvent(AnalyticsEvent(AnalyticsActions.PURCHASE_AI_MATERIAL, params))
        }
    }

    private fun TestScope.rejectedViewModel(reason: AiMaterialRejectReason): AiMaterialViewModel {
        stubGenerate(Result.failure(AiMaterialRejectedException(reason)))
        return generatedViewModel("ハート")
    }

    private fun TestScope.generatedViewModel(prompt: String): AiMaterialViewModel {
        val viewModel = createViewModel()
        viewModel.updatePrompt(prompt)
        viewModel.generate()
        advanceUntilIdle()
        return viewModel
    }

    private val activity: Activity = mockk()

    private fun grantedTickets(alreadyGranted: Boolean) = GrantedTickets(
        ticketsAdded = 5,
        ticketsRemaining = TICKETS_REMAINING,
        expiresAt = Instant.parse("2027-05-12T00:00:00Z"),
        alreadyGranted = alreadyGranted
    )

    private companion object {
        const val RESULT_URI = "file:///cache/material.png"
        const val MODEL = "gemini-3.1-flash-lite-image"
        const val TICKETS_REMAINING = 7
        val PRODUCT = TicketProduct(formattedPrice = "¥160")
    }
}
