package com.example.skilledworkershireapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.skilledworkershireapp.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupBackButton()
        loadAvailabilityStatus()
        setupEditProfile()
        setupAvailability()
        setupSignOut()
        setupDelete()
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

    private fun loadAvailabilityStatus() {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("workers")
            .child(uid)
            .child("isAvailable")
            .get()
            .addOnSuccessListener { snap ->
                val isAvailable = snap.value as? Boolean ?: true
                binding.tvAvailabilityStatus.text =
                    if (isAvailable) "Available" else "Busy"
                binding.tvAvailabilityStatus.setTextColor(
                    if (isAvailable) 0xFF00A866.toInt()
                    else 0xFFFF4444.toInt()
                )
            }
    }

    private fun setupEditProfile() {
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, AddEditWorkerActivity::class.java))
        }
    }

    private fun setupAvailability() {
        binding.btnAvailability.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val current = binding.tvAvailabilityStatus.text == "Available"
            val newStatus = !current

            database.getReference("workers")
                .child(uid)
                .child("isAvailable")
                .setValue(newStatus)
                .addOnSuccessListener {
                    binding.tvAvailabilityStatus.text =
                        if (newStatus) "Available" else "Busy"
                    binding.tvAvailabilityStatus.setTextColor(
                        if (newStatus) 0xFF00A866.toInt()
                        else 0xFFFF4444.toInt()
                    )
                    Toast.makeText(
                        this,
                        if (newStatus) "Status updated: Available"
                        else "Status updated: Busy",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update availability", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupSignOut() {
        binding.btnSignOut.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ ->
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    overridePendingTransition(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupDelete() {
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("This will permanently delete your account and all your data. This cannot be undone!")
                .setPositiveButton("Delete") { _, _ ->
                    deleteAccount()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun deleteAccount() {
        val uid = auth.currentUser?.uid ?: return
        val user = auth.currentUser ?: return

        // Delete from Realtime Database first (workers node and users node if any)
        database.getReference("workers")
            .child(uid)
            .removeValue()
            .addOnSuccessListener {
                // Also clean up users/{uid} if present
                database.getReference("users").child(uid).removeValue()

                // Then delete from Firebase Auth
                user.delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Account deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Failed to delete. Please login again and retry.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to delete account. Try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
