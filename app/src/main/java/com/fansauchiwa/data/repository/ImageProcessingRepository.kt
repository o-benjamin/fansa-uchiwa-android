package com.fansauchiwa.data.repository

import android.net.Uri
import com.fansauchiwa.data.BackgroundRemovalException
import com.fansauchiwa.data.EraserPath
import com.fansauchiwa.data.infra.ImageProcessingDataSource
import javax.inject.Inject

interface ImageProcessingRepository {
    /**
     * 画像の背景を透過する
     * @return 透過後の画像URI。失敗時は [BackgroundRemovalException] を持つ failure
     */
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
    override suspend fun removeBackground(sourceUri: Uri): Result<Uri> = try {
        Result.success(imageProcessingDataSource.removeBackground(sourceUri))
    } catch (e: BackgroundRemovalException) {
        Result.failure(e)
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

