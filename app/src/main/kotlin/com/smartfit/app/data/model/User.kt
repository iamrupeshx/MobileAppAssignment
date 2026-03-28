package com.smartfit.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val age: Int,
    val weightKg: Float,
    val heightCm: Float,
    val gender: String = "male",
    val stepGoal: Int = 10_000,
    val waterGoal: Int = 8,
    val calorieGoal: Int = 2200,
    val profileImageUri: String? = null
)
