package com.example.fansauchiwa.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageReference(
    val id: String,
    val path: String
) : Parcelable
