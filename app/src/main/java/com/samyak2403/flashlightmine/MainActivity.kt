package com.samyak2403.flashlightmine

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import com.samyak2403.flashlightmine.databinding.ActivityMainBinding
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private var isFlashlightOn = false
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize CameraManager
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[0]

        // Toggle flashlight using Shake or On/Off Button
        binding.offButton.setOnClickListener {
            toggleFlashlight()
        }

        binding.shakeText.setOnClickListener {
            Snackbar.make(it, "Shake functionality can be implemented!", Snackbar.LENGTH_SHORT)
                .show()
        }

        setupShakeListener()
    }

    private fun toggleFlashlight() {
        try {
            isFlashlightOn = !isFlashlightOn
            cameraManager.setTorchMode(cameraId, isFlashlightOn)

            // Update UI based on flashlight state
            binding.main.setBackgroundColor(if (isFlashlightOn) 0xFF332D2B.toInt() else 0xFF1E1E1E.toInt())
            val statusBarColor = if (isFlashlightOn) 0xFF1E1E1E.toInt() else 0xFF121212.toInt()

            binding.lightBulb.setImageResource(if (isFlashlightOn) R.drawable.bulb_on else R.drawable.bulb_off)
            binding.status.text = if (isFlashlightOn) "ON" else "OFF"




            // Update status bar color
            window.statusBarColor = statusBarColor

            // Change ellipses tint dynamically
            setEllipsesTint(isFlashlightOn)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun setupShakeListener() {
        val sensorManager = getSystemService(SENSOR_SERVICE) as android.hardware.SensorManager
        val accelerometer =
            sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)

        val shakeListener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                if (event == null) return

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val gForce =
                    sqrt(x * x + y * y + z * z) / android.hardware.SensorManager.GRAVITY_EARTH
                if (gForce > 2.5) { // Shake threshold
                    toggleFlashlight()
                }
            }

            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {
                // Not used
            }
        }

        sensorManager.registerListener(
            shakeListener,
            accelerometer,
            android.hardware.SensorManager.SENSOR_DELAY_UI
        )
    }

    private fun setEllipsesTint(isFlashlightOn: Boolean) {
        val tintColor = if (isFlashlightOn) 0xFFFFA500.toInt() else 0xFF1E1E1E.toInt()
        binding.ellipse1.background.setTint(tintColor)
        binding.ellipse2.background.setTint(tintColor)
        binding.ellipse3.background.setTint(tintColor)
        binding.ellipse4.background.setTint(tintColor)
    }
}
