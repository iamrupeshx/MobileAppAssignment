package com.smartfit.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.smartfit.app.navigation.Routes
import com.smartfit.app.ui.components.*
import com.smartfit.app.ui.theme.*
import com.smartfit.app.util.DateUtil
import com.smartfit.app.viewmodel.ActivityViewModel
import java.util.Calendar

@Composable
fun SummaryScreen(viewModel: ActivityViewModel, isDark: Boolean, onNavigate: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    // Build 7-day data
    val weeklyData = remember(state.activities) {
        val cal = Calendar.getInstance()
        (6 downTo 0).map { daysBack ->
            cal.time = java.util.Date()
            cal.add(Calendar.DAY_OF_YEAR, -daysBack)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = cal.timeInMillis
            val dayActs = state.activities.filter { it.date in dayStart until dayEnd }
            Triple(DateUtil.dayName(dayStart),
                dayActs.sumOf { it.caloriesBurned }, dayActs.sumOf { it.steps })
        }
    }
    val weeklyFoodData = remember(state.foodLogs) {
        val cal = Calendar.getInstance()
        (6 downTo 0).map { daysBack ->
            cal.time = java.util.Date()
            cal.add(Calendar.DAY_OF_YEAR, -daysBack)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = cal.timeInMillis
            state.foodLogs.filter { it.date in dayStart until dayEnd }.sumOf { it.calories }
        }
    }

    val todayStart     = DateUtil.todayStartMs()
    val todayActs      = state.activities.filter { it.date >= todayStart }
    val maxBurned      = weeklyData.maxOf { it.second }.coerceAtLeast(1)
    val maxConsumed    = weeklyFoodData.maxOrNull()?.coerceAtLeast(1) ?: 1
    val bestDay        = weeklyData.maxByOrNull { it.second }

    Scaffold(containerColor = Color.Transparent,
        bottomBar = { SmartFitBottomNav(Routes.SUMMARY, isDark, onNavigate) }
    ) { padding ->
        GradientBackground(isDark) {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Summary", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Your fitness overview",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.55f))
                }

                // Tab selector
                item {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onBackground.copy(0.06f)).padding(4.dp)
                    ) {
                        listOf("Today", "This Week").forEachIndexed { i, label ->
                            Box(Modifier.weight(1f).clip(RoundedCornerShape(50))
                                .background(if (selectedTab == i) Purple500 else Color.Transparent)
                                .clickable { selectedTab = i }.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (selectedTab == i) Color.White
                                    else MaterialTheme.colorScheme.onBackground.copy(0.6f),
                                    fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    // TODAY
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("🔥", "Burned", "%,d".format(state.todayCaloriesBurned), "kcal",
                                GradCalorie, Modifier.weight(1f))
                            StatCard("🍽️", "Consumed", "%,d".format(state.todayCaloriesConsumed), "kcal",
                                GradFood, Modifier.weight(1f))
                        }
                    }
                    item {
                        GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("Net Calories Today", fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground)
                                val net = state.todayCaloriesConsumed - state.todayCaloriesBurned
                                Text(
                                    if (net > 0) "+%,d kcal".format(net) else "%,d kcal".format(net),
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (net > 500) RedError else GreenSuccess
                                )
                            }
                        }
                    }
                    item {
                        GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                            Text("Today's Workouts", fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(12.dp))
                            if (todayActs.isEmpty()) {
                                Text("No workouts logged today — go crush it! 💪",
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                                    style = MaterialTheme.typography.bodyMedium)
                            } else {
                                todayActs.forEach { act ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Text(activityEmoji(act.activityType), fontSize = 22.sp)
                                            Column {
                                                Text(act.activityType, color = MaterialTheme.colorScheme.onBackground,
                                                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                                Text("${act.durationMinutes} min",
                                                    color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                                                    style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                        Text("${act.caloriesBurned} kcal", color = OrangeCal,
                                            fontWeight = FontWeight.Bold)
                                    }
                                    Divider(color = MaterialTheme.colorScheme.outline.copy(0.08f))
                                }
                            }
                        }
                    }
                } else {
                    // WEEKLY
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("🔥", "Total Burned", "%,d".format(state.weeklyCaloriesBurned), "kcal",
                                GradCalorie, Modifier.weight(1f))
                            StatCard("📅", "Sessions", "${state.activities.size}", "",
                                GradSteps, Modifier.weight(1f))
                        }
                    }

                    // Calories burned bar chart
                    item {
                        GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                            Text("Calories Burned (7 days)", fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(20.dp))
                            Row(Modifier.fillMaxWidth().height(140.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                                weeklyData.forEach { (day, cal, _) ->
                                    val target = cal.toFloat() / maxBurned.toFloat()
                                    val animH by animateFloatAsState(target, tween(1000, easing = FastOutSlowInEasing), label = "bar")
                                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                                        if (cal > 0) Text(cal.toString(), style = MaterialTheme.typography.labelSmall,
                                            color = Purple400, fontSize = 9.sp)
                                        Spacer(Modifier.height(2.dp))
                                        Box(Modifier.width(26.dp).fillMaxHeight(animH.coerceAtLeast(0.04f))
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(Brush.verticalGradient(listOf(Purple400, Purple700))))
                                        Spacer(Modifier.height(6.dp))
                                        Text(day, style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.5f), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Food calories chart
                    if (weeklyFoodData.any { it > 0 }) {
                        item {
                            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                                Text("Calories Consumed (7 days)", fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(20.dp))
                                Row(Modifier.fillMaxWidth().height(120.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                                    weeklyFoodData.forEachIndexed { i, cal ->
                                        val day = weeklyData.getOrNull(i)?.first ?: ""
                                        val target = cal.toFloat() / maxConsumed.toFloat()
                                        val animH by animateFloatAsState(target, tween(1000, easing = FastOutSlowInEasing), label = "fbar$i")
                                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                                            Box(Modifier.width(26.dp).fillMaxHeight(animH.coerceAtLeast(0.04f))
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                .background(Brush.verticalGradient(listOf(GreenSuccess, Color(0xFF059669)))))
                                            Spacer(Modifier.height(6.dp))
                                            Text(day, style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onBackground.copy(0.5f), fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Activity breakdown
                    item {
                        GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                            Text("Activity Breakdown", fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(12.dp))
                            val grouped = state.activities.groupBy { it.activityType }
                            if (grouped.isEmpty()) {
                                Text("No activities yet", color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                            } else {
                                grouped.forEach { (type, logs) ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${activityEmoji(type)} $type",
                                            color = MaterialTheme.colorScheme.onBackground)
                                        Text("${logs.size} session${if (logs.size > 1) "s" else ""}",
                                            color = Purple400, fontWeight = FontWeight.SemiBold)
                                    }
                                    Divider(color = MaterialTheme.colorScheme.outline.copy(0.08f))
                                }
                            }
                        }
                    }

                    // Best day
                    if (bestDay != null && bestDay.second > 0) {
                        item {
                            GlassCard(isDark = isDark, usePurpleTint = true, modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text("🏆", fontSize = 38.sp)
                                    Column {
                                        Text("Best Day This Week", color = MaterialTheme.colorScheme.onBackground.copy(0.6f),
                                            style = MaterialTheme.typography.labelMedium)
                                        Text("${bestDay.first} — %,d kcal".format(bestDay.second),
                                            color = MaterialTheme.colorScheme.onBackground,
                                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}
