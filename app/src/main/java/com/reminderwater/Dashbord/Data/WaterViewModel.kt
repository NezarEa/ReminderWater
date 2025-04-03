package com.reminderwater.Dashboard.Data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.reminderwater.Dashbord.Data.UserData
import com.reminderwater.Dashbord.Data.WaterEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WaterViewModel(private val repository: WaterRepository) : ViewModel() {

    val dailyWaterGoal = MutableLiveData<Int>()
    val currentConsumption = MutableLiveData<Int>()
    val userName = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()

    init {
        loadInitialData()
    }

    fun fetchUserData() {
        repository.getUserData(
            onSuccess = { userData ->
                dailyWaterGoal.value = userData.dailyWaterGoal
                currentConsumption.value = userData.currentConsumption
                userName.value = userData.name
            },
            onError = { exception ->
                errorMessage.value = "Failed to fetch user data: ${exception.message}"
                Log.e("WaterViewModel", "Error fetching user data", exception)
            }
        )
    }

    fun getWaterHistory(): LiveData<List<WaterEntry>> {
        return repository.getWaterHistory()
    }

    private fun loadInitialData() {
        repository.getUserData(
            onSuccess = { userData ->
                checkDateAndReset(userData)
            },
            onError = { exception ->
                Log.e("WaterViewModel", "Error loading initial data", exception)
            }
        )
    }

    private fun checkDateAndReset(userData: UserData) {
        val currentDate = getCurrentDate()
        if (userData.lastUpdatedDate != currentDate) {
            handleNewDay(userData, currentDate)
        } else {
            updateLiveData(userData)
        }
    }

    private fun updateLiveData(userData: UserData) {
        dailyWaterGoal.value = userData.dailyWaterGoal
        currentConsumption.value = userData.currentConsumption
    }

    private fun handleNewDay(userData: UserData, currentDate: String) {
        if (userData.currentConsumption < userData.dailyWaterGoal) {
            val newGoal = userData.dailyWaterGoal + 500
            repository.updateDailyGoal(
                newGoal,
                onComplete = {
                    dailyWaterGoal.value = newGoal
                    resetDailyConsumption(currentDate)
                },
                onError = { exception ->
                    Log.e("WaterViewModel", "Failed to update daily goal", exception)
                }
            )
        } else {
            resetDailyConsumption(currentDate)
        }
    }

    private fun resetDailyConsumption(currentDate: String) {
        repository.updateConsumption(
            amount = 0,
            date = currentDate,
            onComplete = {
                currentConsumption.value = 0
            },
            onError = { exception ->
                Log.e("WaterViewModel", "Failed to reset daily consumption", exception)
            }
        )
    }

    fun addWater(amount: Int) {
        val current = currentConsumption.value ?: 0
        val newAmount = current + amount
        repository.updateConsumption(
            newAmount,
            getCurrentDate(),
            onComplete = {
                currentConsumption.value = newAmount
            },
            onError = { exception ->
                Log.e("WaterViewModel", "Failed to add water", exception)
            }
        )
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
