package com.fansauchiwa.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansauchiwa.EditScreenInputArg
import com.fansauchiwa.analytics.AnalyticsActions
import com.fansauchiwa.analytics.AnalyticsEvent
import com.fansauchiwa.analytics.AnalyticsRepository
import com.fansauchiwa.analytics.AnalyticsScreens
import com.fansauchiwa.analytics.DailyPromptAnalyticsParams
import com.fansauchiwa.data.DailyPrompt
import com.fansauchiwa.data.DecorationColors
import com.fansauchiwa.data.Template
import com.fansauchiwa.data.TodayProvider
import com.fansauchiwa.data.Uchiwa
import com.fansauchiwa.data.UuidProvider
import com.fansauchiwa.data.extractUchiwaIdFromImagePath
import com.fansauchiwa.data.repository.DailyPromptRepository
import com.fansauchiwa.data.repository.DailyPromptUchiwaRepository
import com.fansauchiwa.data.repository.LocalDatabaseRepository
import com.fansauchiwa.data.repository.MasterpieceRepository
import com.fansauchiwa.data.repository.SettingsRepository
import com.fansauchiwa.data.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val masterpieceRepository: MasterpieceRepository,
    private val localDatabaseRepository: LocalDatabaseRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val templateRepository: TemplateRepository,
    private val settingsRepository: SettingsRepository,
    private val dailyPromptRepository: DailyPromptRepository,
    private val dailyPromptUchiwaRepository: DailyPromptUchiwaRepository,
    private val todayProvider: TodayProvider,
    private val uuidProvider: UuidProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // view_daily_prompt を送ったお題の id。カードがスクロールで隠れて戻るたび・タブを切り替えるたびに送らないよう、
    // ホームが開いている間は1つのお題につき1回にする
    private val loggedDailyPromptViewIds = mutableSetOf<String>()

    init {
        observeDailyPrompt()
    }

    fun logScreenView() {
        viewModelScope.launch {
            analyticsRepository.logScreenView(AnalyticsScreens.HOME_SCREEN)
        }
    }

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        viewModelScope.launch {
            analyticsRepository.logEvent(AnalyticsEvent(name = eventName, params = params))
        }
    }

    fun logNewCreateTap() {
        logEvent(AnalyticsActions.TAP_HOME_NEW_CREATE)
    }

    fun logItemEditTap() {
        logEvent(AnalyticsActions.TAP_HOME_ITEM_EDIT)
    }

    fun logTemplateTap(templateId: String) {
        logEvent(AnalyticsActions.TAP_HOME_TEMPLATE, mapOf("template_id" to templateId))
    }

    fun onTabSelected(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onMainColorSelected(color: DecorationColors) {
        _uiState.update { it.copy(selectedMainColor = color) }
    }

    fun showNameDialog(targetTemplate: Template?) {
        _uiState.update {
            it.copy(
                isNameDialogShown = true,
                selectedTargetTemplate = targetTemplate
            )
        }
    }

    fun dismissNameDialog() {
        _uiState.update {
            it.copy(
                isNameDialogShown = false,
                selectedTargetTemplate = null
            )
        }
    }

    fun onNameConfirmed() {
        _uiState.update {
            it.copy(isNameDialogShown = false)
        }
    }

    fun loadAllMasterpieces() {
        viewModelScope.launch {
            val pathList = masterpieceRepository.loadAllMasterpieces()
            _uiState.update { it.copy(masterpiecePathList = pathList) }
        }
    }

    fun loadTemplates() {
        viewModelScope.launch {
            val templates = templateRepository.getTemplates()
            _uiState.update { currentState ->
                currentState.copy(
                    templates = templates,
                    selectedMainColor = currentState.selectedMainColor
                )
            }
        }
    }

    fun observeApologyDialogState() {
        viewModelScope.launch {
            settingsRepository.getHasSeenApologyDialogStream().collect { hasSeen ->
                _uiState.update { it.copy(showApologyDialog = !hasSeen) }
            }
        }
    }

    fun fetchApologyDialogState() {
        viewModelScope.launch {
            settingsRepository.fetchHasSeenApologyDialog()
        }
    }

    fun dismissApologyDialog() {
        viewModelScope.launch {
            settingsRepository.setHasSeenApologyDialog(true)
            _uiState.update { it.copy(showApologyDialog = false) }
        }
    }

    private fun observeDailyPrompt() {
        combine(
            dailyPromptRepository.getDailyPromptStream(),
            settingsRepository.getDailyPromptDismissedDateStream()
        ) { prompt, dismissedDate ->
            // カードを閉じた日はもう出さず、翌日また出す
            prompt?.takeUnless { dismissedDate == todayProvider.today() }
        }
            .onEach { prompt -> _uiState.update { it.copy(dailyPrompt = prompt) } }
            .launchIn(viewModelScope)
    }

    /**
     * ホームが前に出るたび（ON_START）に呼ぶ。日付が変わっていればお題も変わり、前の日に閉じたカードもまた出る。
     * Remote Config の取得は最小間隔（1時間）で間引かれる
     */
    fun fetchDailyPrompt() {
        viewModelScope.launch {
            settingsRepository.fetchDailyPromptDismissedDate()
        }
        // Remote Config の取得を待つ間も、閉じた日付は先に読めるよう分けて起動する
        viewModelScope.launch {
            dailyPromptRepository.fetchDailyPrompt()
        }
    }

    /** お題のカードが画面に出たときに呼ぶ */
    fun onDailyPromptShown(prompt: DailyPrompt) {
        if (!loggedDailyPromptViewIds.add(prompt.id)) return
        logEvent(
            AnalyticsActions.VIEW_DAILY_PROMPT,
            mapOf(DailyPromptAnalyticsParams.PARAM_PROMPT_ID to prompt.id)
        )
    }

    /**
     * 「このお題で作る」を押したときに呼び、白紙のうちわにお題の文字を置いて編集画面を開くための引数を返す。
     * 白紙からの新規作成と保存の率を比べるため、tap_home_new_create は送らない
     */
    fun onDailyPromptCreate(prompt: DailyPrompt): EditScreenInputArg {
        logEvent(
            AnalyticsActions.TAP_DAILY_PROMPT,
            mapOf(DailyPromptAnalyticsParams.PARAM_PROMPT_ID to prompt.id)
        )
        val uchiwaId = uuidProvider.generate()
        dailyPromptUchiwaRepository.markStartedFromPrompt(uchiwaId)
        return EditScreenInputArg(uchiwaId = uchiwaId, dailyPromptText = prompt.text)
    }

    /** お題のカードの×。今日はもう出さない */
    fun dismissDailyPrompt() {
        viewModelScope.launch {
            settingsRepository.setDailyPromptDismissedDate(todayProvider.today())
        }
    }

    fun extractUchiwaId(path: String): String {
        return extractUchiwaIdFromImagePath(path)
    }

    fun enterSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = true, selectedPaths = emptyList()) }
    }

    fun exitSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = false, selectedPaths = emptyList()) }
    }

    fun togglePathSelection(path: String) {
        _uiState.update { currentState ->
            val currentSelected = currentState.selectedPaths
            val newSelected = if (path in currentSelected) {
                currentSelected - path
            } else {
                currentSelected + path
            }
            currentState.copy(selectedPaths = newSelected)
        }
    }

    fun deleteSelectedMasterpieces() {
        viewModelScope.launch {
            logEvent(AnalyticsActions.TAP_HOME_ITEM_DELETE)
            val selectedPaths = _uiState.value.selectedPaths
            selectedPaths.forEach { path ->
                val uchiwaId = extractUchiwaId(path)
                // ファイル削除
                masterpieceRepository.deleteMasterpiece(path)
                // データベースのカラム削除
                localDatabaseRepository.deleteUchiwa(uchiwaId)
            }
            exitSelectionMode()
            loadAllMasterpieces()
        }
    }

    fun duplicateSelectedMasterpieces() {
        viewModelScope.launch {
            logEvent(AnalyticsActions.TAP_HOME_ITEM_DUPLICATE)
            val selectedPaths = _uiState.value.selectedPaths
            selectedPaths.forEach { path ->
                val oldId = extractUchiwaId(path)
                val newId = uuidProvider.generate()
                // ファイル複製
                masterpieceRepository.duplicateMasterpiece(path, newId)
                    ?: return@forEach
                // データベースの複製
                val savedUchiwa = localDatabaseRepository.getUchiwa(oldId)
                    ?: return@forEach
                localDatabaseRepository.saveUchiwa(
                    Uchiwa(
                        id = newId,
                        decorations = savedUchiwa.decorations,
                        uchiwaColor = savedUchiwa.uchiwaColor,
                        backgroundColor = savedUchiwa.backgroundColor
                    )
                )
            }
            exitSelectionMode()
            loadAllMasterpieces()
        }
    }
}
