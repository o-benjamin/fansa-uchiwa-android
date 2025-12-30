package com.example.fansauchiwa.data

import android.graphics.Bitmap

interface MasterpieceDataSource {
    fun saveBitmap(bitmap: Bitmap, id: String): String?
    fun loadAllMasterpieces(): List<String>
    fun deleteMasterpiece(filePath: String): Boolean
}
