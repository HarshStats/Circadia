package com.example.circadia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.circadia.database.SleepNight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SleepNightAdapter(private var data: List<SleepNight> = listOf()) :
    RecyclerView.Adapter<SleepNightAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val tvEfficiency: TextView = view.findViewById(R.id.tvEfficiency)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_sleep_night, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val night = data[position]

        // Format Date
        val dateFormater = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.tvDate.text = dateFormater.format(Date(night.startTimeMilli))

        // Calculate and Format Duration
        val durationMillis = night.endTimeMilli - night.startTimeMilli
        val hours = (durationMillis / (1000 * 60 * 60))
        val minutes = (durationMillis / (1000 * 60)) % 60
        holder.tvDuration.text = "${hours}h ${minutes}m"

        // Format Efficiency
        holder.tvEfficiency.text = "${night.sleepEfficiencyPercentage}%"
    }

    override fun getItemCount() = data.size

    fun submitList(newList: List<SleepNight>) {
        data = newList
        notifyDataSetChanged()
    }
}