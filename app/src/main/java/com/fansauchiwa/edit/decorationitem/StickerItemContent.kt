package com.fansauchiwa.edit.decorationitem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.ui.StickerAsset
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

private const val MinimumStickerInnerScale = 0.1f
private const val StickerStrokeScaleFactor = 10f

/**
 * StickerのコンテンツをCanvasを使って描画するComposable
 *
 * @param decoration 描画対象のStickerデコレーション
 * @param modifier Modifier
 */
@Composable
fun StickerItemContent(
    decoration: Decoration.Sticker,
    modifier: Modifier
) {
    val uiModel = remember(decoration) {
        decoration.toStickerItemContentUiModel()
    } ?: return
    val imageVector = ImageVector.vectorResource(id = uiModel.resId)
    val painter = rememberVectorPainter(image = imageVector)
    val renderState = remember(uiModel, imageVector, painter.intrinsicSize) {
        uiModel.toRenderState(
            imageVector = imageVector,
            intrinsicSize = painter.intrinsicSize
        )
    }

    StickerItemCanvas(
        renderState = renderState,
        modifier = modifier
    )
}

@Composable
private fun StickerItemCanvas(
    renderState: StickerItemRenderState,
    modifier: Modifier
) {
    val density = LocalDensity.current

    Canvas(
        modifier = modifier
            .size(
                width = with(density) { renderState.canvasSize.width.toDp() },
                height = with(density) { renderState.canvasSize.height.toDp() }
            )
            .drawWithCache {
                onDrawWithContent {
                    drawSticker(renderState)
                }
            }
    ) {
        // 描画はdrawWithCache内で行う
    }
}

private data class StickerItemContentUiModel(
    val resId: Int,
    val fillColor: Color,
    val primaryStroke: StickerStrokeUiModel,
    val secondaryStroke: StickerStrokeUiModel
)

private data class StickerStrokeUiModel(
    val color: Color,
    val width: Float
)

private data class StickerItemRenderState(
    val canvasSize: Size,
    val viewportSize: Size,
    val fillColor: Color,
    val primaryStroke: StickerStrokeUiModel,
    val secondaryStroke: StickerStrokeUiModel,
    val innerScale: Float,
    val root: StickerVectorGroupNode
)

private sealed interface StickerVectorNode

private data class StickerVectorGroupNode(
    val translationX: Float,
    val translationY: Float,
    val rotation: Float,
    val pivotX: Float,
    val pivotY: Float,
    val scaleX: Float,
    val scaleY: Float,
    val children: List<StickerVectorNode>
) : StickerVectorNode

private data class StickerVectorPathNode(
    val path: Path
) : StickerVectorNode

private fun Decoration.Sticker.toStickerItemContentUiModel(): StickerItemContentUiModel? {
    if (resId == 0) return null

    return StickerItemContentUiModel(
        resId = resId,
        fillColor = color,
        primaryStroke = StickerStrokeUiModel(
            color = strokeColor,
            width = strokeWidth
        ),
        secondaryStroke = StickerStrokeUiModel(
            color = secondStrokeColor,
            width = secondStrokeWidth
        )
    )
}

private fun StickerItemContentUiModel.toRenderState(
    imageVector: ImageVector,
    intrinsicSize: Size
): StickerItemRenderState {
    val totalStrokeWidth = primaryStroke.width + secondaryStroke.width

    return StickerItemRenderState(
        canvasSize = intrinsicSize,
        viewportSize = Size(imageVector.viewportWidth, imageVector.viewportHeight),
        fillColor = fillColor,
        primaryStroke = primaryStroke,
        secondaryStroke = secondaryStroke,
        innerScale = calculateStickerInnerScale(
            intrinsicSize = intrinsicSize,
            totalStrokeWidth = totalStrokeWidth
        ),
        root = imageVector.root.toStickerVectorGroupNode()
    )
}

private fun calculateStickerInnerScale(
    intrinsicSize: Size,
    totalStrokeWidth: Float
): Float {
    val shrinkAmount = totalStrokeWidth * StickerStrokeScaleFactor
    val innerScaleX = if (intrinsicSize.width > totalStrokeWidth * 2) {
        (intrinsicSize.width - shrinkAmount) / intrinsicSize.width
    } else {
        MinimumStickerInnerScale
    }
    val innerScaleY = if (intrinsicSize.height > totalStrokeWidth * 2) {
        (intrinsicSize.height - shrinkAmount) / intrinsicSize.height
    } else {
        MinimumStickerInnerScale
    }

    return minOf(innerScaleX, innerScaleY)
}

