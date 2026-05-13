package com.fansauchiwa.edit.decorationitem

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.ColorSpace
import android.graphics.HardwareRenderer
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RenderNode
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.hardware.HardwareBuffer
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import androidx.compose.ui.graphics.Canvas as ComposeCanvas

// ==========================================
// シェーダーパラメータ定義
// ==========================================

data class PuffyShaderParams(
    /** フチの斜面の幅（丸み）。小さくすると平らな面積が広く急な斜面に、大きくするとかまぼこ型に丸みを帯びる */
    val edgeWidthMulti: Float = 4.0f,
    /** 影の滑らかさ（法線計算のサンプリング距離）。大きくすると滑らかになるがディテールがぼやける */
    val stepMulti: Float = 2.5f,
    /** 立体の高さ（面の傾斜の強さ）。小さくすると傾斜が急になり影が落ちやすく、大きくすると明るくなる */
    val normalZ: Float = 2.0f,
    /** 光源のX方向の位置（右が+、左が-） */
    val lightDirX: Float = 0.5f,
    /** 光源のY方向の位置（下が+、上が- ※AndroidのY軸方向） */
    val lightDirY: Float = -0.5f,
    /** 光源のZ方向の位置（光の手前具合） */
    val lightDirZ: Float = 1.5f,
    /** 影の濃さ。0.0に近づけると影が濃く（漆黒に）、大きくすると影が薄く明るくなる */
    val shadowDarkness: Float = 0.0f,
    /** 反射の鋭さ。大きくすると鋭く光り（ガラス状）、小さくすると広くぼやける（ゴム状） */
    val shininess: Float = 300.0f,
    /** ハイライト（白飛び）の強さ。大きくするとより強烈に白飛びする */
    val specularIntensity: Float = 1.2f,

    // ▼ 頂点の尖りを防ぐぼかし（ぼかし半径） ▼
    /** 斜面幅に対してどの程度の割合（距離）を計算に含めるか */
    val blurRadiusMulti: Float = 0.4f,
    /** ぼかしの最大ピクセル半径。広すぎる範囲を参照して破綻するのを防ぐための上限値 */
    val blurMaxPix: Float = 15.0f,

    // ▼ ぼかしの重み付け（シャープ芯の強さ vs 全体の滑らかさ） ▼
    /** 中央（ピーク）の重み。大きくすると芯がエッジとして残りやすい */
    val blurWeightCenter: Float = 4.0f,
    /** 上下左右の重み */
    val blurWeightCross: Float = 2.0f,
    /** 斜め方向の重み */
    val blurWeightDiag: Float = 1.0f
)

// ==========================================
// シェーダー定義
// ==========================================

// 1. エッジ検出＆初期化（Pass 0）
const val INIT_SDF_SHADER = """
    uniform shader textMask;
    uniform float2 size;
    half4 main(float2 fragCoord) {
        float alpha = textMask.eval(fragCoord).a;
        if (alpha <= 0.0) return half4(0.0, 0.0, 0.0, 0.0);

        float aTop   = textMask.eval(fragCoord + float2(0.0, -1.0)).a;
        float aBot   = textMask.eval(fragCoord + float2(0.0, 1.0)).a;
        float aLeft  = textMask.eval(fragCoord + float2(-1.0, 0.0)).a;
        float aRight = textMask.eval(fragCoord + float2(1.0, 0.0)).a;

        if (aTop <= 0.0 || aBot <= 0.0 || aLeft <= 0.0 || aRight <= 0.0 || alpha < 1.0) {
            return half4(fragCoord.x / size.x, fragCoord.y / size.y, 1.0, alpha);
        } else {
            return half4(0.0, 0.0, 0.0, alpha);
        }
    }
"""

