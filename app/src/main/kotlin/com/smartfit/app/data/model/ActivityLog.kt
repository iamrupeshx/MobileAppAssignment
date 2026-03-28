package com.smartfit.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val activityType: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val steps: Int = 0,
    val distanceKm: Float = 0f,
    val notes: String = "",
    val date: Long = System.currentTimeMillis()
)
