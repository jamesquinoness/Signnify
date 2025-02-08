package com.example.signnifyhome

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.animation.PathInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.graphics.Color // Add this for color references

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {

    private lateinit var ripple1: View
    private lateinit var ripple2: View
    private lateinit var ripple3: View
    private lateinit var tapToVerify: TextView
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>

    private val handlerAnimation = Handler(Looper.getMainLooper())
    private lateinit var vibrator: Vibrator

    @SuppressLint("WrongViewCast", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Enable edge-to-edge if necessary
            enableEdgeToEdge()

            // Set the traditional XML layout as the content view
            setContentView(R.layout.activity_homepage) // Ensure you're using the correct layout file

            // Find the circle views from XML layout
            ripple1 = findViewById(R.id.ripples1)
            ripple2 = findViewById(R.id.ripples2)
            ripple3 = findViewById(R.id.ripples3)
            tapToVerify = findViewById(R.id.tapToVerify)

            // Start the animation pulse after the activity is created
            startPulse()

            // Initialize vibrator for haptic feedback
            vibrator = getSystemService(Vibrator::class.java)

            // Find the button to open the camera
            val mainButton: Button = findViewById(R.id.main_button)

            // Set the onClickListener for normal click
            mainButton.setOnClickListener {
                // Animate the button on click (scale down and up)
                val scaleDownX = ObjectAnimator.ofFloat(mainButton, "scaleX", 0.9f).apply { duration = 100 }
                val scaleDownY = ObjectAnimator.ofFloat(mainButton, "scaleY", 0.9f).apply { duration = 100 }

                val scaleUpX = ObjectAnimator.ofFloat(mainButton, "scaleX", 1f).apply { duration = 100 }
                val scaleUpY = ObjectAnimator.ofFloat(mainButton, "scaleY", 1f).apply { duration = 100 }

                // Start scale down animation
                scaleDownX.start()
                scaleDownY.start()

                // Start scale up animation after delay
                scaleUpX.startDelay = 100 // Delay before starting scale-up
                scaleUpY.startDelay = 100
                scaleUpX.start()
                scaleUpY.start()

                // Provide haptic feedback on click
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(50) // Legacy support for pre-O devices
                }

                // Proceed with checking the camera permission
                checkCameraPermissionAndOpen()
            }

            // Register the camera permission request result callback
            cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission is required to take a picture", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate", e)
        }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            // If permission is not granted, request it
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCamera() {
        // Create an intent to open the camera
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        // Check if there is a camera app available
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPulse() {
        // Start the pulsating animation with a delay for a domino effect
        handlerAnimation.postDelayed({ animateRipple(ripple1, 300) }, 300)
        handlerAnimation.postDelayed({ animateRipple(ripple2, 150) }, 150) // Delay for domino effect
        handlerAnimation.postDelayed({ animateRipple(ripple3, 0) }, 0)
        handlerAnimation.postDelayed({ changeTextColor(tapToVerify, 0) }, 0)
    }

    private fun animateRipple(view: View, delay: Long) {
        val customInterpolator = PathInterpolator(0.69f, 0.00f, 0.0f, 1.0f)
        val customfadeInterpolator = PathInterpolator(0.69f, 0.00f, .0f, 0.32f)

        // Animate scale and alpha with a 20% reduction in size and opacity.
        val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.48f).apply {
            duration = 1500
            startDelay = delay
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = customInterpolator  // Apply ease-in-out effect
        }

        val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.48f).apply {
            duration = 1500
            startDelay = delay
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = customInterpolator  // Apply ease-in-out effect
        }

        // Fade-out animation (alpha goes from 1 to 0)
        val fadeOutAnimator = ObjectAnimator.ofFloat(view, "alpha", 0.35f, 0f).apply {
            duration = 1500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = customfadeInterpolator  // Apply ease-in-out effect
        }

        scaleXAnimator.start()
        scaleYAnimator.start()
        fadeOutAnimator.start()
    }

    private fun changeTextColor(view: View, delay: Long) {
        val customInterpolator = PathInterpolator(0.69f, 0.00f, 0.0f, 1.0f)
        // If the view is a TextView, animate text color
        if (view is TextView) {
            val fadeTextToWhiteAnimator = ObjectAnimator.ofArgb(view, "textColor", Color.WHITE, view.currentTextColor).apply {
                duration = 1500
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
                interpolator = customInterpolator  // Apply ease-in-out effect
            }
            fadeTextToWhiteAnimator.start()
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }
}
