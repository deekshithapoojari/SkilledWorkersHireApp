package com.example.skilledworkershireapp

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.skilledworkershireapp.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var isUserTab = true
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupTabs()
        setupPasswordToggle()
        setupSignIn()
        setupForgotPassword()
        setupRegisterLink()
    }

    private fun setupTabs() {
        binding.tabUser.setOnClickListener {
            isUserTab = true
            binding.tabUser.setBackgroundResource(R.drawable.bg_tab_active)
            binding.tabUser.setTextColor(getColor(R.color.text_primary))
            binding.tabWorker.setBackgroundResource(R.drawable.bg_tab_inactive)
            binding.tabWorker.setTextColor(getColor(R.color.text_hint))
        }

        binding.tabWorker.setOnClickListener {
            isUserTab = false
            binding.tabWorker.setBackgroundResource(R.drawable.bg_tab_active)
            binding.tabWorker.setTextColor(getColor(R.color.text_primary))
            binding.tabUser.setBackgroundResource(R.drawable.bg_tab_inactive)
            binding.tabUser.setTextColor(getColor(R.color.text_hint))
        }
    }

    private fun setupPasswordToggle() {
        binding.tvTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.etPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                binding.tvTogglePassword.text = "🙈"
            } else {
                binding.etPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.tvTogglePassword.text = "👁️"
            }
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }
    }

    private fun setupSignIn() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validation
            if (email.isEmpty()) {
                binding.etEmail.error = "Email is required"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Enter a valid email"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.etPassword.error = "Password is required"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.etPassword.error = "Minimum 6 characters"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }

            // Show loading
            showLoading(true)

            // Firebase sign in
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    showLoading(false)
                    val userId = result.user?.uid ?: ""

                    // Check if worker or user
                    database.getReference("workers")
                        .child(userId)
                        .get()
                        .addOnSuccessListener { workerSnap ->
                            if (workerSnap.exists()) {
                                // For workers, set isActive = true on login
                                workerSnap.ref.child("isActive").setValue(true)
                                val intent = Intent(this, WorkerDashboardActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                startActivity(intent)
                            } else {
                                val intent = Intent(this, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                startActivity(intent)
                            }
                            overridePendingTransition(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                            )
                        }
                        .addOnFailureListener {
                            // Default to customer Main if workers check fails
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            overridePendingTransition(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                            )
                        }
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    val errorMsg = e.message?.lowercase() ?: ""
                    val msg = when {
                        e is FirebaseAuthInvalidUserException ||
                        errorMsg.contains("no user record") ||
                        errorMsg.contains("user-not-found") ||
                        errorMsg.contains("user not found") ->
                            "No account found with this email"
                        e is FirebaseAuthInvalidCredentialsException ||
                        errorMsg.contains("password is invalid") ||
                        errorMsg.contains("invalid-credential") ||
                        errorMsg.contains("wrong password") ||
                        errorMsg.contains("credentials are incorrect") ->
                            "Incorrect password"
                        e is FirebaseNetworkException ||
                        errorMsg.contains("network") ||
                        errorMsg.contains("connection") ||
                        errorMsg.contains("timeout") ->
                            "No internet connection"
                        else -> "Sign in failed. Try again"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun setupForgotPassword() {
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(
                    this,
                    "Enter your email first",
                    Toast.LENGTH_SHORT
                ).show()
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(
                    this,
                    "Enter a valid email address",
                    Toast.LENGTH_SHORT
                ).show()
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Reset link sent to $email",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Failed to send reset email. Verify email and try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun setupRegisterLink() {
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSignIn.isEnabled = false
            binding.tvSignInText.text = "Signing in..."
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnSignIn.isEnabled = true
            binding.tvSignInText.text = "Sign In"
        }
    }
}
