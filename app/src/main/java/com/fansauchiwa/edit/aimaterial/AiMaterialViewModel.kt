package com.fansauchiwa.edit.aimaterial

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.analytics.AiMaterialParams
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.analytics.AnalyticsScreens
import com.fansauchiwa.data.AiMaterialRejectedException
import com.fansauchiwa.data.AiMaterialReportLimitException
import com.fansauchiwa.data.AiMaterialReportReason
import com.fansauchiwa.data.repository.AiMaterialRepository
import com.fansauchiwa.data.repository.CrashReportingRepository
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
    private val analyticsRepository: AnalyticsRepository,
    private val crashReportingRepository: CrashReportingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiMaterialUiState())
    val uiState: StateFlow<AiMaterialUiState> = _uiState.asStateFlow()

    private val _confirmEvent = MutableSharedFlow<String>()
    val confirmEvent: SharedFlow<String> = _confirmEvent.asSharedFlow()

    // 二度押しで同じ素材を2回置かない・2回数えない
    private var isConfirmed = false

    fun logScreenView() {
        viewModelScope.launch {
            analyticsRepository.logScreenView(AnalyticsScreens.AI_MATERIAL_SCREEN)
        }
    }

    fun updatePrompt(prompt: String) = updateInput { it.copy(prompt = prompt) }

    fun updateSourceImage(uri: Uri?) = updateInput { it.copy(sourceImageUri = uri) }

    fun generate() {
        val state = _uiState.value
        if (!state.canGenerate) return
        _uiState.value = state.copy(generation = AiMaterialGeneration.Generating)
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
            _uiState.update { it.copy(generation = generation) }
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    name = AnalyticsActions.GENERATE_AI_MATERIAL,
                    params = generation.toAnalyticsParams()
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
