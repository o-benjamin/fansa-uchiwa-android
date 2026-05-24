package com.fansauchiwa.home

import android.os.Parcelable
import com.fansauchiwa.data.DecorationColors
import com.fansauchiwa.data.Template
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

enum class HomeTab {
    CREATE,
    ALBUM
}

@Parcelize
data class HomeUiState(
    val selectedTab: HomeTab = HomeTab.CREATE,
    val selectedDefaultColor: DecorationColors? = null,
    val isNameDialogShown: Boolean = false,
    val selectedTargetTemplate: @RawValue Template? = null,
    val masterpiecePathList: List<String> = emptyList(),
    val isSelectionMode: Boolean = false,
    val selectedPaths: List<String> = emptyList(),
    val templates: @RawValue List<Template> = emptyList(),
    val showApologyDialog: Boolean = false
) : Parcelable
