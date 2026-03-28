package com.smartfit.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.smartfit.app.navigation.Routes
import com.smartfit.app.ui.components.*
import com.smartfit.app.ui.theme.*
import com.smartfit.app.util.DateUtil
import com.smartfit.app.viewmodel.ActivityViewModel

@Composable
fun FoodLogScreen(
    viewModel: ActivityViewModel, isDark: Boolean,
    onAddClick: () -> Unit, onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val todayStart = DateUtil.todayStartMs()
    val todayFood  = state.foodLogs.filter { it.date >= todayStart }

    val mealTypes  = listOf("All", "Breakfast", "Lunch", "Dinner", "Snack")
    var selectedMeal by remember { mutableStateOf("All") }
    val filtered = if (selectedMeal == "All") todayFood
                   else todayFood.filter { it.mealType == selectedMeal }

    val totalCal  = todayFood.sumOf { it.calories }
    val totalProt = todayFood.sumOf { it.proteinG.toDouble() }.toFloat()
    val totalCarbs= todayFood.sumOf { it.carbsG.toDouble()  }.toFloat()
    val totalFat  = todayFood.sumOf { it.fatG.toDouble()    }.toFloat()

    Scaffold(containerColor = Color.Transparent,
        bottomBar = { SmartFitBottomNav(Routes.FOOD_LOG, isDark, onNavigate) },
        floatingActionButton = { AnimatedFab(onAddClick, contentDescription = "Add meal") }
    ) { padding ->
        GradientBackground(isDark) {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Nutrition", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Track what you eat today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.55f))
                }

                // Calorie summary card
                item {
                    GlassCard(isDark = isDark, usePurpleTint = true, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Today's Intake", style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
                                Text("%,d kcal".format(totalCal),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold, color = GreenSuccess)
                            }
                            Text("🍽️", fontSize = 40.sp)
                        }
                        Spacer(Modifier.height(14.dp))
                        // Macros row
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            MacroPill("🥩", "Protein", totalProt, GradFood[0])
                            MacroPill("🍞", "Carbs",   totalCarbs, GradActive[0])
                            MacroPill("🥑", "Fat",     totalFat,  GradNet[0])
                        }
                    }
                }

                // Meal filter chips
                item {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mealTypes) { meal ->
                            val selected = selectedMeal == meal
                            FilterChip(selected, { selectedMeal = meal }, label = { Text(meal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Purple500.copy(0.25f),
                                    selectedLabelColor = Purple400,
                                    containerColor = Color.Transparent,
                                    labelColor = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true, selected = selected,
                                    selectedBorderColor = Purple500, borderColor = MaterialTheme.colorScheme.outline.copy(0.3f)
                                )
                            )
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    item {
                        GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🍽️", fontSize = 48.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("No meals logged yet", fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground)
                                Text("Tap + to add your first meal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                            }
                        }
                    }
                } else {
                    items(filtered, key = { it.id }) { food ->
                        AnimatedVisibility(true, enter = fadeIn() + slideInVertically()) {
                            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text(mealEmoji(food.mealType), fontSize = 30.sp)
                                        Column {
                                            Text(food.foodName, fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onBackground)
                                            Text("${food.mealType} · ${food.servingSize}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                                            if (food.proteinG > 0 || food.carbsG > 0 || food.fatG > 0) {
                                                Text("P:${food.proteinG.toInt()}g  C:${food.carbsG.toInt()}g  F:${food.fatG.toInt()}g",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Purple400.copy(0.8f))
                                            }
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${food.calories}\nkcal",
                                            color = GreenSuccess, fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                        IconButton({ viewModel.deleteFoodLog(food) },
                                            modifier = Modifier.size(36.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete food entry",
                                                tint = RedError.copy(0.6f), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun MacroPill(icon: String, label: String, value: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 18.sp)
        Text("%.0fg".format(value), style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
    }
}

private fun mealEmoji(meal: String) = when (meal) {
    "Breakfast" -> "🌅"; "Lunch" -> "☀️"; "Dinner" -> "🌙"; else -> "🍎"
}
