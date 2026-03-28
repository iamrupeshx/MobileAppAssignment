package com.smartfit.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.smartfit.app.navigation.Routes
import com.smartfit.app.ui.components.*
import com.smartfit.app.ui.theme.*
import com.smartfit.app.util.DateUtil
import com.smartfit.app.viewmodel.ActivityViewModel

@Composable
fun ActivityLogScreen(
    viewModel: ActivityViewModel, isDark: Boolean,
    onAddClick: () -> Unit, onEditClick: (Int) -> Unit, onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val todayStart = DateUtil.todayStartMs()
    val todayActivities = state.activities.filter { it.date >= todayStart }
    val pastActivities  = state.activities.filter { it.date < todayStart }

    // API Powered Tips - Filtered to only show those with valid images
    val suggestions = remember(state.workoutSuggestions) {
        state.workoutSuggestions.filter { it.imageUrl.isNotEmpty() }
    }
    var currentTipIndex by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(suggestions.size) {
        if (suggestions.isNotEmpty()) {
            while(true) {
                kotlinx.coroutines.delay(10000)
                currentTipIndex = (currentTipIndex + 1) % suggestions.size
            }
        }
    }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) kotlinx.coroutines.delay(1500).also { viewModel.clearMessages() }
    }

    Scaffold(containerColor = Color.Transparent,
        bottomBar = { SmartFitBottomNav(Routes.ACTIVITY_LOG, isDark, onNavigate) },
        floatingActionButton = { AnimatedFab(onAddClick, contentDescription = "Log workout",
            icon = Icons.Default.Add) }
    ) { padding ->
        GradientBackground(isDark) {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text("Workout Log", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("All your training sessions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.55f))
                }

                // Dynamic Exercise Tip from API - Only showing illustrated exercises
                if (suggestions.isNotEmpty()) {
                    item {
                        val suggestion = suggestions[currentTipIndex]
                        GlassCard(isDark = isDark, usePurpleTint = true, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(Purple500.copy(0.1f)), contentAlignment = Alignment.Center) {
                                    AsyncImage(
                                        model = suggestion.imageUrl,
                                        contentDescription = suggestion.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text("Expert Tip: ${suggestion.name}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Purple400)
                                    AnimatedContent(
                                        targetState = suggestion.description.ifBlank { "Try this ${suggestion.category} exercise to improve your fitness!" },
                                        transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                                        label = "tip_anim"
                                    ) { tip ->
                                        Text(tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(0.85f), maxLines = 2)
                                    }
                                }
                                IconButton(onClick = { onNavigate(Routes.SUGGESTIONS) }) {
                                    Icon(Icons.Default.ChevronRight, null, tint = Purple500)
                                }
                            }
                        }
                    }
                }

                // Success snackbar
                item {
                    AnimatedVisibility(state.successMessage != null, enter = fadeIn() + slideInVertically()) {
                        GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Success",
                                    tint = GreenSuccess, modifier = Modifier.size(20.dp))
                                Text(state.successMessage ?: "", color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Weekly stats summary
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("🔥", "This Week", "%,d".format(state.weeklyCaloriesBurned), "kcal",
                            GradCalorie, Modifier.weight(1f))
                        StatCard("📅", "Sessions", "${state.activities.size}", "total",
                            GradSteps, Modifier.weight(1f))
                    }
                }

                if (todayActivities.isEmpty() && pastActivities.isEmpty()) {
                    item {
                        GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💪", fontSize = 48.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("No workouts yet!", fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground)
                                Text("Tap + to log your first workout",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                            }
                        }
                    }
                }

                if (todayActivities.isNotEmpty()) {
                    item { SectionHeader("Today", isDark) }
                    items(todayActivities, key = { it.id }) { activity ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteActivity(activity); true
                                } else false
                            }
                        )
                        SwipeToDismissBox(dismissState,
                            modifier = Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp)),
                            backgroundContent = {
                                Box(Modifier.fillMaxSize()
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                                    .background(RedError.copy(0.85f))
                                    .padding(end = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) { Icon(Icons.Default.Delete, contentDescription = "Delete workout", tint = Color.White) }
                            }
                        ) {
                            ActivityCard(activity, isDark, onEditClick)
                        }
                    }
                }

                if (pastActivities.isNotEmpty()) {
                    item { SectionHeader("Earlier", isDark) }
                    items(pastActivities, key = { it.id }) { activity ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteActivity(activity); true
                                } else false
                            }
                        )
                        SwipeToDismissBox(dismissState,
                            modifier = Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp)),
                            backgroundContent = {
                                Box(Modifier.fillMaxSize()
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                                    .background(RedError.copy(0.85f))
                                    .padding(end = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) { Icon(Icons.Default.Delete, contentDescription = "Delete workout", tint = Color.White) }
                            }
                        ) {
                            ActivityCard(activity, isDark, onEditClick)
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun ActivityCard(activity: com.smartfit.app.data.model.ActivityLog, isDark: Boolean, onEdit: (Int) -> Unit) {
    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(activityEmoji(activity.activityType), fontSize = 32.sp)
                Column {
                    Text(activity.activityType, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground)
                    Text("${activity.durationMinutes} min · ${DateUtil.formatDate(activity.date)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                    if (activity.steps > 0)
                        Text("${"%,d".format(activity.steps)} steps",
                            style = MaterialTheme.typography.labelSmall, color = Purple400.copy(0.8f))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${activity.caloriesBurned}", color = OrangeCal,
                    fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Text("kcal", style = MaterialTheme.typography.labelSmall, color = OrangeCal.copy(0.7f))
                IconButton({ onEdit(activity.id) }, Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit workout",
                        tint = Purple400, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
