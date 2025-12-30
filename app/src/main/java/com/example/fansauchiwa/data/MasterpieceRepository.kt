package com.example.fansauchiwa.data

import android.graphics.Bitmap
import javax.inject.Inject

interface MasterpieceRepository {
    fun saveMasterpieceBitmap(bitmap: Bitmap): String?
    fun saveMasterpieceToGallery(imagePath: String): Boolean
}

class MasterpieceRepositoryImpl @Inject constructor(
    private val masterpieceDataSource: MasterpieceDataSource,
    private val galleryImageDataSource: GalleryImageDataSource
) : MasterpieceRepository {
    override fun saveMasterpieceBitmap(bitmap: Bitmap): String? {
        val id = "${System.currentTimeMillis()}"
        return masterpieceDataSource.saveBitmap(bitmap, id)
    }

    override fun saveMasterpieceToGallery(imagePath: String): Boolean {
        return galleryImageDataSource.saveImageToGallery(imagePath)
    }
}

