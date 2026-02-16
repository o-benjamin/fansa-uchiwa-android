package com.fansauchiwa.data.repository

import android.net.Uri
import com.fansauchiwa.data.EraserPath
import com.fansauchiwa.data.infra.ImageProcessingDataSource
import javax.inject.Inject

interface ImageProcessingRepository {
    suspend fun removeBackground(sourceUri: Uri): Result<Uri>

    /**
     * 手動修正（消しゴム）のパスを画像に適用する
     * @param imageUri 元画像のURI
     * @param paths 適用するパスのリスト
     * @param previewWidth プレビュー表示領域の幅（ピクセル）
     * @param previewHeight プレビュー表示領域の高さ（ピクセル）
     * @return 加工後の画像URI
     */
    suspend fun applyManualCorrection(
        imageUri: Uri,
        paths: List<EraserPath>,
        previewWidth: Int,
        previewHeight: Int
    ): Result<Uri>
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

    override suspend fun applyManualCorrection(
        imageUri: Uri,
        paths: List<EraserPath>,
        previewWidth: Int,
        previewHeight: Int
    ): Result<Uri> {
        val resultUri = imageProcessingDataSource.applyManualCorrection(
            imageUri,
            paths,
            previewWidth,
            previewHeight
        )
        return if (resultUri != null) {
            Result.success(resultUri)
        } else {
            Result.failure(Exception("Failed to apply manual correction"))
        }
    }
}

