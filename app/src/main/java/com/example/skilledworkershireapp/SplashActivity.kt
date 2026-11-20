package com.example.skilledworkershireapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.skilledworkershireapp.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        startAnimations()
    }

    private fun startAnimations() {

        // Logo pop in
        val logoScaleX = ObjectAnimator.ofFloat(
            binding.logoCard, "scaleX", 0.3f, 1f
        ).apply {
            duration = 700
            startDelay = 300
            interpolator = OvershootInterpolator(1.5f)
        }

        val logoScaleY = ObjectAnimator.ofFloat(
            binding.logoCard, "scaleY", 0.3f, 1f
        ).apply {
            duration = 700
            startDelay = 300
            interpolator = OvershootInterpolator(1.5f)
        }

        // Center content fade in
        val centerFade = ObjectAnimator.ofFloat(
            binding.centerContent, "alpha", 0f, 1f
        ).apply {
            duration = 400
            startDelay = 200
        }

        // App name fade + slide
        val nameFade = ObjectAnimator.ofFloat(
            binding.tvAppName, "alpha", 0f, 1f
        ).apply {
            duration = 500
            startDelay = 900
        }
        val nameSlide = ObjectAnimator.ofFloat(
            binding.tvAppName, "translationY", 30f, 0f
        ).apply {
            duration = 500
            startDelay = 900
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Tagline fade + slide
        val tagFade = ObjectAnimator.ofFloat(
            binding.tvTagline, "alpha", 0f, 1f
        ).apply {
            duration = 500
            startDelay = 1100
        }
        val tagSlide = ObjectAnimator.ofFloat(
            binding.tvTagline, "translationY", 20f, 0f
        ).apply {
            duration = 500
            startDelay = 1100
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Loading fade in
        val loadFade = ObjectAnimator.ofFloat(
            binding.loadingArea, "alpha", 0f, 1f
        ).apply {
            duration = 400
            startDelay = 1400
        }

        // Version fade in
        val versionFade = ObjectAnimator.ofFloat(
            binding.tvVersion, "alpha", 0f, 1f
        ).apply {
            duration = 400
            startDelay = 1600
        }

        // Play all together
        AnimatorSet().apply {
            playTogether(
                logoScaleX, logoScaleY,
                centerFade,
                nameFade, nameSlide,
                tagFade, tagSlide,
                loadFade, versionFade
            )
            start()
        }

        // Progress bar animation
        ValueAnimator.ofInt(0, 100).apply {
            duration = 2000
            startDelay = 1600
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                binding.progressBar.progress = it.animatedValue as Int
            }
            start()
        }

        // Navigate after 3 seconds
        binding.root.postDelayed({
            navigateNext()
        }, 3000)
    }

    private fun navigateNext() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            return
        }

        // Check if worker or user
        FirebaseDatabase.getInstance()
            .getReference("workers")
            .child(currentUser.uid)
            .get()
            .addOnSuccessListener { snap ->
                val intent = if (snap.exists()) {
                    Intent(this, WorkerDashboardActivity::class.java)
                } else {
                    Intent(this, MainActivity::class.java)
                }
                startActivity(intent)
                finish()
                overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }
            .addOnFailureListener {
                // If check fails go to Login
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
    }
}