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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * アプリ内メッセージ（Firebase In-App Messaging。#428）。Activity に1つで、[InAppMessageHost] が使う。
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

    private var isHomeShown = false

    /**
     * メッセージの表示とボタンを受け取る。呼び出し元を止めるまで返らない。Activity が前にいる間だけ呼ぶ（[InAppMessageHost]）。
     * ほかの observe~ と違い viewModelScope で購読しないのは、Firebase In-App Messaging が Activity の一時停止のたびに
     * アプリのリスナーをすべて外し、前に戻るたびに付け直す必要があるため
     */
    suspend fun observeMessages() {
        coroutineScope {
            inAppMessagingRepository.getImpressionStream()
                // メッセージを出した起動で、続けて全画面の広告まで出さない（更新の案内と同じ）
                .onEach { adMobRepository.skipNextInterstitialAd() }
                .launchIn(this)
            inAppMessagingRepository.getClickedTargetStream()
                .onEach(::onTargetClicked)
                .launchIn(this)
        }
    }

    /**
     * 今の画面がホームかどうかが変わったとき。編集中に割り込まないよう、ホーム以外ではメッセージを止める
     */
    fun onHomeShownChanged(isHomeShown: Boolean) {
        this.isHomeShown = isHomeShown
        inAppMessagingRepository.setMessagesSuppressed(!isHomeShown)
        // ホームを離れたら、まだ開いていない行き先は捨てる。あとでホームに戻ったときに急に開かないように
        if (!isHomeShown) _uiState.update { it.copy(pendingTarget = null) }
    }

    /**
     * ホームが [InAppMessageUiState.pendingTarget] を受け取ったとき（開けずにホームのままだったときも）
     */
    fun onPendingTargetHandled() {
        _uiState.update { it.copy(pendingTarget = null) }
    }

    private fun onTargetClicked(target: InAppMessageTarget) {
        when (target) {
            InAppMessageTarget.STORE -> appUpdateRepository.openStorePage()
            // ホーム以外で押された（止める前に出てしまった）ときは開かない。ホームが受け取れず、戻ったときに急に開くため
            InAppMessageTarget.DAILY_PROMPT, InAppMessageTarget.AI_MATERIAL ->
                if (isHomeShown) _uiState.update { it.copy(pendingTarget = target) }
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
