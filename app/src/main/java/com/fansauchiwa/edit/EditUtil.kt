package com.fansauchiwa.edit

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.Transformation
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * デコレーションの移動量に対してキャンバス中央（X=0, Y=0）へのスナップ判定・補正を行う
 *
 * @param decorationOffset デコレーションの確定済み座標
 * @param offsetDiff 現在の移動量（calculateClampedOffset適用後）
 * @param snapThreshold スナップ判定の閾値（px）
 * @return スナップ補正後の結果
 */
internal fun applySnapToCenter(
    decorationOffset: Offset,
    offsetDiff: Offset,
    snapThreshold: Float
): SnapResult {
    val newX = decorationOffset.x + offsetDiff.x
    val newY = decorationOffset.y + offsetDiff.y

    val snappedX = abs(newX) <= snapThreshold
    val snappedY = abs(newY) <= snapThreshold

    val snappedOffsetDiff = Offset(
        x = if (snappedX) -decorationOffset.x else offsetDiff.x,
        y = if (snappedY) -decorationOffset.y else offsetDiff.y
    )

    return SnapResult(
        offsetDiff = snappedOffsetDiff,
        snappedX = snappedX,
        snappedY = snappedY
    )
}

internal fun calculateTransformations(
    cumulativeOffset: Offset,
    dragStartPosition: Offset,
): Transformation {
    // ドラッグしている場所と要素の中心との距離からScaleを計算
    val initialDistance = dragStartPosition.getDistance()
    val currentDistance = (dragStartPosition + cumulativeOffset).getDistance()
    val distanceChange = currentDistance - initialDistance
    val scaleFactor = 0.005f
    val scaleDiff = distanceChange * scaleFactor

    // ドラッグしている場所と要素の中心との角度からRotationを計算
    val initialAngle = dragStartPosition.toAngleDegrees()
    val currentPosition = dragStartPosition + cumulativeOffset
    val currentAngle = currentPosition.toAngleDegrees()
    val rotationDiff = currentAngle - initialAngle

    return Transformation(scaleDiff, rotationDiff)
}


internal fun calculateHandleOffset(
    baseOffset: Offset,
    scale: Float,
    rotation: Float,
    decorationSize: Size,
    corner: HandleCorner,
): Offset {
    val cornerOffset = when (corner) {
        HandleCorner.TopLeft -> Offset(-decorationSize.width / 2f, -decorationSize.height / 2f)
        HandleCorner.TopRight -> Offset(decorationSize.width / 2f, -decorationSize.height / 2f)
        HandleCorner.BottomLeft -> Offset(-decorationSize.width / 2f, decorationSize.height / 2f)
        HandleCorner.BottomRight -> Offset(decorationSize.width / 2f, decorationSize.height / 2f)
    }
    val scaledCornerOffset = cornerOffset * scale
    return baseOffset + scaledCornerOffset.rotateBy(rotation)
}

internal fun rotatedDragAmount(
    currentRotation: Float,
    currentScale: Float,
    dragAmount: Offset,
): Offset {
    val scaledDragAmount = dragAmount * currentScale
    return scaledDragAmount.rotateBy(currentRotation)
}

internal fun Offset.toAngleDegrees(): Float {
    return atan2(y, x) * 180f / PI.toFloat()
}

internal fun Offset.rotateBy(degrees: Float): Offset {
    val angleRad = Math.toRadians(degrees.toDouble()).toFloat()
    val cos = cos(angleRad)
    val sin = sin(angleRad)
    return Offset(
        x = this.x * cos - this.y * sin,
        y = this.x * sin + this.y * cos
    )
}

/**
 * 回転角度のスナップ判定と補正を行う。
 *
 * スナップ目標角度（0, 90, 180, 270度）に対して ±[ROTATION_SNAP_THRESHOLD_DEGREES] 以内であれば、
 * 角度をスナップ先に固定する。
 *
 * @param totalRotation 現在のトータル回転角度（確定済み角度 + diff）
 * @return スナップ結果
 */
