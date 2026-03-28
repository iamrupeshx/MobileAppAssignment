package com.smartfit.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore("smartfit_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_IS_LOGGED_IN    = booleanPreferencesKey("is_logged_in")
        val KEY_LOGGED_IN_USER  = intPreferencesKey("logged_in_user_id")
        val KEY_USER_NAME       = stringPreferencesKey("user_name")
        val KEY_DARK_THEME      = booleanPreferencesKey("dark_theme")
        val KEY_STEP_GOAL       = intPreferencesKey("step_goal")
        val KEY_WATER_GOAL      = intPreferencesKey("water_goal")
        val KEY_CALORIE_GOAL    = intPreferencesKey("calorie_goal")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_IS_LOGGED_IN] ?: false }

    val loggedInUserId: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_LOGGED_IN_USER] ?: -1 }

    val userName: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_USER_NAME] ?: "User" }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_DARK_THEME] ?: true }

    val dailyStepGoal: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_STEP_GOAL] ?: 10_000 }

    val waterGoal: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_WATER_GOAL] ?: 8 }
    
    val calorieGoal: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_CALORIE_GOAL] ?: 2200 }

    suspend fun saveLoginSession(userId: Int, name: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN]   = true
            prefs[KEY_LOGGED_IN_USER] = userId
            prefs[KEY_USER_NAME]      = name
        }
    }

    suspend fun clearLoginSession() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN]   = false
            prefs[KEY_LOGGED_IN_USER] = -1
            prefs[KEY_USER_NAME]      = ""
        }
    }

    suspend fun setDarkTheme(dark: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = dark }
    }

    suspend fun setStepGoal(goal: Int) {
        context.dataStore.edit { it[KEY_STEP_GOAL] = goal }
    }

    suspend fun setWaterGoal(goal: Int) {
        context.dataStore.edit { it[KEY_WATER_GOAL] = goal }
    }

    suspend fun setCalorieGoal(goal: Int) {
        context.dataStore.edit { it[KEY_CALORIE_GOAL] = goal }
    }
}
