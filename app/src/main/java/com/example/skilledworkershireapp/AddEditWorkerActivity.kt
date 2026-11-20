package com.example.skilledworkershireapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.skilledworkershireapp.databinding.ActivityAddEditWorkerBinding
import com.example.skilledworkershireapp.utils.Validator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddEditWorkerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditWorkerBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val categories = listOf(
        "Electrician", "Plumber", "Painter",
        "Carpenter", "Mechanic", "Cleaner",
        "Mason", "HVAC"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditWorkerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupBackButton()
        setupCategorySpinner()
        loadExistingData()
        setupPhoneField()
        setupSaveButton()
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

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.spinnerCategory.adapter = adapter
    }

    private fun loadExistingData() {
        val uid = auth.currentUser?.uid ?: return

        database.getReference("workers")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) return@addOnSuccessListener

                val name = snapshot.child("fullName").value?.toString()?.trim()
                    ?: snapshot.child("name").value?.toString()?.trim()
                    ?: ""
                val category = snapshot.child("category").value?.toString()?.trim() ?: ""
                val phone = snapshot.child("phone").value?.toString()?.trim() ?: ""
                val location = snapshot.child("location").value?.toString()?.trim() ?: ""
                val experience = snapshot.child("experience").value?.toString()?.trim() ?: ""

                binding.etFullName.setText(name)
                binding.etPhone.setText(phone)
                binding.etLocation.setText(location)
                binding.etExperience.setText(experience)

                // Set spinner to saved category
                val index = categories.indexOf(category)
                if (index >= 0) {
                    binding.spinnerCategory.setSelection(index)
                }
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

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem?.toString() ?: ""
            val phone = binding.etPhone.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()
            val experience = binding.etExperience.text.toString().trim()

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

            // Category validation
            if (category.isEmpty()) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
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

            // Location validation
            if (location.isEmpty()) {
                binding.etLocation.error = "Location is required"
                binding.etLocation.requestFocus()
                return@setOnClickListener
            }
            if (!Validator.isValidLocation(location)) {
                binding.etLocation.error = "Letters, digits, spaces and commas only"
                binding.etLocation.requestFocus()
                return@setOnClickListener
            }

            // Experience validation
            if (experience.isEmpty()) {
                binding.etExperience.error = "Experience is required"
                binding.etExperience.requestFocus()
                return@setOnClickListener
            }
            if (!Validator.isValidExperience(experience)) {
                binding.etExperience.error = "Enter valid experience (e.g. 3 years)"
                binding.etExperience.requestFocus()
                return@setOnClickListener
            }

            showLoading(true)

            // Format phone before saving
            val formattedPhone = Validator.formatPhone(phone)
            saveProfile(fullName, category, formattedPhone, location, experience)
        }
    }

    private fun saveProfile(
        fullName: String,
        category: String,
        phone: String,
        location: String,
        experience: String
    ) {
        val uid = auth.currentUser?.uid ?: return

        val updates = mapOf<String, Any>(
            "fullName"   to fullName,
            "name"       to fullName,
            "category"   to category,
            "phone"      to phone,
            "location"   to location,
            "experience" to experience,
            "id"         to uid
        )

        database.getReference("workers")
            .child(uid)
            .updateChildren(updates)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Profile saved!",
                    Toast.LENGTH_SHORT
                ).show()
                // Go back to dashboard
                val intent = Intent(this, WorkerDashboardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Failed to save. Try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false
            binding.tvSaveText.text = "Saving..."
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true
            binding.tvSaveText.text = "Save Profile"
        }
    }
}
