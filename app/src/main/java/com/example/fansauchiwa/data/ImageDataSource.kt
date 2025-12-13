package com.example.fansauchiwa.data

import android.graphics.Bitmap
import android.net.Uri

interface ImageDataSource {
    fun save(uri: Uri): String?
    fun load(imageId: String): Bitmap?
    fun getAllImageIds(): List<String>
    fun delete(imageId: String): Boolean
}