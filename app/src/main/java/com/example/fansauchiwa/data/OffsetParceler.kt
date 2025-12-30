package com.example.fansauchiwa.data

import android.os.Parcel
import androidx.compose.ui.geometry.Offset
import kotlinx.parcelize.Parceler

object OffsetParceler : Parceler<Offset> {
    override fun create(parcel: Parcel): Offset {
        val x = parcel.readFloat()
        val y = parcel.readFloat()
        return Offset(x, y)
    }

    override fun Offset.write(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
    }
}

