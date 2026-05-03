package com.fansauchiwa.data.infra

import android.net.Uri
import com.fansauchiwa.data.ImageReference

interface ImageDataSource {
    fun save(uri: Uri, id: String): String?
    fun load(imageId: String): ImageReference?
    fun getAllImages(): List<ImageReference>
    fun deleteImages(imageIds: List<String>): Boolean
}
