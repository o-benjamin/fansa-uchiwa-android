package com.fansauchiwa.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.data.repository.SettingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsUseCases: SettingsUseCases,
    private val settingsErrorMessageMapper: SettingsErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        fetchSettings()
    }

    private fun fetchSettings() {
        viewModelScope.launch {
            try {
                settingsUseCases.fetchHapticFeedbackEnabled()
            } catch (exception: Exception) {
                showError(exception)
            }
        }
    }

    private fun observeSettings() {
        settingsUseCases.observeHapticFeedbackEnabledStream()
            .onEach(::showHapticFeedbackEnabled)
            .catch { throwable -> showError(throwable) }
            .launchIn(viewModelScope)
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            settingsUseCases.updateHapticFeedbackEnabled(enabled)
        }
    }

    private fun showHapticFeedbackEnabled(enabled: Boolean) {
        _uiState.value = SettingsUiState.Success(isHapticFeedbackEnabled = enabled)
    }

    private fun showError(throwable: Throwable) {
        _uiState.value = SettingsUiState.Error(
            message = settingsErrorMessageMapper.map(throwable)
        )
    }
}

@ViewModelScoped
class SettingsUseCases @Inject constructor(
    private val observeHapticFeedbackEnabledUseCase: ObserveHapticFeedbackEnabledUseCase,
    private val fetchHapticFeedbackEnabledUseCase: FetchHapticFeedbackEnabledUseCase,
    private val updateHapticFeedbackEnabledUseCase: UpdateHapticFeedbackEnabledUseCase
) {

    fun observeHapticFeedbackEnabledStream(): Flow<Boolean> =
        observeHapticFeedbackEnabledUseCase()

    suspend fun fetchHapticFeedbackEnabled() {
        fetchHapticFeedbackEnabledUseCase()
    }

    suspend fun updateHapticFeedbackEnabled(enabled: Boolean) {
        updateHapticFeedbackEnabledUseCase(enabled)
    }
}

class ObserveHapticFeedbackEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(): Flow<Boolean> =
        settingsRepository.getHapticFeedbackEnabledStream().distinctUntilChanged()
}

class FetchHapticFeedbackEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke() {
        settingsRepository.fetchHapticFeedbackEnabled()
    }
}

class UpdateHapticFeedbackEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.setHapticFeedbackEnabled(enabled)
    }
}

class SettingsErrorMessageMapper @Inject constructor() {

    fun map(throwable: Throwable): String {
        return throwable.message ?: UNKNOWN_ERROR_MESSAGE
    }

    private companion object {
        private const val UNKNOWN_ERROR_MESSAGE = "Unknown error"
    }
}
