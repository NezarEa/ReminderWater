package com.reminderwater.Dashbord.Data

data class UserData(
    val name: String = "",
    val dailyWaterGoal: Int = 2000,
    val currentConsumption: Int = 0,
    val lastUpdatedDate: String = ""
)
