package com.example.fansauchiwa.data

import android.graphics.Bitmap

interface MasterpieceDataSource {
    fun saveBitmap(bitmap: Bitmap, id: String): String?
}