private fun VectorGroup.toStickerVectorGroupNode(): StickerVectorGroupNode {
    return StickerVectorGroupNode(
        translationX = translationX,
        translationY = translationY,
        rotation = rotation,
        pivotX = pivotX,
        pivotY = pivotY,
        scaleX = scaleX,
        scaleY = scaleY,
        children = List(size) { index ->
            when (val node = this[index]) {
                is VectorGroup -> node.toStickerVectorGroupNode()
                is VectorPath -> node.toStickerVectorPathNode()
                else -> error("Unsupported vector node: ${node::class.simpleName}")
            }
        }
    )
}

private fun VectorPath.toStickerVectorPathNode(): StickerVectorPathNode {
    val vectorPath = Path()
    PathParser().addPathNodes(pathData).toPath(vectorPath)
    return StickerVectorPathNode(path = vectorPath)
}

/**
 * 描画モードを定義する列挙型
 * - StrokeOnly: 枠線のみを描画
 * - FillOnly: 塗りつぶしのみを描画
 */
private enum class DrawMode { StrokeOnly, FillOnly }

/**
 * ImageVectorを枠線付きで描画する
 * strokeWidthが大きくなっても外形サイズが変わらないように内部画像を縮小して描画する
 */
private fun DrawScope.drawSticker(
    renderState: StickerItemRenderState
) {
    val scaleX = size.width / renderState.viewportSize.width
    val scaleY = size.height / renderState.viewportSize.height
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    withTransform({
        translate(centerX, centerY)
        scale(renderState.innerScale, renderState.innerScale, pivot = Offset.Zero)
        translate(-centerX, -centerY)
        scale(scaleX, scaleY, pivot = Offset.Zero)
    }) {
        val adjustedPrimaryStrokeWidth = renderState.primaryStroke.width / renderState.innerScale
        val adjustedSecondaryStrokeWidth = renderState.secondaryStroke.width / renderState.innerScale

        if (adjustedSecondaryStrokeWidth > 0f) {
            drawStickerNode(
                node = renderState.root,
                fillColor = renderState.fillColor,
                strokeColor = renderState.secondaryStroke.color,
                strokeWidth = adjustedPrimaryStrokeWidth + adjustedSecondaryStrokeWidth,
                drawMode = DrawMode.StrokeOnly
            )
        }

        if (adjustedPrimaryStrokeWidth > 0f) {
            drawStickerNode(
                node = renderState.root,
                fillColor = renderState.fillColor,
                strokeColor = renderState.primaryStroke.color,
                strokeWidth = adjustedPrimaryStrokeWidth,
                drawMode = DrawMode.StrokeOnly
            )
        }

        drawStickerNode(
            node = renderState.root,
            fillColor = renderState.fillColor,
            strokeColor = renderState.primaryStroke.color,
            strokeWidth = adjustedPrimaryStrokeWidth,
            drawMode = DrawMode.FillOnly
        )
    }
}

private fun DrawScope.drawStickerNode(
    node: StickerVectorNode,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float,
    drawMode: DrawMode
) {
    when (node) {
        is StickerVectorGroupNode -> {
            withTransform({
                translate(node.translationX, node.translationY)
                rotate(node.rotation, pivot = Offset(node.pivotX, node.pivotY))
                scale(node.scaleX, node.scaleY, pivot = Offset(node.pivotX, node.pivotY))
            }) {
                node.children.forEach { child ->
                    drawStickerNode(
                        node = child,
                        fillColor = fillColor,
                        strokeColor = strokeColor,
                        strokeWidth = strokeWidth,
                        drawMode = drawMode
                    )
                }
            }
        }

        is StickerVectorPathNode -> {
            when (drawMode) {
                DrawMode.StrokeOnly -> {
                    if (strokeWidth > 0f) {
                        drawPath(
                            path = node.path,
                            color = strokeColor,
                            style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
                        )
                    }
                }

                DrawMode.FillOnly -> {
                    drawPath(
                        path = node.path,
                        color = fillColor,
                        style = Fill
                    )
                }
            }
        }
    }
}

// region StickerItemContent Previews

private val previewStickerDecoration = Decoration.Sticker(
    id = "preview-sticker-1",
    label = StickerAsset.HEART.type,
    color = Color.Magenta,
    strokeColor = Color.White,
    strokeWidth = 3f,
    secondStrokeColor = Color.White,
    secondStrokeWidth = 0f,
)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun StickerItemContentPreview() {
    FansaUchiwaTheme {
        StickerItemContent(
            decoration = previewStickerDecoration,
            modifier = Modifier,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun StickerItemContentWithSecondStrokePreview() {
    FansaUchiwaTheme {
        StickerItemContent(
            decoration = previewStickerDecoration.copy(
                secondStrokeColor = Color.Cyan,
                secondStrokeWidth = 6f,
            ),
            modifier = Modifier,
        )
    }
}

// endregion
