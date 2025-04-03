package com.reminderwater.Dashboard.Data  // Fixed typo: Dashbord -> Dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.reminderwater.Dashbord.Data.UserData
import com.reminderwater.Dashbord.Data.WaterEntry

class WaterRepository {
    private val database = FirebaseDatabase.getInstance(
        "https://reminder-water-23a84-default-rtdb.europe-west1.firebasedatabase.app/"
    ).reference

    private val auth = FirebaseAuth.getInstance()

    fun getUserData(
        onSuccess: (UserData) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onError(Exception("User not authenticated"))
            return
        }

        database.child("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                val userData = UserData(
                    dailyWaterGoal = snapshot.child("dailyWaterGoal").getValue(Int::class.java) ?: 2000,
                    currentConsumption = snapshot.child("currentConsumption").getValue(Int::class.java) ?: 0,
                    lastUpdatedDate = snapshot.child("lastUpdatedDate").getValue(String::class.java) ?: ""
                )
                onSuccess(userData)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    private val dummyHistory = listOf(
        WaterEntry("2025-03-05", 2000),
        WaterEntry("2025-03-06", 1800),
        WaterEntry("2025-03-07", 2500),
        WaterEntry("2025-03-08", 1900),
        WaterEntry("2025-03-09", 2300),
        WaterEntry("2025-03-10", 2100),
    )

    fun getWaterHistory(): LiveData<List<WaterEntry>> {
        val liveData = MutableLiveData<List<WaterEntry>>()
        liveData.postValue(dummyHistory)
        return liveData
    }

    fun updateDailyGoal(
        goal: Int,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onError(Exception("Unauthorized access"))
            return
        }

        database.child("users").child(userId).child("dailyWaterGoal")
            .setValue(goal)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete()
                } else {
                    onError(task.exception ?: Exception("Goal update failed"))
                }
            }
    }

    fun updateConsumption(
        amount: Int,
        date: String,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onError(Exception("Unauthorized access"))
            return
        }

        val updates = mapOf(
            "currentConsumption" to amount,
            "lastUpdatedDate" to date
        )

        database.child("users").child(userId)
            .updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete()
                } else {
                    onError(task.exception ?: Exception("Consumption update failed"))
                }
            }
    }
}
