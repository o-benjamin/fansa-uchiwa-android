package com.fansauchiwa.edit

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
    val appContext = LocalContext.current.applicationContext
    val currentOnShake = rememberUpdatedState(onShake)
    val sensorObserver = remember(appContext) {
        AndroidShakeSensorObserver(appContext)
    }
    val shakeDetectionUseCase = remember {
        ShakeDetectionUseCase()
    }

    DisposableEffect(sensorObserver, shakeDetectionUseCase) {
        sensorObserver.startObserving { accelerationVector ->
            if (shakeDetectionUseCase.shouldDispatchShake(accelerationVector)) {
                currentOnShake.value()
            }
        }
        onDispose {
            sensorObserver.stopObserving()
        }
    }
}

internal data class AccelerationVector(
    val x: Float,
    val y: Float,
    val z: Float
) {
    fun magnitude(): Float = sqrt(x * x + y * y + z * z)
}

internal class ShakeDetectionUseCase(
    private val shakeThreshold: Float = SHAKE_THRESHOLD,
    private val shakeDebounceMs: Long = SHAKE_DEBOUNCE_MS,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) {
    private var lastShakeTime = 0L

    fun shouldDispatchShake(accelerationVector: AccelerationVector): Boolean {
        val now = currentTimeMillis()
        if (accelerationVector.magnitude() <= shakeThreshold) {
            return false
        }
        if (now - lastShakeTime <= shakeDebounceMs) {
            return false
        }

        lastShakeTime = now
        return true
    }
}

private interface ShakeSensorObserver {
    fun startObserving(onAccelerationChanged: (AccelerationVector) -> Unit)
    fun stopObserving()
}

private class AndroidShakeSensorObserver(
    context: Context
) : ShakeSensorObserver, SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var onAccelerationChanged: ((AccelerationVector) -> Unit)? = null

    override fun startObserving(onAccelerationChanged: (AccelerationVector) -> Unit) {
        this.onAccelerationChanged = onAccelerationChanged
        val sensor = accelerometer ?: return
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun stopObserving() {
        onAccelerationChanged = null
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val values = event?.values ?: return
        if (values.size < 3) {
            return
        }

        onAccelerationChanged?.invoke(
            AccelerationVector(
                x = values[0],
                y = values[1],
                z = values[2]
            )
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
