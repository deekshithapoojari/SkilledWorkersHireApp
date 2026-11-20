package com.example.skilledworkershireapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.skilledworkershireapp.databinding.ActivityWorkerDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class WorkerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var isAvailable = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupGreeting()
        loadWorkerProfile()
        setupToggleAvailability()
        setupEditProfile()
        setupSignOut()
        setupSettings()
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good Morning 👋"
            hour < 17 -> "Good Afternoon 👋"
            else      -> "Good Evening 👋"
        }
    }

    private fun loadWorkerProfile() {
        val uid = auth.currentUser?.uid ?: return

        database.getReference("workers")
            .child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return

                    val fullName = snapshot.child("fullName").value?.toString()?.trim()
                    val legacyName = snapshot.child("name").value?.toString()?.trim()
                    val name = if (!fullName.isNullOrEmpty()) fullName
                               else if (!legacyName.isNullOrEmpty()) legacyName
                               else "Worker"

                    val category = snapshot.child("category").value?.toString()?.trim().let {
                        if (it.isNullOrEmpty()) "Not set" else it
                    }
                    val phone = snapshot.child("phone").value?.toString()?.trim().let {
                        if (it.isNullOrEmpty()) "Not set" else it
                    }
                    val location = snapshot.child("location").value?.toString()?.trim().let {
                        if (it.isNullOrEmpty()) "Not set" else it
                    }
                    val experience = snapshot.child("experience").value?.toString()?.trim().let {
                        if (it.isNullOrEmpty()) "Not set" else it
                    }
                    isAvailable = snapshot.child("isAvailable").value as? Boolean ?: true

                    binding.tvWorkerName.text = name
                    binding.tvCategory.text = category
                    binding.tvPhone.text = phone
                    binding.tvLocation.text = location
                    binding.tvExperience.text = experience

                    updateAvailabilityUI()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@WorkerDashboardActivity,
                        "Failed to load profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setupToggleAvailability() {
        binding.tvToggleAvailability.setOnClickListener {
            isAvailable = !isAvailable
            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            database.getReference("workers")
                .child(uid)
                .child("isAvailable")
                .setValue(isAvailable)
                .addOnSuccessListener {
                    updateAvailabilityUI()
                    val status = if (isAvailable) "Available" else "Busy"
                    Toast.makeText(this, "Status: $status", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateAvailabilityUI() {
        if (isAvailable) {
            binding.tvAvailabilityStatus.text = "● Available"
            binding.tvAvailabilityStatus.setTextColor(0xFF00DC82.toInt())
        } else {
            binding.tvAvailabilityStatus.text = "● Busy"
            binding.tvAvailabilityStatus.setTextColor(0xFFFF4444.toInt())
        }
    }

    private fun setupEditProfile() {
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, AddEditWorkerActivity::class.java))
        }
        binding.btnCompleteProfile.setOnClickListener {
            startActivity(Intent(this, AddEditWorkerActivity::class.java))
        }
    }

    private fun setupSignOut() {
        binding.btnSignOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }
    }

    private fun setupSettings() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
        }
    }
}
