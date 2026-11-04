package com.fansauchiwa.home

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.DailyPrompt
import com.fansauchiwa.data.DecorationColors
import com.fansauchiwa.data.Template
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

enum class HomeTab {
    HOME,
    MY_DESIGN
}

@Parcelize
data class HomeUiState(
    val selectedTab: HomeTab = HomeTab.HOME,
    val selectedMainColor: DecorationColors = DecorationColors.PINK,
    val isNameDialogShown: Boolean = false,
    val selectedTargetTemplate: @RawValue Template? = null,
    val masterpiecePathList: List<String> = emptyList(),
    val isSelectionMode: Boolean = false,
    val selectedPaths: List<String> = emptyList(),
    val templates: @RawValue List<Template> = emptyList(),
    val showApologyDialog: Boolean = false,
    // 今日のお題。null なら出さない。カードに出すのは #360
    val dailyPrompt: DailyPrompt? = null
) : Parcelable
