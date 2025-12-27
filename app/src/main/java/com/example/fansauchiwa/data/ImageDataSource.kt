package com.example.fansauchiwa.data

import android.net.Uri

interface ImageDataSource {
    fun save(uri: Uri, id: String): String?
    fun load(imageId: String): ImageReference?
    fun getAllImages(): List<ImageReference>
    fun delete(imageId: String): Boolean
}