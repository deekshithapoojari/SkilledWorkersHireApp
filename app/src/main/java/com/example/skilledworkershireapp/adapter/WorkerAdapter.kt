package com.example.skilledworkershireapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.skilledworkershireapp.databinding.ItemWorkerCardBinding
import com.example.skilledworkershireapp.model.Worker

class WorkerAdapter(
    private var workers: List<Worker>,
    private val onClick: (Worker) -> Unit
) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    inner class WorkerViewHolder(
        private val binding: ItemWorkerCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(worker: Worker) {
            binding.tvWorkerName.text = worker.displayName
            binding.tvWorkerCategory.text = worker.category
            binding.tvWorkerLocation.text = "📍 ${worker.location}"
            binding.tvWorkerExperience.text = if (worker.experience.isNotBlank()) "⭐ ${worker.experience}" else ""

            if (worker.isAvailable) {
                binding.tvAvailability.text = "Available"
                binding.tvAvailability.setTextColor(0xFF00A866.toInt())
                binding.tvAvailability.setBackgroundColor(0x1500DC82.toInt())
            } else {
                binding.tvAvailability.text = "Busy"
                binding.tvAvailability.setTextColor(0xFFFF4444.toInt())
                binding.tvAvailability.setBackgroundColor(0x15FF0000.toInt())
            }

            binding.root.setOnClickListener {
                onClick(worker)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WorkerViewHolder {
        val binding = ItemWorkerCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorkerViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: WorkerViewHolder,
        position: Int
    ) = holder.bind(workers[position])

    override fun getItemCount() = workers.size

    fun updateList(newList: List<Worker>) {
        workers = newList
        notifyDataSetChanged()
    }
}
