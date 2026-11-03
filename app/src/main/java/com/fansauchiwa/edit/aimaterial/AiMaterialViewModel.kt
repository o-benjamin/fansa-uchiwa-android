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
import com.fansauchiwa.data.repository.AiMaterialRepository
import com.fansauchiwa.data.repository.CrashReportingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
                state.copy(generation = AiMaterialGeneration.Idle)
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

    // 待っている間と結果を見ている間は、入力を変えない。断られたあとに直したら、断りの表示を消す
    private fun updateInput(change: (AiMaterialUiState) -> AiMaterialUiState) {
        _uiState.update { state ->
            when (state.generation) {
                is AiMaterialGeneration.Generating, is AiMaterialGeneration.Succeeded -> state
                else -> change(state).copy(generation = AiMaterialGeneration.Idle)
            }
        }
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
