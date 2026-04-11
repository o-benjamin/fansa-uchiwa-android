package com.fansauchiwa.edit

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

private const val SHAKE_THRESHOLD = 12f
private const val SHAKE_DEBOUNCE_MS = 1000L

/**
 * デバイスのシェイク（振動）を検知するコンポーザブル
 * @param onShake シェイクが検知された時のコールバック
 */
@Composable
fun ShakeDetector(onShake: () -> Unit) {
    val context = LocalContext.current
    val currentOnShake by rememberUpdatedState(onShake)
    val accelerometerGateway = remember(context) {
        AndroidAccelerometerGateway(
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        )
    }
    val shakeDetectionUseCase = remember {
        ShakeDetectionUseCase(timeProvider = System::currentTimeMillis)
    }

    DisposableEffect(accelerometerGateway, shakeDetectionUseCase) {
        accelerometerGateway.start { sample ->
            if (shakeDetectionUseCase.shouldDetect(sample)) {
                currentOnShake()
            }
        }

        onDispose {
            accelerometerGateway.stop()
        }
    }
}

internal data class AccelerationSample(
    val x: Float,
    val y: Float,
    val z: Float
)

internal class ShakeDetectionUseCase(
    private val threshold: Float = SHAKE_THRESHOLD,
    private val debounceMs: Long = SHAKE_DEBOUNCE_MS,
    private val timeProvider: () -> Long
) {
    private var lastShakeTime = 0L

    fun shouldDetect(sample: AccelerationSample): Boolean {
        val acceleration = sqrt(sample.x * sample.x + sample.y * sample.y + sample.z * sample.z)
        val now = timeProvider()
        val isShake = acceleration > threshold && now - lastShakeTime > debounceMs
        if (isShake) {
            lastShakeTime = now
        }
        return isShake
    }
}

private interface AccelerometerGateway {
    fun start(onSampleChanged: (AccelerationSample) -> Unit)
    fun stop()
}

private class AndroidAccelerometerGateway(
    private val sensorManager: SensorManager
) : AccelerometerGateway {
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var listener: SensorEventListener? = null

    override fun start(onSampleChanged: (AccelerationSample) -> Unit) {
        if (listener != null || accelerometer == null) {
            return
        }

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val values = event?.values ?: return
                onSampleChanged(
                    AccelerationSample(
                        x = values[0],
                        y = values[1],
                        z = values[2]
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        listener = sensorListener
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun stop() {
        listener?.let(sensorManager::unregisterListener)
        listener = null
    }
}
