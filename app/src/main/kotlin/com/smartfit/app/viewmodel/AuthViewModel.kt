package com.smartfit.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartfit.app.data.local.datastore.UserPreferences
import com.smartfit.app.data.model.User
import com.smartfit.app.data.repository.ActivityRepository
import com.smartfit.app.util.HashUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean    = false,
    val isSuccess: Boolean    = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val repository: ActivityRepository,
    private val userPrefs: UserPreferences
) : ViewModel() {
    private val TAG = "AuthViewModel"
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        Log.d(TAG, "Login attempt for email: $email")
        when {
            email.isBlank()   -> { _uiState.update { it.copy(errorMessage = "Please enter your email") }; return }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                                 { _uiState.update { it.copy(errorMessage = "Please enter a valid email") }; return }
            password.isBlank() -> { _uiState.update { it.copy(errorMessage = "Please enter your password") }; return }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = repository.findUserByEmail(email.trim().lowercase())
                if (user == null) {
                    Log.w(TAG, "Login failed: no account for $email")
                    _uiState.update { it.copy(isLoading = false, errorMessage = "No account found with this email") }
                    return@launch
                }
                if (user.passwordHash != HashUtil.sha256(password)) {
                    Log.w(TAG, "Login failed: wrong password for $email")
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Incorrect password") }
                    return@launch
                }
                Log.d(TAG, "Login success for userId=${user.id}")
                userPrefs.saveLoginSession(user.id, user.fullName)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Login failed. Please try again") }
            }
        }
    }

    fun register(
        fullName: String, email: String, password: String, confirmPassword: String,
        age: Int, weightKg: Float, heightCm: Float, gender: String = "male", stepGoal: Int = 10_000
    ) {
        Log.d(TAG, "Register attempt for email: $email")
        when {
            fullName.isBlank()   -> { _uiState.update { it.copy(errorMessage = "Please enter your full name") }; return }
            email.isBlank()      -> { _uiState.update { it.copy(errorMessage = "Please enter your email") }; return }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                                    { _uiState.update { it.copy(errorMessage = "Please enter a valid email") }; return }
            age <= 0 || age > 120 -> { _uiState.update { it.copy(errorMessage = "Please enter a valid age") }; return }
            weightKg <= 0f       -> { _uiState.update { it.copy(errorMessage = "Please enter your weight in kg") }; return }
            heightCm <= 0f       -> { _uiState.update { it.copy(errorMessage = "Please enter your height in cm") }; return }
            password.length < 6  -> { _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }; return }
            password != confirmPassword -> { _uiState.update { it.copy(errorMessage = "Passwords do not match") }; return }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                if (repository.findUserByEmail(email.trim().lowercase()) != null) {
                    Log.w(TAG, "Register failed: email already exists $email")
                    _uiState.update { it.copy(isLoading = false, errorMessage = "This email is already registered") }
                    return@launch
                }
                val newUser = User(
                    fullName = fullName.trim(), email = email.trim().lowercase(),
                    passwordHash = HashUtil.sha256(password), age = age,
                    weightKg = weightKg, heightCm = heightCm, gender = gender,
                    stepGoal = stepGoal, waterGoal = 8
                )
                val id = repository.registerUser(newUser)
                if (id <= 0) {
                    Log.e(TAG, "Register failed: DB returned id=$id")
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Registration failed. Try again") }
                    return@launch
                }
                Log.d(TAG, "Register success: userId=$id")
                // REMOVED: userPrefs.saveLoginSession(id.toInt(), fullName.trim())
                // We want the user to login manually after registration
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                Log.e(TAG, "Register error: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Registration failed: ${e.message}") }
            }
        }
    }

    fun logout() = viewModelScope.launch {
        Log.d(TAG, "User logged out")
        userPrefs.clearLoginSession()
    }
    fun resetSuccess() = _uiState.update { it.copy(isSuccess = false) }
    fun clearError()   = _uiState.update { it.copy(errorMessage = null) }
}
