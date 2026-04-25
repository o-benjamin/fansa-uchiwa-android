package com.fansauchiwa.data

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class MasterpieceLocalSource @Inject constructor(
    @ApplicationContext private val context: Context
) : MasterpieceDataSource {
    private val storage = MasterpieceFileStorage(context)

    override fun saveBitmap(bitmap: Bitmap, id: String): String? = storage.saveBitmap(bitmap, id)

    override fun loadAllMasterpieces(): List<String> = storage.loadAll()

    override fun deleteMasterpiece(filePath: String): Boolean = storage.delete(filePath)

    override fun duplicateMasterpiece(sourceFilePath: String, newId: String): String? =
        storage.duplicate(sourceFilePath, newId)
}

private class MasterpieceFileStorage(
    private val context: Context
) {
    fun saveBitmap(bitmap: Bitmap, id: String): String? = runOrNull {
        val file = masterpieceFile(id)
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, outputStream)
        }
        file.absolutePath
    }

    fun loadAll(): List<String> = runOrDefault(emptyList()) {
        masterpieceDirectory()
            .listFiles()
            .orEmpty()
            .asSequence()
            .filter(::isMasterpieceFile)
            .sortedByDescending(File::lastModified)
            .map(File::absolutePath)
            .toList()
    }

    fun delete(filePath: String): Boolean = runOrDefault(false) {
        val file = File(filePath)
        file.exists() && file.delete()
    }

    fun duplicate(sourceFilePath: String, newId: String): String? = runOrNull {
        val sourceFile = File(sourceFilePath)
        if (!sourceFile.exists()) null
        else sourceFile.copyTo(masterpieceFile(newId), overwrite = true).absolutePath
    }

    private fun masterpieceDirectory(): File =
        ContextWrapper(context).getDir(MASTERPIECE_DIRECTORY_NAME, Context.MODE_PRIVATE)

    private fun masterpieceFile(id: String): File = File(masterpieceDirectory(), "$id.$FILE_EXTENSION")

    private fun isMasterpieceFile(file: File): Boolean = file.extension == FILE_EXTENSION

    private inline fun <T> runOrNull(block: () -> T): T? = try {
        block()
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    }

    private inline fun <T> runOrDefault(defaultValue: T, block: () -> T): T = try {
        block()
    } catch (_: IOException) {
        defaultValue
    } catch (_: SecurityException) {
        defaultValue
    }

    private companion object {
        const val MASTERPIECE_DIRECTORY_NAME = "masterpiece"
        const val FILE_EXTENSION = "png"
        const val COMPRESS_QUALITY = 100
    }
}
