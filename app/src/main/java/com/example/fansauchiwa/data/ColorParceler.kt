package com.example.fansauchiwa.data

import android.os.Parcel
import androidx.compose.ui.graphics.Color
import kotlinx.parcelize.Parceler

object ColorParceler : Parceler<Color> {
    override fun create(parcel: Parcel): Color {
        val value = parcel.readLong()
        return Color(value.toULong())
    }

    override fun Color.write(parcel: Parcel, flags: Int) {
        parcel.writeLong(value.toLong())
    }
}