internal fun applyRotationSnap(totalRotation: Float): RotationSnapResult {
    val normalized = ((totalRotation % 360f) + 360f) % 360f
    for (snapPoint in ROTATION_SNAP_POINTS) {
        if (abs(normalized - snapPoint) <= ROTATION_SNAP_THRESHOLD_DEGREES) {
            val snappedTotal = totalRotation - normalized + snapPoint
            return RotationSnapResult(snappedRotation = snappedTotal, isSnapped = true)
        }
    }
    // 360度近傍（例: 358度）のケース: 0度へのスナップ
    if (360f - normalized <= ROTATION_SNAP_THRESHOLD_DEGREES) {
        val snappedTotal = totalRotation - normalized + 360f
        return RotationSnapResult(snappedRotation = snappedTotal, isSnapped = true)
    }
    return RotationSnapResult(snappedRotation = totalRotation, isSnapped = false)
}

private val ROTATION_SNAP_POINTS = listOf(0f, 90f, 180f, 270f)
private const val ROTATION_SNAP_THRESHOLD_DEGREES = 4f

internal val TextUnit.nonScaledSp: TextUnit
    @Composable
    get() = (value / LocalDensity.current.fontScale).sp

/**
 * isNew でないエントリに 0 始まりの通し番号を付与するマップを作成する。
 * isNew = true のエントリには null が割り当てられる。
 */
fun <T> buildRankIndexMap(
    entries: List<T>,
    isNew: (T) -> Boolean
): Map<T, Int?> {
    var rank = 0
    return entries.associateWith { entry ->
        if (!isNew(entry)) rank++ else null
    }
}

/**
 * デコレーションアイテムの移動量を境界内に制限する
 *
 * @param currentConfirmedOffset 現在の確定座標
 * @param cumulativeOffset 累積の移動量
 * @param dragAmount 今回のドラッグ量
 * @param boundarySize 境界サイズ（うちわ画像のサイズ）
 * @return 制限を適用した後の新しい累積の移動量
 */
internal fun calculateClampedOffset(
    currentConfirmedOffset: Offset,
    cumulativeOffset: Offset,
    dragAmount: Offset,
    boundarySize: IntSize?
): Offset {
    if (boundarySize == null || boundarySize.width == 0 || boundarySize.height == 0) {
        return cumulativeOffset + dragAmount
    }

    // 新しい位置を計算
    val newCumulativeOffset = cumulativeOffset + dragAmount
    val newPosition = currentConfirmedOffset + newCumulativeOffset

    // 境界範囲を計算（中心が原点なので、-width/2 から width/2 まで）
    val halfWidth = (boundarySize.width / 2f) * 0.7f
    val halfHeight = (boundarySize.height / 2f) * 0.9f

    // 新しい位置を境界内に制限
    val clampedX = newPosition.x.coerceIn(-halfWidth, halfWidth)
    val clampedY = newPosition.y.coerceIn(-halfHeight, halfHeight)

    // 制限された位置から累積の移動量を逆算
    return Offset(
        x = clampedX - currentConfirmedOffset.x,
        y = clampedY - currentConfirmedOffset.y
    )
}

internal fun resolveDecorationOffset(
    decorationId: String,
    selectedDecorationId: String?,
    baseOffset: Offset,
    offsetDiff: Offset
): Offset {
    if (decorationId != selectedDecorationId) return baseOffset
    return baseOffset + offsetDiff
}

internal fun resolveDecorationScale(
    decorationId: String,
    selectedDecorationId: String?,
    baseScale: Float,
    scaleDiff: Float
): Float {
    if (decorationId != selectedDecorationId) return baseScale
    return baseScale * scaleDiff
}

internal fun resolveDecorationRotation(
    decorationId: String,
    selectedDecorationId: String?,
    baseRotation: Float,
    rotationDiff: Float
): Float {
    if (decorationId != selectedDecorationId) return baseRotation
    return baseRotation + rotationDiff
}

