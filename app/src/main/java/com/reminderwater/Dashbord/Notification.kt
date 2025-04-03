package com.reminderwater.Dashbord

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.reminderwater.Dashbord.Data.TimeSlot
import com.reminderwater.R
import java.util.concurrent.TimeUnit

class Notification : Fragment() {

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        checkNotificationPermission()

        setupRecyclerView(view)
        setupBottomNavigation(view)
        view.findViewById<View>(R.id.btnScheduleNotification).setOnClickListener {
            if (areNotificationsEnabled()) {
                scheduleNotification()
            } else {
                showPermissionDeniedMessage()
            }
        }

        return view
    }

    private fun setupRecyclerView(view: View) {
        val timeSlots = listOf(
            TimeSlot("08:00 AM", "250ml", "Pending"),
            TimeSlot("10:00 AM", "250ml", "Pending"),
            TimeSlot("12:00 PM", "250ml", "Pending"),
            TimeSlot("02:00 PM", "250ml", "Pending"),
            TimeSlot("04:00 PM", "250ml", "Pending"),
            TimeSlot("06:00 PM", "250ml", "Pending"),
            TimeSlot("08:00 PM", "250ml", "Pending")
        )

        val adapter = TimeSlotAdapter(timeSlots)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvTimeSlots)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupBottomNavigation(view: View) {
        val bottomNavigationView: BottomNavigationView = view.findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_1 -> {
                    findNavController().navigate(R.id.action_notification_to_water)
                    true
                }
                R.id.page_2 -> {
                    findNavController().navigate(R.id.action_notification_to_analysis)
                    true
                }
                R.id.page_3 -> {
                    true
                }
                R.id.page_4 -> {
                    findNavController().navigate(R.id.action_notification_to_profile)
                    true
                }
                else -> false
            }
        }

        // Add a badge to the notifications tab (page_3)
        val menuItemId = bottomNavigationView.menu.findItem(R.id.page_3).itemId
        val badge = bottomNavigationView.getOrCreateBadge(menuItemId)
        badge.isVisible = true
        badge.number = 1
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun showPermissionDeniedMessage() {
        AlertDialog.Builder(requireContext())
            .setTitle("Notification Permission Required")
            .setMessage("Please enable notifications in app settings to schedule reminders.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun scheduleNotification() {
        val workManager = WorkManager.getInstance(requireContext())

        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .build()

        workManager.enqueue(notificationWorkRequest)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleNotification()
            } else {
                showPermissionDeniedMessage()
            }
        }
    }
}