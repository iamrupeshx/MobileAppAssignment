package com.smartfit.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartfit.app.data.model.ActivityLog
import com.smartfit.app.data.model.FoodLog
import com.smartfit.app.data.model.WorkoutSuggestion
import com.smartfit.app.data.repository.ActivityRepository
import com.smartfit.app.util.DateUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ActivityUiState(
    val activities: List<ActivityLog>           = emptyList(),
    val foodLogs: List<FoodLog>                 = emptyList(),
    val todayCaloriesBurned: Int                = 0,
    val todayCaloriesConsumed: Int              = 0,
    val todaySteps: Int                         = 0,
    val weeklyCaloriesBurned: Int               = 0,
    val workoutSuggestions: List<WorkoutSuggestion> = emptyList(),
    val isLoadingSuggestions: Boolean           = false,
    val isLoading: Boolean                      = false,
    val successMessage: String?                 = null,
    val errorMessage: String?                   = null,
    val waterGlasses: Int                       = 0,
)

class ActivityViewModel(
    private val repository: ActivityRepository,
    val userId: Int
) : ViewModel() {
    private val TAG = "ActivityViewModel"

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "ViewModel initialized for userId=$userId")
        observeActivities()
        observeFoodLogs()
        fetchWorkoutSuggestions()
    }

    private fun observeActivities() {
        viewModelScope.launch {
            repository.getActivities(userId).collect { list ->
                val todayStart = DateUtil.todayStartMs()
                val weekStart  = DateUtil.weekStartMs()
                val todayCal   = list.filter { it.date >= todayStart }.sumOf { it.caloriesBurned }
                val todaySteps = list.filter { it.date >= todayStart }.sumOf { it.steps }
                val weekCal    = list.filter { it.date >= weekStart  }.sumOf { it.caloriesBurned }
                Log.d(TAG, "Activities updated: ${list.size} total, todayCal=$todayCal, todaySteps=$todaySteps")
                _uiState.update { it.copy(
                    activities           = list,
                    todayCaloriesBurned  = todayCal,
                    todaySteps           = todaySteps,
                    weeklyCaloriesBurned = weekCal
                )}
            }
        }
    }

    private fun observeFoodLogs() {
        viewModelScope.launch {
            repository.getFoodLogs(userId).collect { list ->
                val todayStart   = DateUtil.todayStartMs()
                val todayFoodCal = list.filter { it.date >= todayStart }.sumOf { it.calories }
                Log.d(TAG, "Food logs updated: ${list.size} entries, todayConsumed=$todayFoodCal")
                _uiState.update { it.copy(
                    foodLogs               = list,
                    todayCaloriesConsumed  = todayFoodCal
                )}
            }
        }
    }

    // ── Activities ───────────────────────────────────────────────────
    fun addActivity(activity: ActivityLog) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        repository.addActivity(activity.copy(userId = userId))
        Log.d(TAG, "Activity added: ${activity.activityType}")
        _uiState.update { it.copy(isLoading = false, successMessage = "Activity logged! 💪") }
    }
    fun updateActivity(activity: ActivityLog) = viewModelScope.launch {
        repository.updateActivity(activity)
        _uiState.update { it.copy(successMessage = "Activity updated!") }
    }
    fun deleteActivity(activity: ActivityLog) = viewModelScope.launch {
        repository.deleteActivity(activity)
        _uiState.update { it.copy(successMessage = "Activity deleted") }
    }

    // ── Food ─────────────────────────────────────────────────────────
    fun addFoodLog(food: FoodLog) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        repository.addFoodLog(food.copy(userId = userId))
        Log.d(TAG, "Food logged: ${food.foodName} (${food.calories} kcal)")
        _uiState.update { it.copy(isLoading = false, successMessage = "Meal logged! 🍽️") }
    }
    fun deleteFoodLog(food: FoodLog) = viewModelScope.launch {
        repository.deleteFoodLog(food)
        _uiState.update { it.copy(successMessage = "Food entry removed") }
    }

    // ── Water ────────────────────────────────────────────────────────
    fun addWaterGlass() {
        val current = _uiState.value.waterGlasses
        Log.d(TAG, "Water glass added: ${current + 1}")
        _uiState.update { it.copy(waterGlasses = current + 1) }
    }
    fun removeWaterGlass() {
        val current = _uiState.value.waterGlasses
        if (current > 0) _uiState.update { it.copy(waterGlasses = current - 1) }
    }

    // ── Suggestions ──────────────────────────────────────────────────
    fun fetchWorkoutSuggestions(bodyPartId: Int? = null) = viewModelScope.launch {
        Log.d(TAG, "Fetching workout suggestions")
        _uiState.update { it.copy(isLoadingSuggestions = true) }
        try {
            val list = repository.fetchWorkoutSuggestions(bodyPartId)
            _uiState.update { it.copy(workoutSuggestions = list, isLoadingSuggestions = false) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch suggestions: ${e.message}", e)
            _uiState.update { it.copy(isLoadingSuggestions = false) }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(successMessage = null, errorMessage = null) }
}
