package com.example.fansauchiwa.data

import android.graphics.Bitmap
import android.net.Uri
import com.example.fansauchiwa.data.source.FansaUchiwaDao
import com.example.fansauchiwa.data.source.FansaUchiwaEntity
import javax.inject.Inject

interface FansaUchiwaRepository {
    // region image
    fun saveImage(uri: Uri, id: String): String?
    fun loadImage(imageId: String): ImageBitmap?
    fun getImagesByIds(ids: List<String>): List<Bitmap>
    fun getAllImages(): List<ImageBitmap>
    fun deleteImage(imageId: String): Boolean
    // endregion

    // region decoration
    suspend fun saveDecorations(decorations: List<Decoration>)
    suspend fun getDecorations(id: Int): List<Decoration>?
    // endregion
}

class FansaUchiwaRepositoryImpl @Inject constructor(
    private val imageDataSource: ImageDataSource,
    private val fansaUchiwaDao: FansaUchiwaDao
) : FansaUchiwaRepository {

    private val converters = Converters()

    // region image
    override fun saveImage(uri: Uri, id: String): String? {
        return imageDataSource.save(uri, id)
    }

    override fun loadImage(imageId: String): ImageBitmap? {
        return imageDataSource.load(imageId)
    }

    override fun getImagesByIds(ids: List<String>): List<Bitmap> {
        return imageDataSource.getImagesByIds(ids)
    }

    override fun getAllImages(): List<ImageBitmap> {
        return imageDataSource.getAllImages()
    }

    override fun deleteImage(imageId: String): Boolean {
        return imageDataSource.delete(imageId)
    }
    // endregion

    // region decoration
    override suspend fun saveDecorations(decorations: List<Decoration>) {
        val decorationsJson = converters.decorationsToJson(decorations)
        val fansaUchiwaEntity = FansaUchiwaEntity(decorations = decorationsJson)
        return fansaUchiwaDao.upsertUchiwaData(fansaUchiwaEntity)
    }

    override suspend fun getDecorations(id: Int): List<Decoration>? {
        val uchiwaData = fansaUchiwaDao.getUchiwaById(id)
        return uchiwaData?.let {
            converters.decorationsFromJson(it.decorations)
        }
    }
    // endregion
}