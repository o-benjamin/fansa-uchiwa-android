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
import com.fansauchiwa.data.BackgroundRemovalException
import com.fansauchiwa.data.BackgroundRemovalFailureReason
import com.fansauchiwa.data.EraserPath
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

// ML Kit のモジュールのダウンロードを待つ上限。超えても裏でダウンロードは続き、次の試行で使える
private const val MODULE_INSTALL_TIMEOUT_MILLIS = 60_000L

class ImageProcessingLocalSource @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageProcessingDataSource {
    override suspend fun removeBackground(sourceUri: Uri): Uri {
        val options = SubjectSegmenterOptions.Builder()
            .enableForegroundBitmap()
            .build()
        val segmenter = SubjectSegmentation.getClient(options)

        try {
            ensureModuleInstalled(segmenter)

            val inputImage = InputImage.fromFilePath(context, sourceUri)
            val result = segmenter.process(inputImage).await()
            val foregroundBitmap = result.foregroundBitmap
                ?: throw BackgroundRemovalException(BackgroundRemovalFailureReason.NO_SUBJECT)

            // 一時ファイルとして保存
            val tempFile = File(context.cacheDir, "processed_image_${System.currentTimeMillis()}.png")
            withContext(Dispatchers.IO) {
                FileOutputStream(tempFile).use { outputStream ->
                    foregroundBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            }
            return Uri.fromFile(tempFile)
        } catch (e: BackgroundRemovalException) {
            // 理由付きの失敗は包み直さず、そのまま呼び出し元へ渡す
            throw e
        } catch (e: CancellationException) {
            // Play 開発者サービスの Task が取り消されたときも await() は CancellationException を投げる。
            // 呼び出し元のコルーチンが取り消されたときだけそのまま投げ、それ以外は失敗として画面に出す
            currentCoroutineContext().ensureActive()
            throw BackgroundRemovalException(BackgroundRemovalFailureReason.PROCESS_FAILED, e)
        } catch (e: Exception) {
            throw BackgroundRemovalException(BackgroundRemovalFailureReason.PROCESS_FAILED, e)
        } finally {
            segmenter.close()
        }
    }

    /**
     * 背景透過の ML Kit モジュールが端末になければダウンロードし、完了まで待つ。
     * マニフェストの `com.google.mlkit.vision.DEPENDENCIES` によるインストール時の取得は、
     * Play ストア以外から入れた場合や取得が済んでいない場合には効かないため、ここでも確かめる。
     */
    private suspend fun ensureModuleInstalled(segmenter: SubjectSegmenter) {
        val moduleInstallClient = ModuleInstall.getClient(context)
        val isAvailable = moduleInstallClient.areModulesAvailable(segmenter)
            .awaitOrModuleUnavailable()
            .areModulesAvailable()
        if (isAvailable) return

        // installModules() の Task は要求を受け付けた時点で完了するため、完了はリスナーで待つ
        val installResult = CompletableDeferred<Unit>()
        val listener = InstallStatusListener { update ->
            // ダウンロード中などの途中経過は待つだけなので扱わない
            when (update.installState) {
                InstallState.STATE_COMPLETED -> installResult.complete(Unit)
                InstallState.STATE_FAILED, InstallState.STATE_CANCELED -> installResult.completeExceptionally(
                    BackgroundRemovalException(
                        BackgroundRemovalFailureReason.MODULE_UNAVAILABLE,
                        Exception("InstallStatusListener errorCode=${update.errorCode}")
                    )
                )
            }
        }
        val request = ModuleInstallRequest.newBuilder()
            .addApi(segmenter)
            .setListener(listener)
            .build()

        try {
            val response = moduleInstallClient.installModules(request).awaitOrModuleUnavailable()
            if (response.areModulesAlreadyInstalled()) return

            withTimeoutOrNull(MODULE_INSTALL_TIMEOUT_MILLIS) { installResult.await() }
                ?: throw BackgroundRemovalException(BackgroundRemovalFailureReason.MODULE_TIMEOUT)
        } finally {
            moduleInstallClient.unregisterListener(listener)
        }
    }

    private suspend fun <T> Task<T>.awaitOrModuleUnavailable(): T = try {
        await()
    } catch (e: ApiException) {
        throw BackgroundRemovalException(
            BackgroundRemovalFailureReason.MODULE_UNAVAILABLE,
            Exception("ApiException statusCode=${e.statusCode}", e)
        )
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

