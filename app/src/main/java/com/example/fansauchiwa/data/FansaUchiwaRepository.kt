package com.example.fansauchiwa.data

import android.net.Uri
import com.example.fansauchiwa.data.source.FansaUchiwaDao
import com.example.fansauchiwa.data.source.FansaUchiwaEntity
import javax.inject.Inject

interface FansaUchiwaRepository {
    // region image
    fun saveImage(uri: Uri, id: String): String?
    fun loadImage(imageId: String): ImageReference?
    fun getAllImages(): List<ImageReference>
    fun deleteImages(imageIds: List<String>): Boolean
    // endregion

    // region decoration
    suspend fun saveDecorations(id: String, decorations: List<Decoration>)
    suspend fun getDecorations(id: String): List<Decoration>?
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

    override fun loadImage(imageId: String): ImageReference? {
        return imageDataSource.load(imageId)
    }

    override fun getAllImages(): List<ImageReference> {
        return imageDataSource.getAllImages()
    }

    override fun deleteImages(imageIds: List<String>): Boolean {
        return imageDataSource.deleteImages(imageIds)
    }
    // endregion

    // region decoration
    override suspend fun saveDecorations(id: String, decorations: List<Decoration>) {
        val decorationsJson = converters.decorationsToJson(decorations)
        val fansaUchiwaEntity = FansaUchiwaEntity(id = id, decorations = decorationsJson)
        return fansaUchiwaDao.upsertUchiwaData(fansaUchiwaEntity)
    }

    override suspend fun getDecorations(id: String): List<Decoration>? {
        val uchiwaData = fansaUchiwaDao.getUchiwaById(id)
        return uchiwaData?.let {
            converters.decorationsFromJson(it.decorations)
        }
    }
    // endregion
}