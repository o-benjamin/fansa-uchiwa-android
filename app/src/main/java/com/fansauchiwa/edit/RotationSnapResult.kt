package com.fansauchiwa.edit

/**
 * 回転スナップ判定の結果を保持するデータクラス
 *
 * @param snappedRotation スナップ補正後の回転角度
 * @param isSnapped スナップされたかどうか
 */
internal data class RotationSnapResult(
    val snappedRotation: Float,
    val isSnapped: Boolean
)

