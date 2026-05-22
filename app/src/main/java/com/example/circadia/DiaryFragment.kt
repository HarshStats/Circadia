package com.example.circadia

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.circadia.database.SleepDatabase
import com.example.circadia.database.SleepNight
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryFragment : Fragment() {

    private lateinit var adapter: SleepNightAdapter
    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary, container, false)

        barChart = view.findViewById(R.id.sleepChart)
        setupChartStyling()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvSleepHistory)
        adapter = SleepNightAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onResume() {
        super.onResume()
        loadSleepData()
    }

    private fun loadSleepData() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val databaseDao = SleepDatabase.getInstance(requireContext()).sleepDatabaseDao
            val history = databaseDao.getAllNights()

            withContext(Dispatchers.Main) {
                adapter.submitList(history)
                populateChart(history)
            }
        }
    }

    private fun setupChartStyling() {
        // Remove messy default backgrounds and borders
        barChart.setDrawGridBackground(false)
        barChart.setDrawBorders(false)
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setScaleEnabled(false) // Prevent ugly zooming

        // Style the X Axis (Bottom)
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_gray)
        xAxis.textSize = 12f
        xAxis.setDrawAxisLine(false)
        xAxis.yOffset = 10f

        // Style the Y Axis (Left)
        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_gray)
        leftAxis.setDrawAxisLine(false)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 100f // Efficiency is 0-100%

        // Disable the Y Axis (Right)
        barChart.axisRight.isEnabled = false
    }

    private fun populateChart(history: List<SleepNight>) {
        if (history.isEmpty()) return

        // Take the 7 most recent nights and reverse them so oldest is on the left
        val recentNights = history.take(7).reversed()
        val entries = ArrayList<BarEntry>()
        val dayLabels = ArrayList<String>()

        val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault()) // "Mon", "Tue"

        for ((index, night) in recentNights.withIndex()) {
            // Plot efficiency percentage on the Y axis
            entries.add(BarEntry(index.toFloat(), night.sleepEfficiencyPercentage.toFloat()))
            dayLabels.add(dayFormatter.format(Date(night.startTimeMilli)))
        }

        // Apply custom Day labels to the X-Axis
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(dayLabels)
        barChart.xAxis.labelCount = dayLabels.size

        // Create the dataset and style the bars
        val dataSet = BarDataSet(entries, "Efficiency")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.neon_blue)
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        dataSet.valueTextSize = 10f

        // Hide value text if it looks too cluttered, by setting text size to 0 or color to transparent
        // dataSet.setDrawValues(false)

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f // Make bars slim and modern

        barChart.data = barData

        // Smooth entrance animation
        barChart.animateY(1000)
        barChart.invalidate() // Refresh the chart
    }
}