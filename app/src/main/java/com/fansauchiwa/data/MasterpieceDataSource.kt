package com.fansauchiwa.data

import android.graphics.Bitmap

interface MasterpieceDataSource {
    fun saveBitmap(bitmap: Bitmap, id: String): String?
    fun loadAllMasterpieces(): List<String>
    fun deleteMasterpiece(filePath: String): Boolean
    fun duplicateMasterpiece(sourceFilePath: String, newId: String): String?
}
