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
    private val storage = MasterpieceStorage(context)

    override fun saveBitmap(bitmap: Bitmap, id: String): String? =
        storage.save(bitmap = bitmap, id = MasterpieceId(id))

    override fun loadAllMasterpieces(): List<String> = storage.loadAll()

    override fun deleteMasterpiece(filePath: String): Boolean = storage.delete(filePath)

    override fun duplicateMasterpiece(sourceFilePath: String, newId: String): String? =
        storage.duplicate(sourceFilePath = sourceFilePath, newId = MasterpieceId(newId))
}

private class MasterpieceStorage(
    private val context: Context
) {
    fun save(bitmap: Bitmap, id: MasterpieceId): String? {
        val file = masterpieceFile(id)

        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            file.absolutePath
        } catch (_: IOException) {
            null
        } catch (_: SecurityException) {
            null
        }
    }

    fun loadAll(): List<String> {
        return try {
            masterpieceDirectory().listFiles()
                .orEmpty()
                .asSequence()
                .filter(File::isMasterpieceImage)
                .sortedByDescending(File::lastModified)
                .map(File::absolutePath)
                .toList()
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    fun delete(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (_: SecurityException) {
            false
        }
    }

    fun duplicate(sourceFilePath: String, newId: MasterpieceId): String? {
        val sourceFile = File(sourceFilePath)
        if (!sourceFile.exists()) {
            return null
        }

        val newFile = masterpieceFile(newId)
        return try {
            sourceFile.copyTo(newFile, overwrite = true)
            newFile.absolutePath
        } catch (_: IOException) {
            null
        } catch (_: SecurityException) {
            null
        }
    }

    private fun masterpieceDirectory(): File = ContextWrapper(context).getDir(
        MASTERPIECE_DIRECTORY_NAME,
        Context.MODE_PRIVATE
    )

    private fun masterpieceFile(id: MasterpieceId): File =
        File(masterpieceDirectory(), id.fileName)
}

@JvmInline
private value class MasterpieceId(
    val value: String
) {
    val fileName: String
        get() = "$value.$MASTERPIECE_FILE_EXTENSION"
}

private fun File.isMasterpieceImage(): Boolean = extension == MASTERPIECE_FILE_EXTENSION

private const val MASTERPIECE_DIRECTORY_NAME = "masterpiece"
private const val MASTERPIECE_FILE_EXTENSION = "png"
