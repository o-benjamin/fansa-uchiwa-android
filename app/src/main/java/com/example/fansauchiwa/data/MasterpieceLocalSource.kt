package com.example.fansauchiwa.data

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class MasterpieceLocalSource @Inject constructor(
    @ApplicationContext private val context: Context
) : MasterpieceDataSource {
    override fun saveBitmap(bitmap: Bitmap, id: String): String? {
        val directory = ContextWrapper(context).getDir(
            "masterpiece",
            Context.MODE_PRIVATE
        )
        val file = File(directory, "$id.png")

        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    override fun loadAllMasterpieces(): List<String> {
        val directory = ContextWrapper(context).getDir(
            "masterpiece",
            Context.MODE_PRIVATE
        )
        return try {
            directory.listFiles()
                ?.filter { it.extension == "png" }
                ?.sortedByDescending { it.lastModified() }
                ?.map { it.absolutePath }
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun deleteMasterpiece(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            file.delete()
        } catch (_: Exception) {
            false
        }
    }
}
