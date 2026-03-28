package com.smartfit.app.util

import android.util.Log
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

object HashUtil {
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

object DateUtil {
    private const val TAG = "DateUtil"

    fun todayStartMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun weekStartMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        Log.d(TAG, "Week start: ${cal.time}")
        return cal.timeInMillis
    }

    fun dayName(ms: Long): String = SimpleDateFormat("EEE", Locale.getDefault()).format(Date(ms))
    fun formatDate(ms: Long): String = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ms))
    fun formatDateTime(ms: Long): String = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(ms))
}

object CalorieUtil {
    private const val TAG = "CalorieUtil"

    private val metValues = mapOf(
        "running"         to 9.8f,
        "walking"         to 3.5f,
        "cycling"         to 6.8f,
        "swimming"        to 7.0f,
        "weight training" to 5.0f,
        "yoga"            to 2.5f,
        "hiit"            to 10.0f,
        "basketball"      to 6.5f,
        "football"        to 7.0f,
        "badminton"       to 5.5f,
        "cardio"          to 7.0f,
        "cardiovascular system" to 7.0f,
        "abs"             to 4.0f,
        "quads"           to 5.0f,
        "hamstrings"      to 5.0f,
        "glutes"          to 5.0f,
        "lats"            to 5.0f,
        "pectorals"       to 5.0f,
        "triceps"         to 4.5f,
        "biceps"          to 4.0f,
        "deltoids"        to 4.0f,
        "other"           to 5.0f
    )

    fun estimate(activityType: String, durationMinutes: Int, weightKg: Float): Int {
        val met = metValues[activityType.lowercase()] ?: 5.0f
        val cal = met * weightKg * (durationMinutes / 60f)
        Log.d(TAG, "Calorie estimate: $activityType, ${durationMinutes}min, ${weightKg}kg = ${cal.toInt()} kcal")
        return cal.toInt()
    }

    fun estimateFromSteps(steps: Int, weightKg: Float): Int {
        // Average 0.04 - 0.06 kcal per step depending on weight
        val cal = steps * (weightKg * 0.00065f)
        return cal.toInt()
    }

    fun bmr(weightKg: Float, heightCm: Float, age: Int, gender: String = "male"): Int {
        val result = if (gender.lowercase() == "female")
            (10 * weightKg + 6.25f * heightCm - 5 * age - 161).toInt()
        else
            (10 * weightKg + 6.25f * heightCm - 5 * age + 5).toInt()
        Log.d(TAG, "BMR calculated: gender=$gender, weight=$weightKg, height=$heightCm, age=$age => $result")
        return result
    }

    fun stepProgress(steps: Int, goal: Int): Float {
        if (goal <= 0) return 0f
        return (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    }
}
