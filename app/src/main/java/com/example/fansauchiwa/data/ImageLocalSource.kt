package com.example.fansauchiwa.data

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ImageLocalSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ImageDataSource {
    override fun save(uri: Uri) {
        val stream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(BufferedInputStream(stream))

        val directory = ContextWrapper(context).getDir(
            "image",
            Context.MODE_PRIVATE
        )

        val file = File(directory, "image.jpg")

        val result = FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }

        if (result) {
            Log.d("footprint", "save success: $file, uri: $uri")
        } else {
            Log.d("footprint", "save failed: $file, uri: $uri")

        }
    }
}