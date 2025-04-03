package com.reminderwater.Dashbord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.reminderwater.Dashboard.Data.WaterRepository
import com.reminderwater.Dashboard.Data.WaterViewModel
import com.reminderwater.R
import java.util.Calendar

class Water : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvWaterProgress: TextView
    private lateinit var tvCurrentConsumption: TextView
    private lateinit var progressWater: ProgressBar
    private lateinit var waterGlassView: WaterGlassView

    private val viewModel: WaterViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val repository = WaterRepository()
                return WaterViewModel(repository) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_water, container, false)
        initializeViews(view)
        setupBottomNavigation(view)
        setupButtons(view)
        setupObservers()
        fetchUserDataAndSetGreeting()

        return view
    }

    private fun initializeViews(view: View) {
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvWaterProgress = view.findViewById(R.id.tvWaterProgress)
        tvCurrentConsumption = view.findViewById(R.id.tvCurrentConsumption)
        progressWater = view.findViewById(R.id.progressWater)
        waterGlassView = view.findViewById(R.id.waterGlassView)
    }

    private fun setupBottomNavigation(view: View) {
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_2 -> navigateTo(R.id.action_water_to_analysis)
                R.id.page_3 -> navigateTo(R.id.action_water_to_notification)
                R.id.page_4 -> navigateTo(R.id.action_water_to_profile)
                else -> true
            }
        }

        val menuItemId = bottomNavigationView.menu.findItem(R.id.page_3).itemId
        val badge = bottomNavigationView.getOrCreateBadge(menuItemId)
        badge.isVisible = true
        badge.number = 1
    }

    private fun navigateTo(actionId: Int): Boolean {
        findNavController().navigate(actionId)
        return true
    }

    private fun setupButtons(view: View) {
        view.findViewById<Button>(R.id.btnAddWater).setOnClickListener {
            viewModel.addWater(250)
        }
    }

    private fun setupObservers() {
        viewModel.dailyWaterGoal.observe(viewLifecycleOwner) { goal ->
            updateWaterProgress(viewModel.currentConsumption.value ?: 0, goal)
        }

        viewModel.currentConsumption.observe(viewLifecycleOwner) { consumption ->
            updateWaterProgress(consumption, viewModel.dailyWaterGoal.value ?: 2000)
            updateWaterGlassView(consumption, viewModel.dailyWaterGoal.value ?: 2000)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            showToast(message)
        }
    }

    private fun fetchUserDataAndSetGreeting() {
        viewModel.fetchUserData()

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            setGreeting(name)
        }
    }

    private fun setGreeting(userName: String) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            currentHour in 5..11 -> "Good Morning"
            currentHour in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }
        tvGreeting.text = "$greeting,"
    }

    private fun updateWaterProgress(currentConsumption: Int, dailyGoal: Int) {
        if (dailyGoal > 0) {
            progressWater.progress = (currentConsumption.toFloat() / dailyGoal * 100).toInt()
        }
        tvWaterProgress.text = "${currentConsumption}ml / ${dailyGoal}ml"
        tvCurrentConsumption.text = "Current: ${currentConsumption}ml"
    }

    private fun updateWaterGlassView(currentConsumption: Int, dailyGoal: Int) {
        if (dailyGoal > 0) {
            val waterLevel = currentConsumption.toFloat() / dailyGoal
            waterGlassView.setWaterLevel(waterLevel, animate = true)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}