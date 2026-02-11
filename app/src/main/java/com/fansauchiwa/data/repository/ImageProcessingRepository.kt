package com.fansauchiwa.data.repository

import android.net.Uri
import com.fansauchiwa.data.infra.ImageProcessingDataSource
import javax.inject.Inject

interface ImageProcessingRepository {
    suspend fun removeBackground(sourceUri: Uri): Result<Uri>
}

class ImageProcessingRepositoryImpl @Inject constructor(
    private val imageProcessingDataSource: ImageProcessingDataSource
) : ImageProcessingRepository {
    override suspend fun removeBackground(sourceUri: Uri): Result<Uri> {
        val resultUri = imageProcessingDataSource.removeBackground(sourceUri)
        return if (resultUri != null) {
            Result.success(resultUri)
        } else {
            Result.failure(Exception("Failed to remove background"))
        }
    }
}

