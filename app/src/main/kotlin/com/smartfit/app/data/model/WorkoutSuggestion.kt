package com.smartfit.app.data.model

data class WorkoutSuggestion(
    val id: Int = 0,
    val name: String,
    val category: String,
    val difficulty: String,         // "Beginner" | "Intermediate" | "Advanced"
    val durationMinutes: Int,
    val estimatedCalories: Int,
    val imageUrl: String = "",
    val description: String = ""
)
