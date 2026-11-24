package com.fansauchiwa.inappmessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.analytics.InAppMessageAnalyticsParams
import com.fansauchiwa.data.InAppMessageTarget
import com.fansauchiwa.data.repository.AdMobRepository
import com.fansauchiwa.data.repository.AppUpdateRepository
import com.fansauchiwa.data.repository.InAppMessagingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * アプリ内メッセージ（Firebase In-App Messaging。#428）。ホームに置く [InAppMessageHost] が使う。
 * メッセージの表示とボタンの URL を開く動きは Firebase In-App Messaging が行い、ここはそのあとの始末をする
 */
@HiltViewModel
class InAppMessageViewModel @Inject constructor(
    private val inAppMessagingRepository: InAppMessagingRepository,
    private val appUpdateRepository: AppUpdateRepository,
    private val adMobRepository: AdMobRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InAppMessageUiState())
    val uiState: StateFlow<InAppMessageUiState> = _uiState.asStateFlow()

    init {
        observeImpression()
        observeClickedTarget()
    }

    /**
     * ホームが前に出たとき。メッセージを出せるようにする
     */
    fun onHomeResumed() {
        inAppMessagingRepository.setMessagesSuppressed(false)
    }

    /**
     * ホームが隠れたとき（編集画面などへ移った、アプリが裏に回った）。編集中に割り込まないよう、メッセージを止める
     */
    fun onHomePaused() {
        inAppMessagingRepository.setMessagesSuppressed(true)
    }

    /**
     * [InAppMessageUiState.pendingDestination] の画面を開いたとき（開けずにホームのままだったときも）
     */
    fun onDestinationOpened() {
        _uiState.update { it.copy(pendingDestination = null) }
    }

    private fun observeImpression() {
        inAppMessagingRepository.getImpressionStream()
            // メッセージを出した起動で、続けて全画面の広告まで出さない（更新の案内と同じ）
            .onEach { adMobRepository.skipNextInterstitialAd() }
            .launchIn(viewModelScope)
    }

    private fun observeClickedTarget() {
        inAppMessagingRepository.getClickedTargetStream()
            .onEach(::onTargetClicked)
            .launchIn(viewModelScope)
    }

    private fun onTargetClicked(target: InAppMessageTarget) {
        when (target) {
            InAppMessageTarget.STORE -> appUpdateRepository.openStorePage()
            InAppMessageTarget.DAILY_PROMPT, InAppMessageTarget.AI_MATERIAL ->
                _uiState.update { it.copy(pendingDestination = target) }
            // Web ページは Firebase In-App Messaging が開き、ホームはメッセージが閉じればそのまま
            InAppMessageTarget.WEB, InAppMessageTarget.HOME, InAppMessageTarget.OTHER -> Unit
        }
        viewModelScope.launch {
            analyticsRepository.logEvent(
                AnalyticsEvent(
                    name = AnalyticsActions.TAP_IN_APP_MESSAGE,
                    params = InAppMessageAnalyticsParams.params(target)
                )
            )
        }
    }
}
