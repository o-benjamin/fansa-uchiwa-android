package com.example.fansauchiwa.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeUiState(
    val masterpiecePathList: List<String> = emptyList()
) : Parcelable

