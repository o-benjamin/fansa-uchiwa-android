package com.fansauchiwa.edit.aimaterial

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.analytics.AiMaterialParams
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.analytics.AnalyticsScreens
import com.fansauchiwa.data.AiMaterialRejectReason
import com.fansauchiwa.data.AiMaterialRejectedException
import com.fansauchiwa.data.AiMaterialReportLimitException
import com.fansauchiwa.data.AiMaterialReportReason
import com.fansauchiwa.data.PlayBillingException
import com.fansauchiwa.data.TicketPurchaseOutcome
import com.fansauchiwa.data.TicketPurchaseRejectReason
import com.fansauchiwa.data.TicketPurchaseRejectedException
import com.fansauchiwa.data.repository.AiMaterialRepository
import com.fansauchiwa.data.repository.CrashReportingRepository
import com.fansauchiwa.data.repository.TicketPurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class AiMaterialViewModel @Inject constructor(
    private val aiMaterialRepository: AiMaterialRepository,
    private val ticketPurchaseRepository: TicketPurchaseRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val crashReportingRepository: CrashReportingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiMaterialUiState())
    val uiState: StateFlow<AiMaterialUiState> = _uiState.asStateFlow()

    private val _confirmEvent = MutableSharedFlow<String>()
    val confirmEvent: SharedFlow<String> = _confirmEvent.asSharedFlow()

    // 二度押しで同じ素材を2回置かない・2回数えない
    private var isConfirmed = false

    // 画面を開いたときの送り直しは1回だけ（画面の回転などで何度も Play に問い合わせない）
    private var hasGrantedOnOpen = false

    fun logScreenView() {
        viewModelScope.launch {
            analyticsRepository.logScreenView(AnalyticsScreens.AI_MATERIAL_SCREEN)
        }
    }

    /**
     * 買ったのに回数を足せていない購入（支払いがあとで済んだコンビニ払い、足す前に通信が切れたなど）を足す。
     * 購入の案内で「画面を開き直すと足す」と出しているので、画面を開いたときに呼ぶ
     */
    fun grantUnconsumedPurchasesOnOpen() {
        if (hasGrantedOnOpen) return
        hasGrantedOnOpen = true
        viewModelScope.launch { grantUnconsumedPurchases() }
    }

    fun updatePrompt(prompt: String) = updateInput { it.copy(prompt = prompt) }

    fun updateSourceImage(uri: Uri?) = updateInput { it.copy(sourceImageUri = uri) }

    fun generate() {
        val state = _uiState.value
        if (!state.canGenerate) return
        _uiState.value = state.copy(generation = AiMaterialGeneration.Generating, addedTickets = null)
        viewModelScope.launch {
            val generation = aiMaterialRepository
                .generate(state.prompt.trim(), state.sourceImageUri)
                .fold(
                    onSuccess = { AiMaterialGeneration.Succeeded(it) },
                    onFailure = { error ->
                        if (error is AiMaterialRejectedException) {
                            AiMaterialGeneration.Rejected(error.reason)
                        } else {
                            crashReportingRepository.recordException(error)
                            AiMaterialGeneration.Failed
                        }
                    }
                )
            _uiState.update { it.copy(generation = generation, ticketsRemaining = generation.ticketsRemaining(it)) }
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    name = AnalyticsActions.GENERATE_AI_MATERIAL,
                    params = generation.toAnalyticsParams()
                )
            )
            if (generation is AiMaterialGeneration.Rejected && generation.offersPurchase) {
                openPurchase(generation.reason)
            }
        }
    }

    /**
     * 回数券を買う。買えたら案内を閉じて残りを出し、入力に戻る（作ると回数を使うので、自動では作り直さない）。
     * [activity] は Play の購入の画面を出すのに使う
     */
    fun purchase(activity: Activity) {
        val ready = _uiState.value.purchase as? AiMaterialPurchase.Ready ?: return
        _uiState.update { it.copy(purchase = AiMaterialPurchase.Purchasing(ready.product)) }
        viewModelScope.launch {
            val result = ticketPurchaseRepository.purchase(activity)
            val granted = (result.getOrNull() as? TicketPurchaseOutcome.Granted)?.tickets
            val problem = result.fold(
                onSuccess = { if (it == TicketPurchaseOutcome.Pending) AiMaterialPurchaseProblem.PENDING else null },
                onFailure = { it.toPurchaseProblem() }
            )
            _uiState.update { state ->
                if (granted != null) {
                    state.copy(
                        purchase = AiMaterialPurchase.Closed,
                        ticketsRemaining = granted.ticketsRemaining,
                        addedTickets = granted
                    )
                } else {
                    state.copy(purchase = AiMaterialPurchase.Ready(ready.product, problem))
                }
            }
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    name = AnalyticsActions.PURCHASE_AI_MATERIAL,
                    params = result.toPurchaseAnalyticsParams()
                )
            )
        }
    }

    /**
     * 作った素材を捨てて入力に戻る。作り直すと回数が減るので、ここでは作り直さない
     * （もう一度「作る」を押したときだけ [generate] が走る）
     */
    fun discardResult() {
        _uiState.update { state ->
            if (state.generation is AiMaterialGeneration.Succeeded) {
                state.copy(
                    generation = AiMaterialGeneration.Idle,
                    reportStatus = AiMaterialReportStatus.NOT_SENT
                )
            } else {
                state
            }
        }
    }

    fun confirm() {
        val image = (_uiState.value.generation as? AiMaterialGeneration.Succeeded)?.image ?: return
        if (isConfirmed) return
        isConfirmed = true
        viewModelScope.launch {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    name = AnalyticsActions.SAVE_AI_MATERIAL,
                    params = mapOf(AiMaterialParams.PARAM_PAYMENT to image.payment.serverValue)
                )
            )
            _confirmEvent.emit(image.uri.toString())
        }
    }

    /**
     * 見ている素材を報告する。頼んだ文章は結果を見ている間は変わらないので、入力欄の文章をそのまま送る
     */
    fun report(reason: AiMaterialReportReason) {
        val state = _uiState.value
        if (!state.canReport) return
        val generation = state.generation as? AiMaterialGeneration.Succeeded ?: return
        _uiState.value = state.copy(reportStatus = AiMaterialReportStatus.SENDING)
        viewModelScope.launch {
            // 送っている間に「うちわに置く」や戻るで画面を閉じても、報告と計測は最後まで送る
            val status = withContext(NonCancellable) {
                sendReport(state.prompt.trim(), reason, generation.image.model)
            }
            // 送っている間に捨てられたら、次の素材に結果を持ち越さない。
            // Succeeded は data class で、中身が同じ結果は == では見分けられないので、参照で比べる
            _uiState.update {
                if (it.generation === generation) it.copy(reportStatus = status) else it
            }
        }
    }

    private fun openPurchase(reason: AiMaterialRejectReason) {
        if (_uiState.value.purchase != AiMaterialPurchase.Closed) return
        _uiState.update { it.copy(purchase = AiMaterialPurchase.Preparing) }
        viewModelScope.launch {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    name = AnalyticsActions.VIEW_AI_MATERIAL_PAYWALL,
                    params = mapOf(AiMaterialParams.PARAM_REASON to reason.serverValue)
                )
            )
            // 支払いが済んでいて足せていない購入があれば、もう一度買わせずにそれを足す
            if (grantUnconsumedPurchases()) {
                _uiState.update { it.copy(purchase = AiMaterialPurchase.Closed) }
                return@launch
            }
            val purchase = ticketPurchaseRepository.getTicketProduct().fold(
                onSuccess = { AiMaterialPurchase.Ready(it, problem = null) },
                onFailure = { error ->
                    // アイテムが見つからないのは Play Console の設定の誤りなので、これも記録する
                    recordUnlessExpected(error)
                    AiMaterialPurchase.Unavailable
                }
            )
            _uiState.update { it.copy(purchase = purchase) }
        }
    }

    /**
     * 足せていない購入をサーバーに送り直す。今回新しく足した分があれば知らせて true を返す。
     * 足し済みだった分（前の呼び出しで足したが応答が届かなかったなど）は、残りだけ直して知らせない
     */
    private suspend fun grantUnconsumedPurchases(): Boolean {
        val granted = ticketPurchaseRepository.grantUnconsumedPurchases()
            .onFailure { recordUnlessExpected(it) }
            .getOrNull()
        val latest = granted?.lastOrNull() ?: return false
        // 消費型は消費するまで1つしか持てないので、足せていない購入はふつう1件
        val newlyGranted = granted.lastOrNull { !it.alreadyGranted }
        _uiState.update {
            it.copy(
                ticketsRemaining = latest.ticketsRemaining,
                addedTickets = newlyGranted ?: it.addedTickets
            )
        }
        return newlyGranted != null
    }

    // 待っている間と結果を見ている間は、入力を変えない。断られたあとに直したら、断りの表示を消す
    private fun updateInput(change: (AiMaterialUiState) -> AiMaterialUiState) {
        _uiState.update { state ->
            when (state.generation) {
                is AiMaterialGeneration.Generating, is AiMaterialGeneration.Succeeded -> state
                else -> change(state).copy(generation = AiMaterialGeneration.Idle)
            }
        }
    }

    // 上限に当たったのは押し直しても送れないだけで不具合ではないので、Crashlytics には残さない
    private suspend fun sendReport(
        prompt: String,
        reason: AiMaterialReportReason,
        model: String
    ): AiMaterialReportStatus {
        val status = when (val error = aiMaterialRepository.report(prompt, reason, model).exceptionOrNull()) {
            null -> AiMaterialReportStatus.SENT
            is AiMaterialReportLimitException -> AiMaterialReportStatus.LIMIT_REACHED
            else -> {
                crashReportingRepository.recordException(error)
                AiMaterialReportStatus.FAILED
            }
        }
        analyticsRepository.logEvent(
            AnalyticsEvent(
                name = AnalyticsActions.REPORT_AI_MATERIAL,
                params = mapOf(
                    AiMaterialParams.PARAM_REASON to reason.serverValue,
                    AiMaterialParams.PARAM_RESULT to status.toAnalyticsResult()
                )
            )
        )
        return status
    }

    // サーバーの断りと Play のエラーは利用者の状況（未払い・Play に入っていないなど）で起き、不具合ではないので記録しない
    private fun recordUnlessExpected(error: Throwable) {
        if (error is TicketPurchaseRejectedException || error is PlayBillingException) return
        crashReportingRepository.recordException(error)
    }

    private fun Throwable.toPurchaseProblem(): AiMaterialPurchaseProblem {
        recordUnlessExpected(this)
        return when (this) {
            is TicketPurchaseRejectedException ->
                if (reason == TicketPurchaseRejectReason.PENDING) {
                    AiMaterialPurchaseProblem.PENDING
                } else {
                    AiMaterialPurchaseProblem.REJECTED
                }
            is PlayBillingException -> AiMaterialPurchaseProblem.PLAY_ERROR
            else -> AiMaterialPurchaseProblem.FAILED
        }
    }

    // 回数がないと断られたら残りは 0。それ以外の断りと失敗では、分かっている残りを変えない
    private fun AiMaterialGeneration.ticketsRemaining(state: AiMaterialUiState): Int = when {
        this is AiMaterialGeneration.Succeeded -> image.ticketsRemaining
        this is AiMaterialGeneration.Rejected && reason == AiMaterialRejectReason.NO_CREDITS -> 0
        else -> state.ticketsRemaining
    }

    private fun Result<TicketPurchaseOutcome>.toPurchaseAnalyticsParams(): Map<String, Any> {
        val error = exceptionOrNull()
        val result = when {
            error is TicketPurchaseRejectedException -> AiMaterialParams.RESULT_REJECTED
            error != null -> AiMaterialParams.RESULT_FAILED
            else -> when (getOrThrow()) {
                is TicketPurchaseOutcome.Granted -> AiMaterialParams.RESULT_SUCCESS
                TicketPurchaseOutcome.Pending -> AiMaterialParams.RESULT_PENDING
                TicketPurchaseOutcome.Cancelled -> AiMaterialParams.RESULT_CANCELLED
            }
        }
        return buildMap {
            put(AiMaterialParams.PARAM_RESULT, result)
            if (error is TicketPurchaseRejectedException) put(AiMaterialParams.PARAM_REASON, error.reason.serverValue)
        }
    }

    private fun AiMaterialReportStatus.toAnalyticsResult(): String = when (this) {
        AiMaterialReportStatus.SENT -> AiMaterialParams.RESULT_SUCCESS
        AiMaterialReportStatus.LIMIT_REACHED -> AiMaterialParams.RESULT_REJECTED
        // NOT_SENT と SENDING は report() の結果にならない
        AiMaterialReportStatus.FAILED,
        AiMaterialReportStatus.NOT_SENT,
        AiMaterialReportStatus.SENDING -> AiMaterialParams.RESULT_FAILED
    }

    private fun AiMaterialGeneration.toAnalyticsParams(): Map<String, Any> = when (this) {
        is AiMaterialGeneration.Rejected -> mapOf(
            AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_REJECTED,
            AiMaterialParams.PARAM_REASON to reason.serverValue
        )
        is AiMaterialGeneration.Succeeded -> mapOf(
            AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_SUCCESS,
            AiMaterialParams.PARAM_BACKGROUND_REMOVED to image.backgroundRemoved.toString()
        )
        // Idle と Generating は generate() の結果にならない
        AiMaterialGeneration.Failed,
        AiMaterialGeneration.Idle,
        AiMaterialGeneration.Generating -> mapOf(
            AiMaterialParams.PARAM_RESULT to AiMaterialParams.RESULT_FAILED
        )
    }
}
