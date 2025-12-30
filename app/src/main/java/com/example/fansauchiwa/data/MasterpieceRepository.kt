package com.example.fansauchiwa.data

import android.graphics.Bitmap
import javax.inject.Inject

interface MasterpieceRepository {
    fun saveMasterpieceBitmap(bitmap: Bitmap, id: String): String?
    fun saveMasterpieceToGallery(imagePath: String): Boolean
    fun loadAllMasterpieces(): List<String>
}

class MasterpieceRepositoryImpl @Inject constructor(
    private val masterpieceDataSource: MasterpieceDataSource,
    private val galleryImageDataSource: GalleryImageDataSource
) : MasterpieceRepository {
    override fun saveMasterpieceBitmap(bitmap: Bitmap, id: String): String? {
        return masterpieceDataSource.saveBitmap(bitmap, id)
    }

    override fun saveMasterpieceToGallery(imagePath: String): Boolean {
        return galleryImageDataSource.saveImageToGallery(imagePath)
    }

    override fun loadAllMasterpieces(): List<String> {
        return masterpieceDataSource.loadAllMasterpieces()
    }
}

