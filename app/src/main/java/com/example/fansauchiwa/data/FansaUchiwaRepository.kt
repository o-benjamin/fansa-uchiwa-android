package com.example.fansauchiwa.data

import android.graphics.Bitmap
import android.net.Uri
import com.example.fansauchiwa.data.source.FansaUchiwaDao
import com.example.fansauchiwa.data.source.FansaUchiwaEntity
import javax.inject.Inject

interface FansaUchiwaRepository {
    // region image
    fun saveImage(uri: Uri, id: String): String?
    fun loadImage(imageId: String): Bitmap?
    fun getAllImageIds(): List<String>
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

    override fun loadImage(imageId: String): Bitmap? {
        return imageDataSource.load(imageId)
    }

    override fun getAllImageIds(): List<String> {
        return imageDataSource.getAllImageIds()
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