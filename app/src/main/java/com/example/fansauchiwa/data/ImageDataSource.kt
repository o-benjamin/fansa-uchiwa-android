package com.example.fansauchiwa.data

import android.graphics.Bitmap
import android.net.Uri

interface ImageDataSource {
    fun save(uri: Uri, id: String): String?
    fun load(imageId: String): ImageBitmap?
    fun getImagesByIds(ids: List<String>): List<Bitmap>
    fun getAllImages(): List<ImageBitmap>
    fun delete(imageId: String): Boolean
}