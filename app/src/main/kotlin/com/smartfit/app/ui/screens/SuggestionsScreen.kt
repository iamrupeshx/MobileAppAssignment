package com.smartfit.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.smartfit.app.data.model.WorkoutSuggestion
import com.smartfit.app.ui.components.*
import com.smartfit.app.ui.theme.*
import com.smartfit.app.viewmodel.ActivityViewModel

@Composable
fun SuggestionsScreen(viewModel: ActivityViewModel, isDark: Boolean, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val categories = listOf("All", "Chest", "Back", "Legs", "Arms", "Shoulders", "Core", "Cardio")
    var selectedCat by remember { mutableStateOf("All") }
    var selectedWorkout by remember { mutableStateOf<WorkoutSuggestion?>(null) }

    // Filter to only show suggestions that HAVE an image
    val filtered = remember(state.workoutSuggestions, selectedCat) {
        val baseList = state.workoutSuggestions.filter { it.imageUrl.isNotEmpty() }
        if (selectedCat == "All") baseList
        else baseList.filter {
            it.category.contains(selectedCat, true) || it.name.contains(selectedCat, true)
        }
    }

    GradientBackground(isDark) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Row(Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Workout Library", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Visual Exercise Guide",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.55f))
                }
            }

            // Category filter
            LazyRow(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val sel = selectedCat == cat
                    FilterChip(sel, {
                        selectedCat = cat
                        val id = categories.indexOf(cat).takeIf { it > 0 }
                        viewModel.fetchWorkoutSuggestions(id)
                    }, label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Purple500.copy(0.2f),
                            selectedLabelColor = Purple400,
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = sel,
                            selectedBorderColor = Purple500, borderColor = MaterialTheme.colorScheme.outline.copy(0.25f)
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            if (state.isLoadingSuggestions) {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)) {
                    items(6) {
                        ShimmerBox(Modifier.fillMaxWidth().height(140.dp), isDark)
                    }
                }
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No illustrated exercises found", color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(filtered, key = { "${it.id}_${it.name}" }) { suggestion ->
                        AnimatedVisibility(true, enter = fadeIn() + expandVertically()) {
                            Box(modifier = Modifier.clickable { selectedWorkout = suggestion }) {
                                GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                                    Column {
                                        // Large Featured Image (Guaranteed to exist due to filter)
                                        Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(18.dp))) {
                                            AsyncImage(
                                                model = suggestion.imageUrl,
                                                contentDescription = suggestion.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            // Category Overlay
                                            Box(Modifier.align(Alignment.TopStart).padding(12.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(0.6f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(suggestion.category, color = Color.White, 
                                                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        
                                        Spacer(Modifier.height(14.dp))
                                        
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column(Modifier.weight(1f)) {
                                                Text(suggestion.name, fontWeight = FontWeight.ExtraBold,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = MaterialTheme.colorScheme.onBackground)
                                                
                                                Spacer(Modifier.height(6.dp))
                                                
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    DifficultyChip(suggestion.difficulty)
                                                    InfoChip("⏱ ${suggestion.durationMinutes}m")
                                                    InfoChip("🔥 ${suggestion.estimatedCalories} kcal")
                                                }
                                            }
                                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, 
                                                tint = Purple400, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Detailed Popup Dialog
        selectedWorkout?.let { workout ->
            WorkoutDetailDialog(workout = workout, isDark = isDark, onDismiss = { selectedWorkout = null })
        }
    }
}

@Composable
fun WorkoutDetailDialog(workout: WorkoutSuggestion, isDark: Boolean, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.75f)).clickable { onDismiss() },
            contentAlignment = Alignment.Center) {
            GlassCard(
                isDark = isDark,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.8f)
                    .clickable(enabled = false) {}, 
                cornerRadius = 32.dp
            ) {
                Column(Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(24.dp))) {
                        AsyncImage(
                            model = workout.imageUrl,
                            contentDescription = workout.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                            .background(Color.Black.copy(0.3f), CircleShape)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    
                    Column(Modifier.padding(vertical = 20.dp).verticalScroll(rememberScrollState())) {
                        Text(workout.name, style = MaterialTheme.typography.headlineMedium, 
                            fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                        
                        Text(workout.category, style = MaterialTheme.typography.titleMedium, color = Purple500, fontWeight = FontWeight.Bold)
                        
                        Spacer(Modifier.height(20.dp))
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            DetailStat(Icons.Default.Speed, workout.difficulty, "Difficulty")
                            DetailStat(Icons.Default.Timer, "${workout.durationMinutes}m", "Duration")
                            DetailStat(Icons.Default.LocalFireDepartment, "${workout.estimatedCalories}", "Kcal")
                        }
                        
                        HorizontalDivider(Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outline.copy(0.1f))
                        
                        Text("Instructions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Text(
                            workout.description.ifBlank { "No detailed instructions available for this exercise." },
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.8f),
                            textAlign = TextAlign.Justify
                        )
                        
                        Spacer(Modifier.height(32.dp))
                        
                        GradientButton(text = "CLOSE EXPLORER", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Purple500, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
    }
}

@Composable
private fun DifficultyChip(difficulty: String) {
    val (color, bg) = when (difficulty.lowercase()) {
        "beginner"     -> GreenSuccess to GreenSuccess.copy(0.15f)
        "intermediate" -> AmberWarn   to AmberWarn.copy(0.15f)
        else           -> RedError    to RedError.copy(0.15f)
    }
    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(difficulty.replaceFirstChar { it.uppercase() }, color = color, 
            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(Modifier.clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.onBackground.copy(0.05f))
        .padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text, color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}
