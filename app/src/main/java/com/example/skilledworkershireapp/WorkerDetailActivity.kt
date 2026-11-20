package com.example.skilledworkershireapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.skilledworkershireapp.databinding.ActivityWorkerDetailBinding
import com.example.skilledworkershireapp.model.Worker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WorkerDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerDetailBinding
    private lateinit var database: FirebaseDatabase
    private var workerPhone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()

        val workerId = intent.getStringExtra("workerId") ?: ""

        setupBackButton()
        loadWorkerDetails(workerId)
        setupCallButton()
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

    private fun loadWorkerDetails(workerId: String) {
        if (workerId.isEmpty()) {
            Toast.makeText(this, "Worker not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database.getReference("workers")
            .child(workerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val worker = snapshot.getValue(Worker::class.java)
                    if (worker != null) {
                        displayWorker(worker)
                    } else {
                        Toast.makeText(
                            this@WorkerDetailActivity,
                            "Worker not found",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@WorkerDetailActivity,
                        "Failed to load worker",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun displayWorker(worker: Worker) {
        workerPhone = worker.phone

        binding.tvWorkerName.text = worker.displayName
        binding.tvCategory.text   = worker.category
        binding.tvPhone.text      = if (worker.phone.isNotBlank()) worker.phone else "Not set"
        binding.tvLocation.text   = if (worker.location.isNotBlank()) worker.location else "Not set"
        binding.tvExperience.text = if (worker.experience.isNotBlank()) worker.experience else "Not set"

        if (worker.isAvailable) {
            binding.tvAvailability.text = "✅ Available"
            binding.tvAvailability.setTextColor(getColor(R.color.brand_green_dark))
        } else {
            binding.tvAvailability.text = "❌ Not Available"
            binding.tvAvailability.setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }

    private fun setupCallButton() {
        binding.btnCall.setOnClickListener {
            if (workerPhone.isEmpty()) {
                Toast.makeText(
                    this,
                    "Phone number not available",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$workerPhone")
            }
            startActivity(intent)
        }
    }
}
