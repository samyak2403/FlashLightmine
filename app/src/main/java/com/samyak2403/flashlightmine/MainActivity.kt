package com.samyak2403.flashlightmine

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.snackbar.Snackbar
import com.samyak2403.flashlightmine.databinding.ActivityMainBinding
import androidx.core.view.WindowCompat
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private var isFlashlightOn = false
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String

    private lateinit var binding: ActivityMainBinding
    
    // Pull rope animation variables
    private var initialY = 0f
    private var isDragging = false
    private val pullThreshold = 100f // Minimum pull distance to trigger toggle
    
    companion object {
        private const val PREFS_NAME = "FlashLightPrefs"
        private const val KEY_FIRST_LAUNCH = "isFirstLaunch"
    }

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

        // Setup pull rope animation on line_5 and lightBulb
        setupPullRopeAnimation()
        
        setupShakeListener()
        
        // Show tutorial on first launch
        showTutorialIfFirstLaunch()
    }
    
    private fun showTutorialIfFirstLaunch() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        
        if (isFirstLaunch) {
            // Wait for views to be laid out
            binding.lightBulb.post {
                showLightBulbTutorial()
            }
            
            // Mark as not first launch anymore
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        }
    }
    
    private fun showLightBulbTutorial() {
        TapTargetView.showFor(this,
            TapTarget.forView(binding.lightBulb, 
                "Pull to LightBulb Flashlight",
                "Drag the bulb down and release to turn the flashlight ON or OFF")
                .outerCircleColor(R.color.tutorial_outer_circle)
                .outerCircleAlpha(0.96f)
                .targetCircleColor(android.R.color.white)
                .titleTextSize(24)
                .titleTextColor(android.R.color.white)
                .descriptionTextSize(16)
                .descriptionTextColor(android.R.color.white)
                .textColor(android.R.color.white)
                .dimColor(android.R.color.black)
                .drawShadow(true)
                .cancelable(true)
                .tintTarget(false)
                .transparentTarget(true)
                .targetRadius(80),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
                    // Tutorial dismissed
                }
            }
        )
    }
    
    private fun setupPullRopeAnimation() {
        // Set pivot point to top of line_5 so it stretches from top
        binding.line5.pivotY = 0f
        
        // Get the original height of line_5 for calculating stretch offset
        binding.line5.post {
            val lineHeight = binding.line5.height.toFloat()
            
            // Make line_5 and lightBulb respond to touch for pull animation
            val pullViews = listOf(binding.line5, binding.lightBulb)
            
            pullViews.forEach { view ->
                view.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialY = event.rawY
                            isDragging = true
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (isDragging) {
                                val deltaY = event.rawY - initialY
                                // Only allow pulling down (positive deltaY)
                                if (deltaY > 0) {
                                    // Calculate stretch factor for line_5 (stays anchored at top, stretches down)
                                    val stretchFactor = 1f + (deltaY / lineHeight) * 0.5f
                                    binding.line5.scaleY = stretchFactor
                                    
                                    // Calculate how much the bottom of line_5 moved due to stretch
                                    // bottomOffset = lineHeight * (stretchFactor - 1)
                                    val bottomOffset = lineHeight * (stretchFactor - 1f)
                                    
                                    // Move lightBulb and ellipses to stay connected to rope end
                                    binding.lightBulb.translationY = bottomOffset
                                    binding.ellipse1.translationY = bottomOffset
                                    binding.ellipse2.translationY = bottomOffset
                                    binding.ellipse3.translationY = bottomOffset
                                    binding.ellipse4.translationY = bottomOffset
                                }
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            if (isDragging) {
                                val deltaY = event.rawY - initialY
                                isDragging = false
                                
                                // If pulled enough, toggle the flashlight
                                if (deltaY > pullThreshold) {
                                    toggleFlashlight()
                                    playLightBulbAnimation()
                                }
                                
                                // Snap back with spring animation
                                snapBackWithSpring()
                            }
                            true
                        }
                        else -> false
                    }
                }
            }
        }
    }
    
    private fun snapBackWithSpring() {
        // Spring animation for line_5 scaleY (back to normal height)
        val springLineScale = SpringAnimation(binding.line5, SpringAnimation.SCALE_Y, 1f).apply {
            spring.stiffness = SpringForce.STIFFNESS_MEDIUM
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        }
        
        // Spring animation for lightBulb
        val springBulb = SpringAnimation(binding.lightBulb, SpringAnimation.TRANSLATION_Y, 0f).apply {
            spring.stiffness = SpringForce.STIFFNESS_MEDIUM
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        }
        
        // Spring animations for ellipses
        val springEllipse1 = SpringAnimation(binding.ellipse1, SpringAnimation.TRANSLATION_Y, 0f).apply {
            spring.stiffness = SpringForce.STIFFNESS_MEDIUM
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        }
        val springEllipse2 = SpringAnimation(binding.ellipse2, SpringAnimation.TRANSLATION_Y, 0f).apply {
            spring.stiffness = SpringForce.STIFFNESS_MEDIUM
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        }
        val springEllipse3 = SpringAnimation(binding.ellipse3, SpringAnimation.TRANSLATION_Y, 0f).apply {
            spring.stiffness = SpringForce.STIFFNESS_MEDIUM
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        }
        val springEllipse4 = SpringAnimation(binding.ellipse4, SpringAnimation.TRANSLATION_Y, 0f).apply {
            spring.stiffness = SpringForce.STIFFNESS_MEDIUM
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        }
        
        // Start all spring animations
        springLineScale.start()
        springBulb.start()
        springEllipse1.start()
        springEllipse2.start()
        springEllipse3.start()
        springEllipse4.start()
    }
    
    private fun playLightBulbAnimation() {
        // Scale pulse animation on lightBulb when toggled
        val scaleX = ObjectAnimator.ofFloat(binding.lightBulb, View.SCALE_X, 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.lightBulb, View.SCALE_Y, 1f, 1.2f, 1f)
        
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 300
            interpolator = OvershootInterpolator()
            start()
        }
    }

    private fun toggleFlashlight() {
        try {
            isFlashlightOn = !isFlashlightOn
            cameraManager.setTorchMode(cameraId, isFlashlightOn)

            // Update UI based on flashlight state
            binding.main.setBackgroundColor(if (isFlashlightOn) 0xFF332D2B.toInt() else 0xFF1E1E1E.toInt())

            binding.lightBulb.setImageResource(if (isFlashlightOn) R.drawable.bulb_on else R.drawable.bulb_off)
            binding.status.text = if (isFlashlightOn) "ON" else "OFF"

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
