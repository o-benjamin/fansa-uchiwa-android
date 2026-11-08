package com.fansauchiwa.data.infra

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.core.graphics.createBitmap
import com.fansauchiwa.data.AiMaterialSourceImage
import com.fansauchiwa.data.GeneratedAiMaterial
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Base64
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

// 元画像の長辺の上限。この大きさの JPEG ならサーバーの上限（base64 で 4MB）に収まる
private const val SOURCE_IMAGE_MAX_EDGE_PX = 1024
private const val SOURCE_IMAGE_JPEG_QUALITY = 85
private const val SOURCE_IMAGE_MIME_TYPE = "image/jpeg"

class AiMaterialImageLocalSource @Inject constructor(
    @ApplicationContext private val context: Context
) : AiMaterialImageDataSource {

    override suspend fun encodeSourceImage(sourceUri: Uri): AiMaterialSourceImage =
        withContext(Dispatchers.IO) {
            val source = ImageDecoder.createSource(context.contentResolver, sourceUri)
            val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val longEdge = max(info.size.width, info.size.height)
                if (longEdge > SOURCE_IMAGE_MAX_EDGE_PX) {
                    val scale = SOURCE_IMAGE_MAX_EDGE_PX.toFloat() / longEdge
                    decoder.setTargetSize(
                        (info.size.width * scale).roundToInt().coerceAtLeast(1),
                        (info.size.height * scale).roundToInt().coerceAtLeast(1)
                    )
                }
                // compress はハードウェアのビットマップを読めない
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
            val bytes = ByteArrayOutputStream().use { output ->
                bitmap.flattenedOnWhite()
                    .compress(Bitmap.CompressFormat.JPEG, SOURCE_IMAGE_JPEG_QUALITY, output)
                output.toByteArray()
            }
            AiMaterialSourceImage(
                mimeType = SOURCE_IMAGE_MIME_TYPE,
                base64Data = Base64.getEncoder().encodeToString(bytes)
            )
        }

    override suspend fun writeGeneratedImage(material: GeneratedAiMaterial): Uri =
        withContext(Dispatchers.IO) {
            val extension = material.mimeType.substringAfter('/')
            val file = File(context.cacheDir, "ai_material_${System.currentTimeMillis()}.$extension")
            file.writeBytes(Base64.getDecoder().decode(material.imageBase64))
            Uri.fromFile(file)
        }

    // JPEG は透明を持てず、透明なところが黒になる。透過 PNG のイラストなどは白の上に描いてから送る
    private fun Bitmap.flattenedOnWhite(): Bitmap {
        if (!hasAlpha()) return this
        val opaque = createBitmap(width, height)
        Canvas(opaque).apply {
            drawColor(Color.WHITE)
            drawBitmap(this@flattenedOnWhite, 0f, 0f, null)
        }
        return opaque
    }
}
