package com.smartfit.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.smartfit.app.data.model.ActivityLog
import com.smartfit.app.ui.components.*
import com.smartfit.app.ui.theme.*
import com.smartfit.app.util.CalorieUtil
import com.smartfit.app.viewmodel.ActivityViewModel

val ACTIVITY_TYPES = listOf(
    "Running", "Walking", "Cycling", "Swimming",
    "Weight Training", "Yoga", "HIIT", "Basketball",
    "Football", "Badminton", "Other"
)

@Composable
fun AddEditActivityScreen(
    viewModel: ActivityViewModel, userId: Int, activityId: Int?,
    userWeightKg: Float = 70f, isDark: Boolean = true, onBack: () -> Unit
) {
    val uiState      by viewModel.uiState.collectAsState()
    val editActivity  = activityId?.let { id -> uiState.activities.find { it.id == id } }
    val isEdit        = editActivity != null

    var selectedType   by remember { mutableStateOf(editActivity?.activityType ?: ACTIVITY_TYPES[0]) }
    var duration       by remember { mutableStateOf(editActivity?.durationMinutes?.toString() ?: "") }
    var calories       by remember { mutableStateOf(editActivity?.caloriesBurned?.toString() ?: "") }
    var steps          by remember { mutableStateOf(editActivity?.steps?.toString() ?: "") }
    var distanceKm     by remember { mutableStateOf(editActivity?.distanceKm?.toString() ?: "") }
    var notes          by remember { mutableStateOf(editActivity?.notes ?: "") }
    var showTypeMenu   by remember { mutableStateOf(false) }
    var errorMsg       by remember { mutableStateOf<String?>(null) }

    val suggestions = uiState.workoutSuggestions
    var currentTipIndex by remember { mutableIntStateOf(0) }

    fun updateCalculations() {
        val mins = duration.toIntOrNull() ?: 0
        val s = steps.toIntOrNull() ?: 0
        
        val calFromDuration = CalorieUtil.estimate(selectedType, mins, userWeightKg)
        val calFromSteps = if (selectedType.lowercase() in listOf("walking", "running")) {
            (s * 0.045f).toInt()
        } else 0
        
        calories = if (calFromSteps > calFromDuration) calFromSteps.toString() 
                   else calFromDuration.toString()
    }

    LaunchedEffect(suggestions.size) {
        if (suggestions.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(10000)
                currentTipIndex = (currentTipIndex + 1) % suggestions.size
            }
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) { viewModel.clearMessages(); onBack() }
    }

    GradientBackground(isDark) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(if (isEdit) "Edit Workout" else "Log Workout",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text(if (isEdit) "Update your activity" else "Record your training",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.55f))
                }
            }
            Spacer(Modifier.height(24.dp))

            // API Driven Tips Section with Image
            if (suggestions.isNotEmpty()) {
                val suggestion = suggestions[currentTipIndex]
                GlassCard(isDark = isDark, usePurpleTint = true, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(50.dp).clip(RoundedCornerShape(10.dp)).background(Purple500.copy(0.1f)), contentAlignment = Alignment.Center) {
                            if (suggestion.imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = suggestion.imageUrl,
                                    contentDescription = suggestion.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("💡", fontSize = 20.sp)
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Coach's Advice: ${suggestion.name}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Purple400)
                            AnimatedContent(
                                targetState = suggestion.description.ifBlank { "Try ${suggestion.name} for a great ${suggestion.category} workout today!" },
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "workout_tip"
                            ) { tip ->
                                Text(tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(0.85f), maxLines = 2)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            // Activity type selector
            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                Text("Activity Type", color = Purple500, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(12.dp))
                Box {
                    OutlinedButton(
                        { showTypeMenu = true },
                        Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("${activityEmoji(selectedType)} $selectedType",
                            modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select activity type",
                            tint = Purple500)
                    }
                    DropdownMenu(showTypeMenu, { showTypeMenu = false }) {
                        ACTIVITY_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text("${activityEmoji(type)} $type") },
                                onClick = {
                                    selectedType = type; showTypeMenu = false
                                    updateCalculations()
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            // Stats
            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                Text("Training Stats", color = Purple500, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassTextField(duration, {
                        duration = it
                        updateCalculations()
                    }, "Duration (min)", isDark = isDark, keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = "Duration", tint = Purple500) })
                    GlassTextField(calories, { calories = it }, "Calories (kcal)",
                        isDark = isDark, keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = "Calories", tint = OrangeCal) })
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassTextField(steps, {
                        steps = it
                        val s = it.toIntOrNull() ?: 0
                        if (s > 0) {
                            distanceKm = "%.2f".format(s / 1300.0f)
                            // Always update duration based on steps (assume 100 steps/min)
                            duration = (s / 100).coerceAtLeast(1).toString()
                        }
                        updateCalculations()
                    }, "Steps (optional)",
                        isDark = isDark, keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                    GlassTextField(distanceKm, { distanceKm = it }, "Distance (km)",
                        isDark = isDark, keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(14.dp))

            // Notes
            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                Text("Notes (optional)", color = Purple500, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(12.dp))
                GlassTextField(notes, { notes = it }, "How did it feel?",
                    isDark = isDark, singleLine = false, modifier = Modifier.fillMaxWidth().height(88.dp))
            }

            AnimatedVisibility(errorMsg != null) {
                Text(errorMsg ?: "", color = RedError, style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(24.dp))

            GradientButton(if (isEdit) "UPDATE WORKOUT" else "LOG WORKOUT", {
                val dur = duration.toIntOrNull()
                val cal = calories.toIntOrNull()
                when {
                    dur == null || dur <= 0 -> { errorMsg = "Please enter a valid duration" }
                    cal == null || cal < 0  -> { errorMsg = "Please enter valid calories" }
                    else -> {
                        errorMsg = null
                        val log = ActivityLog(
                            id = editActivity?.id ?: 0, userId = userId,
                            activityType = selectedType, durationMinutes = dur,
                            caloriesBurned = cal, steps = steps.toIntOrNull() ?: 0,
                            distanceKm = distanceKm.toFloatOrNull() ?: 0f, notes = notes
                        )
                        if (isEdit) viewModel.updateActivity(log) else viewModel.addActivity(log)
                    }
                }
            }, Modifier.fillMaxWidth(), isLoading = uiState.isLoading)
            Spacer(Modifier.height(40.dp))
        }
    }
}
