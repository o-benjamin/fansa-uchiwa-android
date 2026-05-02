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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.edit.FontFamilies
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import androidx.compose.ui.graphics.Canvas as ComposeCanvas

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
    
    float getHeight(float2 coord) {
        half4 data = sdfTexture.eval(coord);
        if (data.a <= 0.0) return 0.0;
        
        float2 closestEdge = data.rg * size;
        float dist = distance(coord, closestEdge);
        
        // ▼ チューニング: 【フチの斜面の幅（丸み）】 ▼
        // edgeWidth: 縁から平らな面（本来の色）に到達するまでの距離。
        // - 小さくする(例: 2.0): 平らな面積が広がり、斜面が急になります。
        // - 大きくする(例: 8.0): 文字全体が丸みを帯び、かまぼこ型になります。
        float edgeWidth = 4.0 * scaleFactor; 
        float sdf = clamp(dist / edgeWidth, 0.0, 1.0);
        
        return sin(sdf * 1.5707963);
    }
    
    half4 main(float2 fragCoord) {
        float2 texCoord = fragCoord * scaleFactor;
        half4 data = sdfTexture.eval(texCoord);
        float alpha = data.a;
        
        if (alpha <= 0.0) return half4(0.0);
        
        // ▼ チューニング: 【影のギザギザ軽減（滑らかさ）】 ▼
        // step: 法線を計算する際のサンプリング距離。
        // - 大きくする(例: 3.5): ギザギザが滑らかになりますが、ディテールが少しぼやけます。
        // - 小さくする(例: 1.0): ディテールはクッキリしますが、影がジャギジャギになりやすいです。
        float step = 2.5 * scaleFactor; 
        
        float hL = getHeight(texCoord + float2(-step, 0.0)); 
        float hR = getHeight(texCoord + float2(step, 0.0));  
        float hT = getHeight(texCoord + float2(0.0, -step)); 
        float hB = getHeight(texCoord + float2(0.0, step));  
        
        // ▼ チューニング: 【立体の高さ（影の落ちやすさ）】 ▼
        // normalizeのZ値(ここでは 2.0): 面の傾斜の強さを決めます。
        // - 小さくする(例: 1.0): 傾斜が急になり、影がくっきり落ちやすくなります。
        // - 大きくする(例: 4.0): 傾斜がなだらかになり、全体的に明るくなります。
        float3 normal = normalize(float3(hL - hR, hT - hB, 2.0));
        
        // ▼ チューニング: 【光の当たる方角】 ▼
        // lightDir: (X, Y, Z) で光源の位置を指定します。
        // - X: 右(+) / 左(-)
        // - Y: 下(+) / 上(-) ※AndroidのY軸は下向き
        // - Z: 光の手前具合 (大きくすると正面から当たるようになる)
        float3 lightDir = normalize(float3(0.5, -0.5, 1.5)); 
        
        float flatDiffuse = lightDir.z; 
        float currentDiffuse = max(0.0, dot(normal, lightDir));
        
        // ▼ チューニング: 【影の濃さ】 ▼
        // shadowDarkness: 光が全く当たらない斜面の最も暗い部分の明るさ。
        // - 0.0に近づける: 影が漆黒（真っ黒）になります。
        // - 0.2など大きくする: 影が薄くなり、全体的に明るくなります。
        float shadowDarkness = 0.0;
        
        // 面の明るさを計算（平らな面は1.0、斜面はshadowDarknessまで落ちる）
        float brightness = shadowDarkness + currentDiffuse * ((1.0 - shadowDarkness) / flatDiffuse);
        
        // ▼ チューニング: 【ハイライト（白飛び）の鋭さと強さ】 ▼
        float3 viewDir = float3(0.0, 0.0, 1.0); 
        float3 halfDir = normalize(lightDir + viewDir);

        // shininess: 反射の鋭さ。
        // - 大きくする(例: 150.0): ジェルやガラスのように、点が鋭く光ります。
        // - 小さくする(例: 20.0): ゴム素材のように、ハイライトが広くぼやけます。
        float shininess = 120.0;

        // specularの係数(* 1.2): ハイライトの「白さ」の強さ。
        // - 大きくする(例: 1.5): より強烈に白飛びします。
        float specular = pow(max(0.0, dot(normal, halfDir)), shininess) * 1.2;
        
        // 最終合成：ベースカラー × 計算した明るさ ＋ ハイライト
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
    modifier: Modifier = Modifier
) {
    val paint = remember(sdfTextureBitmap, baseColor, scaleFactor) {
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
    clearInner: Boolean = false
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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun TextItemContent(
    decoration: Decoration.Text,
    textSize: TextUnit,
    modifier: Modifier = Modifier,
    isPuffyEnabled: Boolean = decoration.isPuffyEnabled
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val textColor = decoration.color
    val strokeColor = decoration.strokeColor
    val secondBorderColor = decoration.secondBorderColor
    val secondBorderWidth = decoration.secondBorderWidth

    val layoutResult = remember(decoration.text, decoration.font, decoration.width, textSize) {
        measurer.measure(
            text = AnnotatedString(decoration.text),
            style = TextStyle(
                fontFamily = decoration.font.value,
                fontWeight = FontWeight(decoration.width),
                fontSize = textSize,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }

    val maxStroke = decoration.strokeWidth + decoration.secondBorderWidth
    val boxSize = with(density) {
        Size(layoutResult.size.width + maxStroke, layoutResult.size.height + maxStroke).toDpSize()
    }

    var fillSdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var strokeSdfBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // ▼ チューニング: 【描画の解像度（綺麗さ）】 ▼
    // scaleFactor: 画像を何倍のサイズで内部生成して縮小表示するか（スーパーサンプリング）。
    // - 大きくする(例: 3.0f): 文字の輪郭や影のギザギザがより滑らかになりますが、処理は重くなります。
    // - 小さくする(例: 1.0f): 処理は軽いですが、画質が荒くなります。
    val scaleFactor = 3.0f

    val strokeDrawStyle = remember(decoration.strokeWidth) {
        Stroke(width = decoration.strokeWidth, join = StrokeJoin.Round)
    }

    LaunchedEffect(layoutResult, decoration.strokeWidth, isPuffyEnabled, maxStroke) {
        if (isPuffyEnabled) {
            val fillMaskBitmap =
                createTextMaskBitmap(layoutResult, density, Fill, scaleFactor, maxStroke)
            fillSdfBitmap = generateSdfTexture(fillMaskBitmap)

            if (decoration.strokeWidth > 0f) {
                val strokeMaskBitmap =
                    createTextMaskBitmap(
                        layoutResult,
                        density,
                        strokeDrawStyle,
                        scaleFactor,
                        maxStroke,
                        clearInner = true
                    )
                strokeSdfBitmap = generateSdfTexture(strokeMaskBitmap)
            } else {
                strokeSdfBitmap = null
            }
        } else {
            fillSdfBitmap = null
            strokeSdfBitmap = null
        }
    }

    Box(
        modifier = modifier.size(boxSize)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            translate(maxStroke / 2f, maxStroke / 2f) {
                if (secondBorderWidth > 0f) {
                    drawText(
                        textLayoutResult = layoutResult,
                        drawStyle = Stroke(
                            width = decoration.strokeWidth + secondBorderWidth,
                            join = StrokeJoin.Round
                        ),
                        color = secondBorderColor,
                    )
                }

                if (!isPuffyEnabled || strokeSdfBitmap == null) {
                    drawText(
                        textLayoutResult = layoutResult,
                        drawStyle = Stroke(width = decoration.strokeWidth, join = StrokeJoin.Round),
                        color = strokeColor,
                    )
                }

                if (!isPuffyEnabled || fillSdfBitmap == null) {
                    drawText(
                        textLayoutResult = layoutResult,
                        drawStyle = Fill,
                        color = textColor,
                    )
                }
            }
        }

        if (isPuffyEnabled) {
            if (strokeSdfBitmap != null) {
                PuffyTextRenderer(
                    sdfTextureBitmap = strokeSdfBitmap!!,
                    baseColor = strokeColor,
                    scaleFactor = scaleFactor,
                    modifier = Modifier.matchParentSize()
                )
            }
            if (fillSdfBitmap != null) {
                PuffyTextRenderer(
                    sdfTextureBitmap = fillSdfBitmap!!,
                    baseColor = textColor,
                    scaleFactor = scaleFactor,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

// region TextItemContent Previews

private val previewTextDecoration = Decoration.Text(
    id = "preview-text-1",
    text = "推し活最高！",
    font = FontFamilies.HACHI_MARU_POP,
    color = Color.White,
    strokeColor = Color.Magenta,
    strokeWidth = 30f,
    secondBorderColor = Color.White,
    secondBorderWidth = 0f,
    width = FontWeight.W900.weight,
)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TextItemContentPreview() {
    FansaUchiwaTheme {
        TextItemContent(
            decoration = previewTextDecoration,
            textSize = 48.sp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TextItemContentWithSecondBorderPreview() {
    FansaUchiwaTheme {
        TextItemContent(
            decoration = previewTextDecoration.copy(
                secondBorderWidth = 10f,
                secondBorderColor = Color.Cyan
            ),
            textSize = 48.sp,
        )
    }
}

// endregion
