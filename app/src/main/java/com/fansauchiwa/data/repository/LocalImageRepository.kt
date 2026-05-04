package com.fansauchiwa.data.repository

import android.net.Uri
import com.fansauchiwa.data.ImageReference
import com.fansauchiwa.data.infra.ImageDataSource
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

    override fun saveImage(uri: Uri, id: String): String? = imageDataSource.save(uri, id)

    override fun loadImage(imageId: String): ImageReference? = imageDataSource.load(imageId)

    override fun getAllImages(): List<ImageReference> = imageDataSource.getAllImages()

    override fun deleteImages(imageIds: List<String>): Boolean = imageDataSource.deleteImages(imageIds)
}
