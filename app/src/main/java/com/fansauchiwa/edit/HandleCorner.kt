package com.fansauchiwa.edit

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * 変形ハンドルの基準点を表す値オブジェクト。
 *
 * 描画層はどの角を使うかだけを指定し、座標計算はこの型に集約する。
 */
internal enum class HandleCorner(
    private val horizontalDirection: Float,
    private val verticalDirection: Float,
) {
    TopLeft(
        horizontalDirection = -1f,
        verticalDirection = -1f
    ),
    TopRight(
        horizontalDirection = 1f,
        verticalDirection = -1f
    ),
    BottomLeft(
        horizontalDirection = -1f,
        verticalDirection = 1f
    ),
    BottomRight(
        horizontalDirection = 1f,
        verticalDirection = 1f
    );

    fun toOffset(size: Size): Offset {
        return Offset(
            x = size.width * horizontalDirection / 2f,
            y = size.height * verticalDirection / 2f
        )
    }
}
