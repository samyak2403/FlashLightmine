package com.samyak2403.flashlightmine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.samyak2403.flashlightmine.ui.FlashlightScreen
import com.samyak2403.flashlightmine.ui.TutorialOverlay
import com.samyak2403.flashlightmine.ui.theme.FlashLightmineTheme
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashLightmineTheme {
                FlashlightApp()
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun FlashlightApp() {
    val context = LocalContext.current
    val torch = remember { TorchController(context) }

    var isOn by remember { mutableStateOf(false) }

    val toggle: () -> Unit = {
        val next = !isOn
        val success = torch.setTorch(next)
        if (success) {
            isOn = next
        } else {
            Toast.makeText(context, "No flashlight available on this device", Toast.LENGTH_SHORT).show()
        }
    }

    // Turn torch off when leaving the composition to avoid a stuck light.
    DisposableEffect(Unit) {
        onDispose {
            torch.setTorch(false)
        }
    }

    // Shake to toggle — same 2.5g threshold as before.
    ShakeDetector(onShake = toggle)

    // First-launch tutorial overlay.
    val prefs = remember {
        context.getSharedPreferences(FlashlightPrefs.NAME, Context.MODE_PRIVATE)
    }
    var showTutorial by remember {
        mutableStateOf(prefs.getBoolean(FlashlightPrefs.KEY_FIRST_LAUNCH, true))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FlashlightScreen(
            isFlashlightOn = isOn,
            onToggle = toggle,
        )
        if (showTutorial) {
            TutorialOverlay(onDismiss = {
                showTutorial = false
                prefs.edit().putBoolean(FlashlightPrefs.KEY_FIRST_LAUNCH, false).apply()
            })
        }
    }
}

private object FlashlightPrefs {
    const val NAME = "FlashLightPrefs"
    const val KEY_FIRST_LAUNCH = "isFirstLaunch"
}

/**
 * Wraps [CameraManager] torch access. Returns false when the device has no
 * flash-capable camera or the operation fails.
 */
private class TorchController(context: Context) {
    private val cameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId: String? = findFlashCameraId()

    private fun findFlashCameraId(): String? = try {
        cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    } catch (e: CameraAccessException) {
        e.printStackTrace()
        null
    }

    fun setTorch(enabled: Boolean): Boolean {
        val id = cameraId ?: return false
        return try {
            cameraManager.setTorchMode(id, enabled)
            true
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            false
        }
    }
}

/**
 * Registers an accelerometer listener while the composable is in the composition
 * and invokes [onShake] when the acceleration exceeds ~2.5g.
 */
@androidx.compose.runtime.Composable
private fun ShakeDetector(onShake: () -> Unit) {
    val context = LocalContext.current
    val currentOnShake by rememberUpdatedState(onShake)

    DisposableEffect(Unit) {
        val sensorManager =
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: return@DisposableEffect onDispose { }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
                if (gForce > 2.5f) currentOnShake()
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI,
        )

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
}
