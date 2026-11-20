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
import com.example.skilledworkershireapp.databinding.ActivityMainBinding
import com.example.skilledworkershireapp.model.Worker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: WorkerAdapter
    private var allWorkers = mutableListOf<Worker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupGreeting()
        setupRecyclerView()
        setupCategories()
        setupSearch()
        setupSignOut()
    }

    override fun onResume() {
        super.onResume()
        loadWorkers()
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good Morning 👋"
            hour < 17 -> "Good Afternoon 👋"
            else      -> "Good Evening 👋"
        }

        val uid = auth.currentUser?.uid
        if (uid != null) {
            database.getReference("users")
                .child(uid)
                .child("fullName")
                .get()
                .addOnSuccessListener { snap ->
                    val name = snap.value?.toString()?.trim() ?: "there"
                    binding.tvUserName.text = "Hello, $name!"
                }
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

    private fun setupCategories() {
        val categories = mapOf(
            binding.catElectrician to "Electrician",
            binding.catPlumber     to "Plumber",
            binding.catPainter     to "Painter",
            binding.catCarpenter   to "Carpenter",
            binding.catMechanic    to "Mechanic",
            binding.catCleaner     to "Cleaner",
            binding.catMason       to "Mason",
            binding.catHvac        to "HVAC"
        )

        categories.forEach { (view, category) ->
            view.setOnClickListener {
                val intent = Intent(this, WorkerListActivity::class.java)
                intent.putExtra("category", category)
                startActivity(intent)
            }
        }
    }

    private fun setupSearch() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}
            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                filterWorkers()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etSearch.addTextChangedListener(watcher)
        binding.etCity.addTextChangedListener(watcher)
    }

    private fun filterWorkers() {
        val query = binding.etSearch.text.toString().trim()
        val city = binding.etCity.text.toString().trim()

        val filtered = allWorkers.filter { worker ->
            val matchesSearch = query.isEmpty() ||
                    worker.displayName.contains(query, ignoreCase = true) ||
                    worker.category.contains(query, ignoreCase = true)

            val matchesCity = city.isEmpty() ||
                    worker.location.contains(city, ignoreCase = true)

            matchesSearch && matchesCity
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvWorkers.visibility  = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvWorkers.visibility  = View.VISIBLE
        }
    }

    private fun loadWorkers() {
        database.getReference("workers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
                    filterWorkers()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to load workers",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
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
}
