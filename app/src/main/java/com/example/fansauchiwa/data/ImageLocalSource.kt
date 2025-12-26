package com.example.fansauchiwa.data

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ImageLocalSource @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageDataSource {
    override fun save(uri: Uri, id: String): String? {
        val stream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(BufferedInputStream(stream))

        val directory = ContextWrapper(context).getDir(
            "image",
            Context.MODE_PRIVATE
        )
        val file = File(directory, "$id.jpg")

        val result = FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }

        return if (result) {
            id
        } else {
            null
        }
    }

    override fun load(imageId: String): Bitmap? {
        val directory = ContextWrapper(context).getDir("image", Context.MODE_PRIVATE)
        val file = File(directory, "$imageId.jpg")

        return if (file.exists()) {
            file.inputStream().use { inputStream ->
                BitmapFactory.decodeStream(BufferedInputStream(inputStream))
            }
        } else {
            null
        }
    }

    override fun getImagesByIds(ids: List<String>): List<Bitmap> {
        val directory = ContextWrapper(context).getDir("image", Context.MODE_PRIVATE)

        return ids.mapNotNull { id ->
            val file = File(directory, "$id.jpg")
            if (file.exists()) {
                file.inputStream().use { inputStream ->
                    BitmapFactory.decodeStream(BufferedInputStream(inputStream))
                }
            } else {
                null
            }
        }
    }

    override fun getAllImages(): List<ImageBitmap> {
        val directory = ContextWrapper(context).getDir("image", Context.MODE_PRIVATE)
        return directory.listFiles()
            ?.mapNotNull { file ->
                file.inputStream().use { input ->
                    val bitmap = BitmapFactory.decodeStream(BufferedInputStream(input))
                    bitmap?.let { ImageBitmap(file.nameWithoutExtension, it) }
                }
            }
            ?: emptyList()
    }

    override fun delete(imageId: String): Boolean {
        val directory = ContextWrapper(context).getDir(
            "image",
            Context.MODE_PRIVATE
        )
        val file = File(directory, "$imageId.jpg")
        return file.delete()
    }
}