package com.fansauchiwa.data

import android.net.Uri

/**
 * ローカル画像ストレージに対するインフラ層のポート。
 *
 * Presentation / ViewModel から直接ファイル操作の責務を持ち込まないよう、
 * Repository から利用する保存・参照・削除の契約だけをここに定義する。
 */
interface ImageDataSource :
    ImageSaveDataSource,
    ImageQueryDataSource,
    ImageDeleteDataSource

interface ImageSaveDataSource {
    fun save(uri: Uri, id: String): String?
}

interface ImageQueryDataSource {
    fun load(imageId: String): ImageReference?
    fun getAllImages(): List<ImageReference>
}

interface ImageDeleteDataSource {
    fun deleteImages(imageIds: List<String>): Boolean
}
