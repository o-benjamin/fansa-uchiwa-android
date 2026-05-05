package com.fansauchiwa.data.infra

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import com.fansauchiwa.data.ImageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

private const val IMAGE_DIRECTORY_NAME = "image"
private const val IMAGE_EXTENSION = "png"

internal class ImageFileStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun save(uri: Uri, id: String): String? {
        val file = imageFile(id)

        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun load(imageId: String): ImageReference? {
        val file = imageFile(imageId)
        return if (file.exists()) {
            ImageReference(imageId, file.absolutePath)
        } else {
            null
        }
    }

    fun getAllImages(): List<ImageReference> {
        return imageDirectory().listFiles()
            ?.map { file -> ImageReference(file.nameWithoutExtension, file.absolutePath) }
            ?: emptyList()
    }

    fun deleteImages(imageIds: List<String>): Boolean {
        return imageIds.all { imageId ->
            imageFile(imageId).delete()
        }
    }

    private fun imageDirectory(): File {
        return ContextWrapper(context).getDir(IMAGE_DIRECTORY_NAME, Context.MODE_PRIVATE)
    }

    private fun imageFile(imageId: String): File {
        return File(imageDirectory(), "$imageId.$IMAGE_EXTENSION")
    }
}
