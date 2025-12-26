package com.example.fansauchiwa.data

import android.graphics.Bitmap
import android.net.Uri

interface ImageDataSource {
    fun save(uri: Uri, id: String): String?
    fun load(imageId: String): Bitmap?
    fun getAllImages(): List<Bitmap>
    fun delete(imageId: String): Boolean
}