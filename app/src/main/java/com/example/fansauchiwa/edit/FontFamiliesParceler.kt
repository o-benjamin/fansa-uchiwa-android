package com.example.fansauchiwa.edit

import android.os.Parcel
import kotlinx.parcelize.Parceler

object FontFamiliesParceler : Parceler<FontFamilies> {
    override fun create(parcel: Parcel): FontFamilies {
        val ordinal = parcel.readInt()
        return FontFamilies.entries[ordinal]
    }

    override fun FontFamilies.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }
}

