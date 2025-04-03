package com.reminderwater.Dashbord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.reminderwater.Dashboard.Data.WaterRepository
import com.reminderwater.Dashboard.Data.WaterViewModel
import com.reminderwater.Dashboard.WaterHistoryAdapter
import com.reminderwater.Dashbord.Data.WaterViewModelFactory
import com.reminderwater.R

class Analysis : Fragment() {

    private lateinit var viewModel: WaterViewModel
    private lateinit var combinedChart: CombinedChart
    private lateinit var waterHistoryRecyclerView: RecyclerView
    private lateinit var adapter: WaterHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_analysis, container, false)

        // Initialize ViewModel
        val repository = WaterRepository()
        val factory = WaterViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(WaterViewModel::class.java)

        // Initialize CombinedChart
        combinedChart = view.findViewById(R.id.combinedChart)
        setupCombinedChart()

        // Initialize RecyclerView
        waterHistoryRecyclerView = view.findViewById(R.id.waterHistoryRecyclerView)
        setupRecyclerView()

        // Observe water data
        observeWaterData()

        // Setup Bottom Navigation
        setupBottomNavigation(view)

        return view
    }

    private fun setupCombinedChart() {
        combinedChart.apply {
            description.isEnabled = false // Disable chart description
            setTouchEnabled(true) // Enable touch gestures
            setPinchZoom(true) // Enable pinch zoom
            setDrawGridBackground(false) // Disable grid background
            setNoDataText("No water consumption data available") // Text to display when no data is available
        }

        // Configure X-Axis
        val xAxis = combinedChart.xAxis
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM // Position X-Axis at the bottom
            setDrawGridLines(false) // Disable grid lines
        }
    }

    private fun setupRecyclerView() {
        adapter = WaterHistoryAdapter()
        waterHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        waterHistoryRecyclerView.adapter = adapter
    }

    private fun observeWaterData() {
        viewModel.getWaterHistory().observe(viewLifecycleOwner) { history ->
            // Update RecyclerView
            adapter.submitList(history)

            // Prepare data for the chart
            val entries = history.mapIndexed { index, entry ->
                Entry(index.toFloat(), entry.amount.toFloat())
            }

            // Update the chart
            updateChart(entries)
        }
    }

    private fun updateChart(entries: List<Entry>) {
        val dataSet = LineDataSet(entries, "Water Consumption").apply {
            // Use theme colors or default colors
            color = ContextCompat.getColor(requireContext(), R.color.chart_line_color) // Line color
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.chart_value_text_color) // Value text color
            lineWidth = 2f // Line width
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.chart_circle_color)) // Circle color
            setDrawCircleHole(false) // Disable circle hole
        }

        // Create LineData and wrap it in CombinedData
        val lineData = LineData(dataSet)
        val combinedData = CombinedData()
        combinedData.setData(lineData)

        // Update chart data
        combinedChart.data = combinedData
        combinedChart.invalidate() // Refresh the chart
    }

    private fun setupBottomNavigation(view: View) {
        val bottomNavigationView: BottomNavigationView = view.findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_1 -> {
                    findNavController().navigate(R.id.action_analysis_to_water)
                    true
                } R.id.page_2 -> {
                    true
                }
                R.id.page_3 -> {
                    findNavController().navigate(R.id.action_analysis_to_notification)
                    true
                }
                R.id.page_4 -> {
                    findNavController().navigate(R.id.action_analysis_to_profile)
                    true
                }
                else -> false
            }
        }

        // Add a badge to the notifications tab (page_3)
        val menuItemId = bottomNavigationView.menu.findItem(R.id.page_3).itemId
        val badge = bottomNavigationView.getOrCreateBadge(menuItemId)
        badge.isVisible = true
        badge.number = 1 // Set badge number
    }
}