// 2. JFAステップ（Pass 1〜N）
const val JFA_STEP_SHADER = """
    uniform shader prevPass;
    uniform float2 size;
    uniform float stepSize;
    
    half4 main(float2 fragCoord) {
        half4 bestData = prevPass.eval(fragCoord);
        float originalAlpha = bestData.a;
        
        float2 bestCoord = bestData.rg * size;
        float bestDist = (bestData.b > 0.5) ? distance(fragCoord, bestCoord) : 999999.0;

        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                if (x == 0 && y == 0) continue;
                float2 neighborPos = fragCoord + float2(x, y) * stepSize;
                if (neighborPos.x < 0.0 || neighborPos.y < 0.0 || neighborPos.x >= size.x || neighborPos.y >= size.y) continue;

                half4 neighborData = prevPass.eval(neighborPos);
                if (neighborData.b > 0.5) {
                    float2 neighborSeed = neighborData.rg * size;
                    float d = distance(fragCoord, neighborSeed);
                    if (d < bestDist) {
                        bestDist = d;
                        bestData = half4(neighborSeed.x / size.x, neighborSeed.y / size.y, 1.0, originalAlpha);
                    }
                }
            }
        }
        return bestData;
    }
"""

// 3. ライティングとぷっくり描画（最終レンダリング）
const val PUFFY_RENDER_SHADER = """
    uniform shader sdfTexture;
    uniform float2 size;
    uniform half3 baseColor;
    uniform float scaleFactor;
    
    uniform float p_edgeWidthMulti;
    uniform float p_stepMulti;
    uniform float p_normalZ;
    uniform float3 p_lightDir;
    uniform float p_shadowDarkness;
    uniform float p_shininess;
    uniform float p_specularIntensity;
    
    uniform float p_blurRadiusMulti;
    uniform float p_blurMaxPix;
    uniform float p_blurWeightCenter;
    uniform float p_blurWeightCross;
    uniform float p_blurWeightDiag;
    
    float getSignedDist(float2 coord) {
        half4 data = sdfTexture.eval(coord);
        float d = distance(coord, data.rg * size);
        return data.a > 0.0 ? d : -d;
    }

    float getHeight(float2 coord) {
        float edgeWidth = p_edgeWidthMulti * scaleFactor; 
        
        float blur = min(edgeWidth * p_blurRadiusMulti, p_blurMaxPix * scaleFactor); 
        
        float d0 = getSignedDist(coord);
        float d1 = getSignedDist(coord + float2(-blur, 0.0));
        float d2 = getSignedDist(coord + float2(blur, 0.0));
        float d3 = getSignedDist(coord + float2(0.0, -blur));
        float d4 = getSignedDist(coord + float2(0.0, blur));
        
        float blur2 = blur * 0.707106; // 45度方向のオフセット(1/√2)
        float d5 = getSignedDist(coord + float2(-blur2, -blur2));
        float d6 = getSignedDist(coord + float2(blur2, -blur2));
        float d7 = getSignedDist(coord + float2(-blur2, blur2));
        float d8 = getSignedDist(coord + float2(blur2, blur2));
        
        float totalWeight = p_blurWeightCenter + 4.0 * p_blurWeightCross + 4.0 * p_blurWeightDiag;
        float dist = (d0 * p_blurWeightCenter + (d1 + d2 + d3 + d4) * p_blurWeightCross + (d5 + d6 + d7 + d8) * p_blurWeightDiag) / totalWeight;
        dist = max(dist, 0.0);
        
        float sdf = clamp(dist / edgeWidth, 0.0, 1.0);
        
        return sin(sdf * 1.5707963);
    }
    
    half4 main(float2 fragCoord) {
        float2 texCoord = fragCoord * scaleFactor;
        half4 data = sdfTexture.eval(texCoord);
        float alpha = data.a;
        
        if (alpha <= 0.0) return half4(0.0);
        
        float step = p_stepMulti * scaleFactor; 
        
        float hL = getHeight(texCoord + float2(-step, 0.0)); 
        float hR = getHeight(texCoord + float2(step, 0.0));  
        float hT = getHeight(texCoord + float2(0.0, -step)); 
        float hB = getHeight(texCoord + float2(0.0, step));  
        
        float3 normal = normalize(float3(hL - hR, hT - hB, p_normalZ));
        
        float3 lightDirVec = normalize(p_lightDir); 
        
        float flatDiffuse = lightDirVec.z; 
        float currentDiffuse = max(0.0, dot(normal, lightDirVec));
        
        float brightness = p_shadowDarkness + currentDiffuse * ((1.0 - p_shadowDarkness) / flatDiffuse);
        
        float3 viewDir = float3(0.0, 0.0, 1.0); 
        float3 halfDir = normalize(lightDirVec + viewDir);

        float specular = pow(max(0.0, dot(normal, halfDir)), p_shininess) * p_specularIntensity;
        
        half3 finalRGB = baseColor * brightness + half3(specular);
        
        return half4(finalRGB * alpha, alpha);
    }
"""

