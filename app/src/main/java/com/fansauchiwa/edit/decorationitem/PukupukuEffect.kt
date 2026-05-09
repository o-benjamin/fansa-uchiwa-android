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

internal fun supportsPukuPukuEffect(sdkInt: Int = Build.VERSION.SDK_INT): Boolean {
    return sdkInt >= Build.VERSION_CODES.TIRAMISU
}

private const val STICKER_PUKUPUKU_SHADER = """
    uniform shader composable;

    half4 main(float2 fragCoord) {
        half4 source = composable.eval(fragCoord);
        float alpha = source.a;
        if (alpha <= 0.0) return half4(0.0);

        float step = 2.5;
        float hL = composable.eval(fragCoord + float2(-step, 0.0)).a;
        float hR = composable.eval(fragCoord + float2(step, 0.0)).a;
        float hT = composable.eval(fragCoord + float2(0.0, -step)).a;
        float hB = composable.eval(fragCoord + float2(0.0, step)).a;

        float3 normal = normalize(float3(hL - hR, hT - hB, 2.0));
        float3 lightDir = normalize(float3(0.5, -0.5, 1.5));
        float flatDiffuse = lightDir.z;
        float currentDiffuse = max(0.0, dot(normal, lightDir));
        float shadowDarkness = 0.0;
        float brightness = shadowDarkness + currentDiffuse * ((1.0 - shadowDarkness) / flatDiffuse);

        float3 viewDir = float3(0.0, 0.0, 1.0);
        float3 halfDir = normalize(lightDir + viewDir);
        float shininess = 300.0;
        float specular = pow(max(0.0, dot(normal, halfDir)), shininess) * 1.2;

        half3 baseColor = source.rgb / alpha;
        half3 finalColor = baseColor * brightness + half3(specular);
        return half4(finalColor * alpha, alpha);
    }
"""

internal fun Modifier.pukupukuEffect(isEnabled: Boolean): Modifier = composed {
    if (!isEnabled || !supportsPukuPukuEffect()) {
        return@composed this
    }

    val shader = remember { RuntimeShader(STICKER_PUKUPUKU_SHADER) }
    val renderEffect = remember(shader) {
        RenderEffect.createRuntimeShaderEffect(shader, "composable").asComposeRenderEffect()
    }
    graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
        this.renderEffect = renderEffect
    }
}
