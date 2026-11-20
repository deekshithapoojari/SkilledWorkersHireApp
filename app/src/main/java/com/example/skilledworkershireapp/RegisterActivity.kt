package com.example.skilledworkershireapp

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.skilledworkershireapp.databinding.ActivityRegisterBinding
import com.example.skilledworkershireapp.utils.Validator
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var isUserRole = true
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupBackButton()
        setupRoleSelection()
        setupPhoneField()
        setupPasswordToggle()
        setupRegister()
        setupSignInLink()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
        }
    }

    private fun setupRoleSelection() {
        binding.roleUser.setOnClickListener {
            isUserRole = true
            binding.roleUser.setBackgroundResource(R.drawable.bg_role_card_selected)
            binding.roleWorker.setBackgroundResource(R.drawable.bg_role_card)
            binding.tvWorkerLabel.setTextColor(getColor(R.color.text_hint))
        }

        binding.roleWorker.setOnClickListener {
            isUserRole = false
            binding.roleWorker.setBackgroundResource(R.drawable.bg_role_card_selected)
            binding.roleUser.setBackgroundResource(R.drawable.bg_role_card)
            binding.tvWorkerLabel.setTextColor(getColor(R.color.brand_green_dark))
        }
    }

    private fun setupPhoneField() {
        binding.etPhone.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.etPhone.text.isEmpty()) {
                binding.etPhone.setText("+91")
                binding.etPhone.setSelection(3)
            }
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

    private fun setupRegister() {
        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            // Name validation
            if (fullName.isEmpty()) {
                binding.etFullName.error = "Name is required"
                binding.etFullName.requestFocus()
                return@setOnClickListener
            }
            if (!Validator.isValidName(fullName)) {
                binding.etFullName.error = "Name must contain letters only"
                binding.etFullName.requestFocus()
                return@setOnClickListener
            }

            // Email validation
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

            // Phone validation
            if (phone.isEmpty()) {
                binding.etPhone.error = "Phone is required"
                binding.etPhone.requestFocus()
                return@setOnClickListener
            }
            if (!Validator.isValidPhone(phone)) {
                binding.etPhone.error = "Enter valid 10 digit number"
                binding.etPhone.requestFocus()
                return@setOnClickListener
            }

            // Password validation
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
            if (password != confirmPassword) {
                binding.etConfirmPassword.error = "Passwords do not match"
                binding.etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            showLoading(true)

            // Format phone before saving
            val formattedPhone = Validator.formatPhone(phone)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: ""
                    val role = if (isUserRole) "user" else "worker"
                    saveUserToDatabase(userId, fullName, email, formattedPhone, role)
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    val errorMsg = e.message?.lowercase() ?: ""
                    val msg = when {
                        e is FirebaseAuthUserCollisionException ||
                        errorMsg.contains("already in use") ||
                        errorMsg.contains("email-already-in-use") ->
                            "Email already registered. Please sign in."
                        e is FirebaseNetworkException ||
                        errorMsg.contains("network") ||
                        errorMsg.contains("connection") ->
                            "No internet connection"
                        else -> "Registration failed: ${e.localizedMessage ?: "Try again."}"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun saveUserToDatabase(
        userId: String,
        fullName: String,
        email: String,
        phone: String,
        role: String
    ) {
        val task = if (role == "worker") {
            val workerData = mapOf(
                "id" to userId,
                "fullName" to fullName,
                "name" to fullName,
                "email" to email,
                "phone" to phone,
                "role" to "worker",
                "category" to "",
                "location" to "",
                "experience" to "",
                "isAvailable" to true,
                "isActive" to true,
                "createdAt" to System.currentTimeMillis()
            )
            database.getReference("workers").child(userId).setValue(workerData)
        } else {
            val userData = mapOf(
                "id" to userId,
                "fullName" to fullName,
                "email" to email,
                "phone" to phone,
                "role" to "user",
                "createdAt" to System.currentTimeMillis()
            )
            database.getReference("users").child(userId).setValue(userData)
        }

        task.addOnSuccessListener {
            showLoading(false)
            Toast.makeText(
                this,
                "Account created successfully!",
                Toast.LENGTH_SHORT
            ).show()

            // Redirect: worker -> WorkerDashboard, user -> Main
            val intent = if (role == "worker") {
                Intent(this, WorkerDashboardActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }.addOnFailureListener {
            showLoading(false)
            Toast.makeText(
                this,
                "Failed to save profile. Try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupSignInLink() {
        binding.tvSignIn.setOnClickListener {
            finish()
            overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false
            binding.tvRegisterText.text = "Creating account..."
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnRegister.isEnabled = true
            binding.tvRegisterText.text = "Create Account"
        }
    }
}
