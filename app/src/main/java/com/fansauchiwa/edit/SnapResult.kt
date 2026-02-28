package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset

/**
 * スナップ判定の結果を保持するデータクラス
 *
 * @param offsetDiff スナップ補正後の移動量
 * @param snappedX X軸がスナップされたかどうか
 * @param snappedY Y軸がスナップされたかどうか
 */
internal data class SnapResult(
    val offsetDiff: Offset,
    val snappedX: Boolean,
    val snappedY: Boolean
)

