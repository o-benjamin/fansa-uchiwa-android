package com.example.fansauchiwa.data

import android.net.Uri
import javax.inject.Inject

interface LocalImageRepository {
    fun saveImage(uri: Uri, id: String): String?
    fun loadImage(imageId: String): ImageReference?
    fun getAllImages(): List<ImageReference>
    fun deleteImages(imageIds: List<String>): Boolean
}

class LocalImageRepositoryImpl @Inject constructor(
    private val imageDataSource: ImageDataSource
) : LocalImageRepository {

    override fun saveImage(uri: Uri, id: String): String? {
        return imageDataSource.save(uri, id)
    }

    override fun loadImage(imageId: String): ImageReference? {
        return imageDataSource.load(imageId)
    }

    override fun getAllImages(): List<ImageReference> {
        return imageDataSource.getAllImages()
    }

    override fun deleteImages(imageIds: List<String>): Boolean {
        return imageDataSource.deleteImages(imageIds)
    }
}

