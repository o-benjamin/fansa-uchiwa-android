package com.fansauchiwa.data.infra

import android.net.Uri
import com.fansauchiwa.data.BackgroundRemovalException
import com.fansauchiwa.data.EraserPath

interface ImageProcessingDataSource {
    /**
     * 画像の背景を透過する
     * @param sourceUri 元画像のURI
     * @return 透過後の画像URI
     * @throws BackgroundRemovalException 失敗したとき（理由は [BackgroundRemovalException.reason]）
     */
    suspend fun removeBackground(sourceUri: Uri): Uri

    /**
     * 画像に人の顔が写っているかを確かめる
     * @param sourceUri 確かめる画像のURI
     * @return 顔があれば true
     * @throws Exception 確かめられなかったとき（モジュールの取得失敗・読み込み失敗など）
     */
    suspend fun containsFace(sourceUri: Uri): Boolean

    /**
     * 手動修正（消しゴム）のパスを画像に適用する
     * @param imageUri 元画像のURI
     * @param paths 適用するパスのリスト
     * @param previewWidth プレビュー表示領域の幅（ピクセル）
     * @param previewHeight プレビュー表示領域の高さ（ピクセル）
     * @return 加工後の画像URI、失敗時はnull
     */
    suspend fun applyManualCorrection(
        imageUri: Uri,
        paths: List<EraserPath>,
        previewWidth: Int,
        previewHeight: Int
    ): Uri?
}

