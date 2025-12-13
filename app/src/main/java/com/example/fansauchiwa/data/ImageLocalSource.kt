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
import java.util.UUID
import javax.inject.Inject

class ImageLocalSource @Inject constructor(
    @field:ApplicationContext private val context: Context
) : ImageDataSource {
    override fun save(uri: Uri, id: String): String? {
        val stream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(BufferedInputStream(stream))

        val directory = ContextWrapper(context).getDir(
            "image",
            Context.MODE_PRIVATE
        )
        val imageId = UUID.randomUUID().toString()
        val file = File(directory, "$imageId.jpg")

        val result = FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }

        return if (result) {
            imageId
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

    override fun getAllImageIds(): List<String> {
        val directory = ContextWrapper(context).getDir("image", Context.MODE_PRIVATE)

        return directory.listFiles()
            ?.filter { it.isFile && it.extension == "jpg" }
            ?.map { it.nameWithoutExtension }
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