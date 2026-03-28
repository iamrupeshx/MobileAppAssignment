package com.smartfit.app.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartfit.app.data.local.datastore.UserPreferences
import com.smartfit.app.data.model.User
import com.smartfit.app.data.repository.ActivityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class ProfileUiState(
    val user: User?         = null,
    val stepGoal: Int       = 10_000,
    val waterGoal: Int      = 8,
    val calorieGoal: Int    = 2200,
    val isDarkTheme: Boolean = true
)

class ProfileViewModel(
    private val repository: ActivityRepository,
    private val userPrefs: UserPreferences,
    private val userId: Int
) : ViewModel() {
    private val TAG = "ProfileViewModel"
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        if (userId != -1) {
            observeUser()
            observePreferences()
        }
    }

    private fun observeUser() = viewModelScope.launch {
        repository.observeUser(userId).collect { user ->
            Log.d(TAG, "User profile updated: ${user?.fullName}")
            _uiState.update { 
                it.copy(
                    user = user,
                    stepGoal = user?.stepGoal ?: it.stepGoal,
                    waterGoal = user?.waterGoal ?: it.waterGoal,
                    calorieGoal = user?.calorieGoal ?: it.calorieGoal
                ) 
            }
        }
    }

    private fun observePreferences() = viewModelScope.launch {
        userPrefs.isDarkTheme.collect { dark ->
            _uiState.update { it.copy(isDarkTheme = dark) }
        }
    }

    fun updateStepGoal(goal: Int) = viewModelScope.launch {
        Log.d(TAG, "Updating step goal to $goal")
        _uiState.value.user?.let { user ->
            repository.updateUser(user.copy(stepGoal = goal))
        }
        userPrefs.setStepGoal(goal)
    }

    fun updateWaterGoal(goal: Int) = viewModelScope.launch {
        Log.d(TAG, "Updating water goal to $goal")
        _uiState.value.user?.let { user ->
            repository.updateUser(user.copy(waterGoal = goal))
        }
        userPrefs.setWaterGoal(goal)
    }

    fun updateCalorieGoal(goal: Int) = viewModelScope.launch {
        _uiState.value.user?.let { user ->
            repository.updateUser(user.copy(calorieGoal = goal))
        }
    }

    fun toggleTheme() = viewModelScope.launch {
        val newDark = !_uiState.value.isDarkTheme
        userPrefs.setDarkTheme(newDark)
    }

    fun updateBodyStats(age: Int, weight: Float, height: Float) = viewModelScope.launch {
        _uiState.value.user?.let { user ->
            repository.updateUser(user.copy(age = age, weightKg = weight, heightCm = height))
        }
    }

    fun updateProfileImage(context: Context, uri: Uri) = viewModelScope.launch {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "profile_pic_$userId.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            val internalUri = Uri.fromFile(file).toString()
            
            _uiState.value.user?.let { user ->
                repository.updateUser(user.copy(profileImageUri = internalUri))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save profile image", e)
        }
    }

    fun updateProfileDetails(name: String, email: String, gender: String) = viewModelScope.launch {
        _uiState.value.user?.let { user ->
            repository.updateUser(user.copy(fullName = name, email = email, gender = gender))
        }
    }
}