// ==========================================
// テクスチャ生成・描画ロジック
// ==========================================

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
suspend fun generateSdfTexture(textMask: Bitmap): Bitmap = withContext(Dispatchers.Default) {
    val width = textMask.width
    val height = textMask.height

    val format = PixelFormat.RGBA_8888
    val usage = HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or HardwareBuffer.USAGE_GPU_COLOR_OUTPUT

    val readerA = ImageReader.newInstance(width, height, format, 2, usage)
    val readerB = ImageReader.newInstance(width, height, format, 2, usage)

    val renderNode = RenderNode("SdfNode")
    renderNode.setPosition(0, 0, width, height)

    val renderer = HardwareRenderer()
    renderer.setContentRoot(renderNode)

    val paint = Paint()
    val colorSpace = ColorSpace.get(ColorSpace.Named.LINEAR_SRGB)

    var prevImage: Image? = null
    var currentImage: Image? = null
    var currentBitmap: Bitmap? = null

    suspend fun renderTo(targetReader: ImageReader, shader: RuntimeShader) {
        renderer.setSurface(targetReader.surface)

        val canvas = renderNode.beginRecording()
        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        renderNode.endRecording()

        val nextImage = suspendCancellableCoroutine { cont ->
            targetReader.setOnImageAvailableListener({ reader ->
                reader.setOnImageAvailableListener(null, null)
                val image = reader.acquireNextImage()
                if (image != null) {
                    cont.resume(image)
                } else {
                    cont.cancel(IllegalStateException("Image is still null"))
                }
            }, Handler(Looper.getMainLooper()))

            renderer.createRenderRequest().syncAndDraw()
        }

        val nextBitmap = nextImage.hardwareBuffer?.let { Bitmap.wrapHardwareBuffer(it, colorSpace) }
            ?: throw IllegalStateException("Failed to wrap HardwareBuffer")

        prevImage?.close()
        prevImage = currentImage
        currentImage = nextImage
        currentBitmap = nextBitmap
    }

    // Pass 0
    val initShader = RuntimeShader(INIT_SDF_SHADER).apply {
        setInputBuffer(
            "textMask",
            BitmapShader(textMask, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        )
        setFloatUniform("size", width.toFloat(), height.toFloat())
    }
    renderTo(readerA, initShader)

    // Pass 1〜N (JFAループ)
    var stepSize = Integer.highestOneBit(maxOf(width, height))
    var useReaderB = true
    var passCount = 1
    while (stepSize >= 1) {
        val jfaShader = RuntimeShader(JFA_STEP_SHADER).apply {
            setFloatUniform("size", width.toFloat(), height.toFloat())
            setFloatUniform("stepSize", stepSize.toFloat())
            setInputBuffer(
                "prevPass",
                BitmapShader(currentBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            )
        }

        val targetReader = if (useReaderB) readerB else readerA
        renderTo(targetReader, jfaShader)

        useReaderB = !useReaderB
        stepSize /= 2
        passCount++
    }

    val finalSoftwareBitmap = currentBitmap!!.copy(Bitmap.Config.ARGB_8888, false)

    currentImage?.close()
    prevImage?.close()
    readerA.close()
    readerB.close()
    renderer.destroy()

    return@withContext finalSoftwareBitmap
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun PuffyTextRenderer(
    sdfTextureBitmap: Bitmap,
    baseColor: Color,
    scaleFactor: Float,
    modifier: Modifier = Modifier,
    shaderParams: PuffyShaderParams = PuffyShaderParams()
) {
    val paint = remember(sdfTextureBitmap, baseColor, scaleFactor, shaderParams) {
        val shader = RuntimeShader(PUFFY_RENDER_SHADER).apply {
            setInputBuffer(
                "sdfTexture",
                BitmapShader(sdfTextureBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            )
            setFloatUniform(
                "size",
                sdfTextureBitmap.width.toFloat(),
                sdfTextureBitmap.height.toFloat()
            )
            setFloatUniform("baseColor", baseColor.red, baseColor.green, baseColor.blue)
            setFloatUniform("scaleFactor", scaleFactor)

            setFloatUniform("p_edgeWidthMulti", shaderParams.edgeWidthMulti)
            setFloatUniform("p_stepMulti", shaderParams.stepMulti)
            setFloatUniform("p_normalZ", shaderParams.normalZ)
            setFloatUniform(
                "p_lightDir",
                shaderParams.lightDirX,
                shaderParams.lightDirY,
                shaderParams.lightDirZ
            )
            setFloatUniform("p_shadowDarkness", shaderParams.shadowDarkness)
            setFloatUniform("p_shininess", shaderParams.shininess)
            setFloatUniform("p_specularIntensity", shaderParams.specularIntensity)

            setFloatUniform("p_blurRadiusMulti", shaderParams.blurRadiusMulti)
            setFloatUniform("p_blurMaxPix", shaderParams.blurMaxPix)
            setFloatUniform("p_blurWeightCenter", shaderParams.blurWeightCenter)
            setFloatUniform("p_blurWeightCross", shaderParams.blurWeightCross)
            setFloatUniform("p_blurWeightDiag", shaderParams.blurWeightDiag)
        }

        val nativePaint = Paint().apply {
            this.shader = shader
            this.isFilterBitmap = true
            this.isAntiAlias = true
        }

        androidx.compose.ui.graphics.Paint().apply {
            asFrameworkPaint().set(nativePaint)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawIntoCanvas { canvas ->
                    canvas.drawRect(
                        left = 0f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height,
                        paint = paint
                    )
                }
            }
    )
}

fun createTextMaskBitmap(
    layoutResult: TextLayoutResult,
    density: Density,
    drawStyle: DrawStyle = Fill,
    scaleFactor: Float = 2f,
    maxStroke: Float = 0f,
    clearInner: Boolean = false,
    clearStroke: Stroke? = null
): Bitmap {
    val width = ((layoutResult.size.width + maxStroke) * scaleFactor).toInt()
    val height = ((layoutResult.size.height + maxStroke) * scaleFactor).toInt()

    if (width <= 0 || height <= 0) {
        return createBitmap(1, 1, Bitmap.Config.ALPHA_8)
    }

    val imageBitmap = ImageBitmap(width, height)
    val canvas = ComposeCanvas(imageBitmap)
    val size = Size(width.toFloat(), height.toFloat())

    CanvasDrawScope().draw(density, LayoutDirection.Ltr, canvas, size) {
        scale(scaleFactor, scaleFactor, pivot = Offset.Zero) {
            translate(maxStroke / 2f, maxStroke / 2f) {
                drawText(
                    textLayoutResult = layoutResult,
                    color = Color.White,
                    drawStyle = drawStyle
                )
                if (clearStroke != null) {
                    drawText(
                        textLayoutResult = layoutResult,
                        color = Color.Transparent,
                        drawStyle = clearStroke,
                        blendMode = BlendMode.Clear
                    )
                }
                if (clearInner) {
                    drawText(
                        textLayoutResult = layoutResult,
                        color = Color.Transparent,
                        drawStyle = Fill,
                        blendMode = BlendMode.Clear
                    )
                }
            }
        }
    }

    return imageBitmap.asAndroidBitmap()
}

