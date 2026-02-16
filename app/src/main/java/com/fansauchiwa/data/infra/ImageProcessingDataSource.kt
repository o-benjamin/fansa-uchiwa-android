package com.fansauchiwa.data.infra

import android.net.Uri
import com.fansauchiwa.data.EraserPath

interface ImageProcessingDataSource {
    suspend fun removeBackground(sourceUri: Uri): Uri?

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

