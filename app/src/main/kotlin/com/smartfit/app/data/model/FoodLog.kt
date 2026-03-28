package com.smartfit.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_logs")
data class FoodLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val foodName: String,
    val mealType: String,       // Breakfast, Lunch, Dinner, Snack
    val calories: Int,
    val proteinG: Float = 0f,
    val carbsG: Float = 0f,
    val fatG: Float = 0f,
    val servingSize: String = "1 serving",
    val date: Long = System.currentTimeMillis()
)
