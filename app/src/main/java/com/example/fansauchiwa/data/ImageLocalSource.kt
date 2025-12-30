package com.example.fansauchiwa.data

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ImageLocalSource @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageDataSource {
    override fun save(uri: Uri, id: String): String? {
        val directory = ContextWrapper(context).getDir(
            "image",
            Context.MODE_PRIVATE
        )
        val file = File(directory, "$id.jpg")

        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    override fun load(imageId: String): ImageReference? {
        val directory = ContextWrapper(context).getDir("image", Context.MODE_PRIVATE)
        val file = File(directory, "$imageId.jpg")

        return if (file.exists()) {
            ImageReference(imageId, file.absolutePath)
        } else {
            null
        }
    }

    override fun getAllImages(): List<ImageReference> {
        val directory = ContextWrapper(context).getDir("image", Context.MODE_PRIVATE)
        return directory.listFiles()
            ?.map { file ->
                ImageReference(file.nameWithoutExtension, file.absolutePath)
            }
            ?: emptyList()
    }

    override fun deleteImages(imageIds: List<String>): Boolean {
        val directory = ContextWrapper(context).getDir("image", Context.MODE_PRIVATE)
        return imageIds.all { imageId ->
            val file = File(directory, "$imageId.jpg")
            file.delete()
        }
    }
}