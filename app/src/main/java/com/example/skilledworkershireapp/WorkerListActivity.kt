package com.example.skilledworkershireapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skilledworkershireapp.adapter.WorkerAdapter
import com.example.skilledworkershireapp.databinding.ActivityWorkerListBinding
import com.example.skilledworkershireapp.model.Worker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WorkerListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerListBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: WorkerAdapter
    private var category = ""
    private var allWorkers = mutableListOf<Worker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        category = intent.getStringExtra("category") ?: ""

        binding.tvCategory.text = if (category.isNotEmpty()) "$category Workers" else "All Workers"

        setupBackButton()
        setupRecyclerView()
        setupCityFilter()
    }

    override fun onResume() {
        super.onResume()
        loadWorkers()
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

    private fun setupRecyclerView() {
        adapter = WorkerAdapter(emptyList()) { worker ->
            val intent = Intent(this, WorkerDetailActivity::class.java)
            intent.putExtra("workerId", worker.id)
            startActivity(intent)
        }
        binding.rvWorkers.layoutManager = LinearLayoutManager(this)
        binding.rvWorkers.adapter = adapter
    }

    private fun setupCityFilter() {
        binding.etCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}
            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                filterByCity(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterByCity(city: String) {
        val filtered = if (city.isEmpty()) {
            allWorkers
        } else {
            allWorkers.filter {
                it.location.contains(city, ignoreCase = true)
            }
        }
        adapter.updateList(filtered)
        binding.tvWorkerCount.text = "${filtered.size} workers found"

        if (filtered.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvWorkers.visibility  = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvWorkers.visibility  = View.VISIBLE
        }
    }

    private fun loadWorkers() {
        val query = if (category.isNotEmpty()) {
            database.getReference("workers")
                .orderByChild("category")
                .equalTo(category)
        } else {
            database.getReference("workers")
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allWorkers.clear()
                for (snap in snapshot.children) {
                    val worker = snap.getValue(Worker::class.java)
                    if (worker != null &&
                        worker.displayName.isNotBlank() &&
                        worker.category.isNotBlank() &&
                        worker.location.isNotBlank()
                    ) {
                        allWorkers.add(worker)
                    }
                }
                filterByCity(binding.etCity.text.toString().trim())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@WorkerListActivity,
                    "Failed to load workers",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
