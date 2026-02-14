package com.fansauchiwa.data.infra

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ImageProcessingLocalSource @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageProcessingDataSource {
    override suspend fun removeBackground(sourceUri: Uri): Uri? {
        return try {
            // InputImageを取得
            val inputImage = InputImage.fromFilePath(context, sourceUri)

            // SubjectSegmenterの設定
            val options = SubjectSegmenterOptions.Builder()
                .enableForegroundBitmap()
                .build()

            val segmenter = SubjectSegmentation.getClient(options)

            // 背景透過処理を実行
            val result = segmenter.process(inputImage).await()
            val foregroundBitmap = result.foregroundBitmap ?: return null

            // 一時ファイルとして保存
            val timestamp = System.currentTimeMillis()
            val tempFile = File(context.cacheDir, "processed_image_$timestamp.png")

            FileOutputStream(tempFile).use { outputStream ->
                foregroundBitmap.compress(
                    android.graphics.Bitmap.CompressFormat.PNG,
                    100,
                    outputStream
                )
            }

            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            null
        }
    }
}

