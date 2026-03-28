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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.smartfit.app.data.model.FoodLog
import com.smartfit.app.ui.components.*
import com.smartfit.app.ui.theme.*
import com.smartfit.app.viewmodel.ActivityViewModel

// Common foods database with calorie lookup
private val COMMON_FOODS = listOf(
    Triple("White Rice (1 cup)", 206, "Rice"),
    Triple("Chicken Breast (100g)", 165, "Protein"),
    Triple("Banana", 89, "Fruit"),
    Triple("Apple", 52, "Fruit"),
    Triple("Egg (1 large)", 78, "Protein"),
    Triple("Bread (1 slice)", 79, "Grains"),
    Triple("Milk (1 cup)", 149, "Dairy"),
    Triple("Orange Juice (1 cup)", 112, "Beverage"),
    Triple("Pasta (1 cup cooked)", 220, "Grains"),
    Triple("Salmon (100g)", 208, "Protein"),
    Triple("Oatmeal (1 cup)", 154, "Grains"),
    Triple("Greek Yogurt (1 cup)", 130, "Dairy"),
    Triple("Peanut Butter (2 tbsp)", 188, "Fats"),
    Triple("Almonds (1 oz)", 164, "Fats"),
    Triple("Sweet Potato (1 medium)", 103, "Vegetable"),
    Triple("Broccoli (1 cup)", 55, "Vegetable"),
    Triple("Avocado (half)", 120, "Fats"),
    Triple("Tuna (100g)", 132, "Protein"),
    Triple("Cheese (1 oz)", 110, "Dairy"),
    Triple("Coffee (black)", 2, "Beverage"),
)

@Composable
fun AddFoodScreen(
    viewModel: ActivityViewModel, userId: Int, isDark: Boolean, onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var foodName     by remember { mutableStateOf("") }
    var calories     by remember { mutableStateOf("") }
    var protein      by remember { mutableStateOf("") }
    var carbs        by remember { mutableStateOf("") }
    var fat          by remember { mutableStateOf("") }
    var servingSize  by remember { mutableStateOf("1 serving") }
    var selectedMeal by remember { mutableStateOf("Breakfast") }
    var showQuick    by remember { mutableStateOf(true) }
    var searchQuery  by remember { mutableStateOf("") }
    var errorMsg     by remember { mutableStateOf<String?>(null) }

    val filteredFoods = remember(searchQuery) {
        if (searchQuery.isBlank()) COMMON_FOODS
        else COMMON_FOODS.filter { it.first.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) { viewModel.clearMessages(); onBack() }
    }

    GradientBackground(isDark) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Log a Meal", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Track what you eat", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.55f))
                }
            }
            Spacer(Modifier.height(24.dp))

            // Meal type selector
            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                Text("Meal Type", color = Purple500, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Breakfast" to "🌅", "Lunch" to "☀️", "Dinner" to "🌙", "Snack" to "🍎")
                        .forEach { (meal, emoji) ->
                            val sel = selectedMeal == meal
                            Column(
                                Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                    .background(if (sel) Purple500.copy(0.2f) else Color.Transparent)
                                    .border(if (sel) 2.dp else 1.dp,
                                        if (sel) Purple500 else MaterialTheme.colorScheme.outline.copy(0.3f),
                                        RoundedCornerShape(12.dp))
                                    .clickable { selectedMeal = meal }.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(emoji, fontSize = 20.sp)
                                Text(meal, style = MaterialTheme.typography.labelSmall,
                                    color = if (sel) Purple400 else MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                }
            }
            Spacer(Modifier.height(14.dp))

            // Toggle: Quick pick vs Manual
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(true to "⚡ Quick Pick", false to "✏️ Manual Entry").forEach { (isQuick, label) ->
                    val sel = showQuick == isQuick
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                        .background(if (sel) Purple500 else Color.Transparent)
                        .border(1.dp, if (sel) Purple500 else MaterialTheme.colorScheme.outline.copy(0.3f), RoundedCornerShape(12.dp))
                        .clickable { showQuick = isQuick }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface.copy(0.6f),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            if (showQuick) {
                // Quick food picker
                GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                    Text("Search Foods", color = Purple500, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(10.dp))
                    GlassTextField(searchQuery, { searchQuery = it }, "Search (e.g. rice, chicken...)",
                        isDark = isDark,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Purple500) })
                    Spacer(Modifier.height(12.dp))
                    filteredFoods.take(8).forEach { (name, cal, category) ->
                        Row(Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                foodName = name; calories = cal.toString()
                                protein  = (cal * 0.25f / 4).toInt().toString()
                                carbs    = (cal * 0.50f / 4).toInt().toString()
                                fat      = (cal * 0.25f / 9).toInt().toString()
                                servingSize = "1 serving"; showQuick = false
                            }
                            .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(name, style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                                Text(category, style = MaterialTheme.typography.labelSmall,
                                    color = Purple400.copy(0.7f))
                            }
                            Text("$cal kcal", color = GreenSuccess, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Divider(color = MaterialTheme.colorScheme.outline.copy(0.1f))
                    }
                }
            } else {
                // Manual entry form
                GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                    Text("Food Details", color = Purple500, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(14.dp))
                    GlassTextField(foodName, { foodName = it }, "Food Name", isDark = isDark,
                        leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = "Food", tint = Purple500) })
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassTextField(calories, { calories = it }, "Calories", isDark = isDark,
                            keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = "Calories", tint = OrangeCal) })
                        GlassTextField(servingSize, { servingSize = it }, "Serving", isDark = isDark,
                            modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Macros (optional)", color = Purple400, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassTextField(protein, { protein = it }, "Protein g", isDark = isDark,
                            keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f))
                        GlassTextField(carbs, { carbs = it }, "Carbs g", isDark = isDark,
                            keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f))
                        GlassTextField(fat, { fat = it }, "Fat g", isDark = isDark,
                            keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Error
            AnimatedVisibility(errorMsg != null) {
                Text(errorMsg ?: "", color = RedError, style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(20.dp))

            GradientButton("LOG MEAL", {
                val cal = calories.toIntOrNull()
                when {
                    foodName.isBlank() -> { errorMsg = "Please enter a food name" }
                    cal == null || cal <= 0 -> { errorMsg = "Please enter valid calories" }
                    else -> {
                        errorMsg = null
                        viewModel.addFoodLog(FoodLog(
                            userId = userId, foodName = foodName.trim(),
                            mealType = selectedMeal, calories = cal,
                            proteinG = protein.toFloatOrNull() ?: 0f,
                            carbsG   = carbs.toFloatOrNull()  ?: 0f,
                            fatG     = fat.toFloatOrNull()    ?: 0f,
                            servingSize = servingSize.trim()
                        ))
                    }
                }
            }, Modifier.fillMaxWidth(), isLoading = uiState.isLoading)
            Spacer(Modifier.height(40.dp))
        }
    }
}
