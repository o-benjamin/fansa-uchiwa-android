package com.fansauchiwa.edit.decorationitem

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

// Samples neighboring alpha values when estimating the sticker surface slope.
private const val STICKER_PUKUPUKU_SAMPLE_STEP = 2.5f

// Controls how steep the inflated surface appears along the Z axis.
private const val STICKER_PUKUPUKU_NORMAL_Z = 2.0f

// Fixed light direction reused from the text puffy effect tuning.
private const val STICKER_PUKUPUKU_LIGHT_X = 0.5f
private const val STICKER_PUKUPUKU_LIGHT_Y = -0.5f
private const val STICKER_PUKUPUKU_LIGHT_Z = 1.5f

// Keeps the darkest shadow at pure black like the text effect.
private const val STICKER_PUKUPUKU_SHADOW_DARKNESS = 0.0f

// Controls highlight sharpness and intensity for the glossy puffy look.
private const val STICKER_PUKUPUKU_SHININESS = 300.0f
private const val STICKER_PUKUPUKU_SPECULAR_INTENSITY = 1.2f

/**
 * Returns whether the current runtime can apply the AGSL-based puffy effect.
 *
 * The optional [sdkInt] parameter exists to keep this check easy to unit test.
 */
internal fun supportsPukuPukuEffect(sdkInt: Int = Build.VERSION.SDK_INT): Boolean {
    return sdkInt >= Build.VERSION_CODES.TIRAMISU
}

private val stickerPukupukuShader = """
    uniform shader composable;

    half4 main(float2 fragCoord) {
        half4 source = composable.eval(fragCoord);
        float alpha = source.a;
        if (alpha <= 0.0) return half4(0.0);

        float step = $STICKER_PUKUPUKU_SAMPLE_STEP;
        float hL = composable.eval(fragCoord + float2(-step, 0.0)).a;
        float hR = composable.eval(fragCoord + float2(step, 0.0)).a;
        float hT = composable.eval(fragCoord + float2(0.0, -step)).a;
        float hB = composable.eval(fragCoord + float2(0.0, step)).a;

        float3 normal = normalize(float3(hL - hR, hT - hB, $STICKER_PUKUPUKU_NORMAL_Z));
        float3 lightDir = normalize(float3(
            $STICKER_PUKUPUKU_LIGHT_X,
            $STICKER_PUKUPUKU_LIGHT_Y,
            $STICKER_PUKUPUKU_LIGHT_Z
        ));
        float flatDiffuse = lightDir.z;
        float currentDiffuse = max(0.0, dot(normal, lightDir));
        float shadowDarkness = $STICKER_PUKUPUKU_SHADOW_DARKNESS;
        float brightness = shadowDarkness + currentDiffuse * ((1.0 - shadowDarkness) / flatDiffuse);

        float3 viewDir = float3(0.0, 0.0, 1.0);
        float3 halfDir = normalize(lightDir + viewDir);
        float shininess = $STICKER_PUKUPUKU_SHININESS;
        float specular = pow(max(0.0, dot(normal, halfDir)), shininess) * $STICKER_PUKUPUKU_SPECULAR_INTENSITY;

        half3 baseColor = source.rgb / alpha;
        half3 finalColor = baseColor * brightness + half3(specular);
        return half4(finalColor * alpha, alpha);
    }
"""

/**
 * Applies the sticker-specific AGSL-based puffy render effect when [isEnabled] is true.
 *
 * The effect only runs on Android 13+ where `RuntimeShader` is available; on older
 * versions this modifier is a no-op so callers can reuse it safely.
 */
internal fun Modifier.pukupukuEffect(isEnabled: Boolean): Modifier = composed {
    if (!isEnabled || !supportsPukuPukuEffect()) {
        return@composed this
    }

    val shader = remember { RuntimeShader(stickerPukupukuShader) }
    val renderEffect = remember(shader) {
        RenderEffect.createRuntimeShaderEffect(shader, "composable").asComposeRenderEffect()
    }
    graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
        this.renderEffect = renderEffect
    }
}
