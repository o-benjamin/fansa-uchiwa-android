package com.example.fansauchiwa.data

import android.graphics.Bitmap
import javax.inject.Inject

interface MasterpieceRepository {
    fun saveMasterpieceBitmap(bitmap: Bitmap): String?
}

class MasterpieceRepositoryImpl @Inject constructor(
    private val masterpieceDataSource: MasterpieceDataSource
) : MasterpieceRepository {
    override fun saveMasterpieceBitmap(bitmap: Bitmap): String? {
        val id = "${System.currentTimeMillis()}"
        return masterpieceDataSource.saveBitmap(bitmap, id)
    }
}