internal fun resolveDecorationZIndex(
    decorationId: String,
    selectedDecorationId: String?
): Float {
    if (decorationId != selectedDecorationId) return 0f
    return 1f
}

/**
 * `scaleDiff` を 1f 基準の乗算値として保持しつつ、既存の保存 API が期待する加算差分へ変換する。
 *
 * 例: `baseScale = 2f`, `scaleDiff = 1.5f` の場合、表示上の目標スケールは `3f` なので、
 * ViewModel に保存する加算差分は `3f - 2f = 1f` になる。
 */
internal fun calculateCommittedScaleDiff(
    baseScale: Float,
    scaleDiff: Float
): Float = baseScale * (scaleDiff - 1f)

internal fun calculateScaleFactor(
    baseScale: Float,
    targetScale: Float
): Float {
    if (baseScale == 0f) return 1f
    return targetScale / baseScale
}

internal fun Decoration.scaleRange(): ClosedFloatingPointRange<Float> = when (this) {
    is Decoration.Text -> 0.5f..6f
    is Decoration.Sticker -> 0.5f..3f
    is Decoration.Image -> 0.5f..5f
}

/**
 * Compose標準の [androidx.compose.foundation.gestures.detectTransformGestures] に、
 * 全ての指が画面から離れたことを検知するための [onEnd] コールバックを追加した独自の拡張関数です。
 * 差分をViewModelのStateに反映させるタイミングを確保するためにカスタムしました。
 *
 * また、大きさに絞った微調整を可能にするため、
 * 1本指での操作時はPan（移動）を許可し、2本指以上での操作時はPanによる移動を無効にする制御を行っています。
 */
suspend fun PointerInputScope.detectTransformGesturesWithEnd(
    panZoomLock: Boolean = false,
    onEnd: () -> Unit = {},
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit,
) {
    awaitEachGesture {
        var rotation = 0f
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        var lockedToPanZoom = false

        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.fastAny { it.isConsumed }
            if (!canceled) {
                val zoomChange = event.calculateZoom()
                val rotationChange = event.calculateRotation()
                val activePointers = event.changes.count { it.pressed }
                val panChange = if (activePointers >= 2) Offset.Zero else event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    rotation += rotationChange
                    pan += panChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                    val panMotion = pan.getDistance()

                    if (
                        zoomMotion > touchSlop ||
                        rotationMotion > touchSlop ||
                        panMotion > touchSlop
                    ) {
                        pastTouchSlop = true
                        lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                    }
                }

                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                    if (effectiveRotation != 0f || zoomChange != 1f || panChange != Offset.Zero) {
                        onGesture(centroid, panChange, zoomChange, effectiveRotation)
                    }
                    event.changes.fastForEach {
                        if (it.positionChanged()) {
                            it.consume()
                        }
                    }
                }
            }
        } while (!canceled && event.changes.fastAny { it.pressed })
        onEnd()
    }
}

suspend fun PointerInputScope.detectNonConsumingTap(onTap: () -> Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)

        // Composeによるタッチ領域の自動拡張分を無視し、厳密に枠線内か判定
        val isInside = down.position.x in 0f..size.width.toFloat() &&
                down.position.y in 0f..size.height.toFloat()
        if (!isInside) return@awaitEachGesture // 範囲外なら無視

        var isTap = true
        var upEvent: androidx.compose.ui.input.pointer.PointerInputChange? = null
        do {
            val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Main)
            if (event.changes.size > 1) isTap = false
            val change = event.changes.firstOrNull { it.id == down.id }
            if (change != null) {
                if (change.isConsumed) isTap = false
                else if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) isTap =
                    false
                if (!change.pressed) upEvent = change
            }
        } while (event.changes.any { it.pressed })

        if (isTap && upEvent != null) {
            upEvent.consume()
            onTap()
        }
    }
}
