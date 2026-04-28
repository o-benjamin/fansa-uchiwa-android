package com.fansauchiwa.data

import android.graphics.Bitmap

typealias MasterpieceId = String
typealias MasterpieceFilePath = String

/**
 * Masterpiece 永続化のためのストレージポート。
 *
 * Presentation 層はこの契約を直接扱わず、Repository 経由でユースケースを実行する。
 * 実装詳細（ローカル保存、ディレクトリ構成、複製方法など）は LocalSource 側に閉じ込める。
 */
interface MasterpieceDataSource {
    /**
     * アプリケーション層向けの保存操作。
     * 既存実装との互換性維持のため、現在は従来APIへ委譲する。
     */
    fun saveMasterpiece(
        bitmap: Bitmap,
        masterpieceId: MasterpieceId
    ): MasterpieceFilePath? = saveBitmap(bitmap = bitmap, id = masterpieceId)

    /**
     * アプリケーション層向けの一覧取得操作。
     */
    fun findAll(): List<MasterpieceFilePath> = loadAllMasterpieces()

    /**
     * アプリケーション層向けの削除操作。
     */
    fun delete(filePath: MasterpieceFilePath): Boolean = deleteMasterpiece(filePath)

    /**
     * アプリケーション層向けの複製操作。
     */
    fun duplicateMasterpieceFile(
        sourceFilePath: MasterpieceFilePath,
        newMasterpieceId: MasterpieceId
    ): MasterpieceFilePath? = duplicateMasterpiece(
        sourceFilePath = sourceFilePath,
        newId = newMasterpieceId
    )

    /**
     * 既存 Repository 実装との互換性維持用 API。
     * 呼び出し側の段階的移行が完了するまで残す。
     */
    fun saveBitmap(bitmap: Bitmap, id: MasterpieceId): MasterpieceFilePath?
    fun loadAllMasterpieces(): List<MasterpieceFilePath>
    fun deleteMasterpiece(filePath: MasterpieceFilePath): Boolean
    fun duplicateMasterpiece(
        sourceFilePath: MasterpieceFilePath,
        newId: MasterpieceId
    ): MasterpieceFilePath?
}
