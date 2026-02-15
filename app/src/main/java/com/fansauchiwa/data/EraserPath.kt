package com.fansauchiwa.data

import androidx.compose.ui.graphics.Path

/**
 * 消しゴムのパス情報を保持するデータクラス
 * @param path ユーザーが描画したパス（画像ローカル座標系）
 * @param strokeWidth パスの線の太さ（ズーム倍率に応じて計算済み）
 */
data class EraserPath(
    val path: Path,
    val strokeWidth: Float
)

