package com.fansauchiwa.data.infra

import android.net.Uri
import com.fansauchiwa.data.ImageReference
import javax.inject.Inject

class ImageLocalSource @Inject constructor(
    private val imageFileStore: ImageFileStore
) : ImageDataSource {

    override fun save(uri: Uri, id: String): String? = imageFileStore.save(uri, id)

    override fun load(imageId: String): ImageReference? = imageFileStore.load(imageId)

    override fun getAllImages(): List<ImageReference> = imageFileStore.getAllImages()

    override fun deleteImages(imageIds: List<String>): Boolean = imageFileStore.deleteImages(imageIds)
}
