package com.fansauchiwa.data.infra

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import androidx.compose.ui.graphics.asAndroidPath
import com.fansauchiwa.data.EraserPath
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

            withContext(Dispatchers.IO) {
                FileOutputStream(tempFile).use { outputStream ->
                    foregroundBitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        outputStream
                    )
                }
            }

            Uri.fromFile(tempFile)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun applyManualCorrection(
        imageUri: Uri,
        paths: List<EraserPath>,
        previewWidth: Int,
        previewHeight: Int
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            // URIから元画像を読み込む
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return@withContext null

            val bitmapWidth = originalBitmap.width
            val bitmapHeight = originalBitmap.height

            // Step 1: プレビュー時の表示倍率と余白を算出（ContentScale.Fit）
            val scale = minOf(
                previewWidth.toFloat() / bitmapWidth,
                previewHeight.toFloat() / bitmapHeight
            )
            val dx = (previewWidth - bitmapWidth * scale) / 2f
            val dy = (previewHeight - bitmapHeight * scale) / 2f

            // Step 2: 逆変換用のMatrixを作成
            val matrix = android.graphics.Matrix()
            matrix.postTranslate(-dx, -dy)
            matrix.postScale(1f / scale, 1f / scale)

            // MutableなBitmapを作成（ARGB_8888で透過をサポート）
            val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            originalBitmap.recycle()

            // Canvasを作成してパスを描画
            val canvas = Canvas(mutableBitmap)

            // 消しゴム用のPaintを設定
            val eraserPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
                // 透過処理: 描画先の該当部分を透明にする
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }

            // 各パスを描画（座標変換を適用）
            for (eraserPath in paths) {
                // Step 3: ブラシサイズも補正
                eraserPaint.strokeWidth = eraserPath.strokeWidth / scale
                // パスをコピーしてMatrixを適用
                val androidPath = android.graphics.Path(eraserPath.path.asAndroidPath())
                androidPath.transform(matrix)
                canvas.drawPath(androidPath, eraserPaint)
            }

            // 加工後のBitmapを一時ファイルに保存
            val timestamp = System.currentTimeMillis()
            val tempFile = File(context.cacheDir, "manual_correction_$timestamp.png")

            FileOutputStream(tempFile).use { outputStream ->
                mutableBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            mutableBitmap.recycle()

            Uri.fromFile(tempFile)
        } catch (_: Exception) {
            null
        }
    }
}

