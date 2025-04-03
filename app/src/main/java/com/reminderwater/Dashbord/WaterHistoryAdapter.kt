package com.reminderwater.Dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reminderwater.Dashbord.Data.WaterEntry
import com.reminderwater.R

class WaterHistoryAdapter : RecyclerView.Adapter<WaterHistoryAdapter.ViewHolder>() {

    private val historyList = mutableListOf<WaterEntry>()

    fun submitList(newList: List<WaterEntry>) {
        historyList.clear()
        historyList.addAll(newList)
        notifyItemRangeInserted(0, newList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_water_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = historyList[position]
        holder.apply {
            dateTextView.text = entry.date
            amountTextView.text = "${entry.amount} ml"
        }
    }

    override fun getItemCount() = historyList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.tvDate)
        val amountTextView: TextView = view.findViewById(R.id.tvAmount)
    }
}
