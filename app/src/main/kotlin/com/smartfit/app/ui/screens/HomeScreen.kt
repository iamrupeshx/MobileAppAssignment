package com.smartfit.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.smartfit.app.navigation.Routes
import com.smartfit.app.ui.components.*
import com.smartfit.app.ui.theme.*
import com.smartfit.app.viewmodel.ActivityViewModel
import com.smartfit.app.viewmodel.ProfileViewModel

@Composable
fun HomeScreen(
    viewModel: ActivityViewModel,
    profileVm: ProfileViewModel,
    isDark: Boolean,
    onNavigate: (String) -> Unit
) {
    val state   by viewModel.uiState.collectAsState()
    val profState by profileVm.uiState.collectAsState()
    val user      = profState.user
    val stepGoal  = profState.stepGoal.takeIf { it > 0 } ?: 10_000
    val calGoal   = profState.calorieGoal.takeIf { it > 0 } ?: 2200
    val waterGoal = profState.waterGoal.takeIf { it > 0 } ?: 8

    val netCalories  = state.todayCaloriesConsumed - state.todayCaloriesBurned
    val remainingCal = calGoal - state.todayCaloriesConsumed + state.todayCaloriesBurned
    val stepProgress = (state.todaySteps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1.2f)
    val calProgress  = (state.todayCaloriesConsumed.toFloat() / calGoal.toFloat()).coerceIn(0f, 1f)

    // Count-up animation for stats
    val animSteps by animateIntAsState(state.todaySteps, tween(1200), label = "steps")
    val animBurned by animateIntAsState(state.todayCaloriesBurned, tween(1000), label = "burned")
    val animConsumed by animateIntAsState(state.todayCaloriesConsumed, tween(1000), label = "consumed")
    val animRemaining by animateIntAsState(remainingCal, tween(1000), label = "remaining")

    Scaffold(containerColor = Color.Transparent,
        bottomBar = { SmartFitBottomNav(Routes.HOME, isDark, onNavigate) }
    ) { padding ->
        GradientBackground(isDark) {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // User Photo
                            Box(
                                Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Orange500, Coral500)))
                                    .clickable { onNavigate(Routes.PROFILE) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (user?.profileImageUri != null) {
                                    AsyncImage(
                                        model = user.profileImageUri,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        user?.fullName?.firstOrNull()?.uppercase() ?: "?",
                                        fontSize = 20.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Column {
                                Text("Good ${greeting()} 👋",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
                                Text(user?.fullName?.split(" ")?.firstOrNull() ?: "Athlete",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                        Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                            .semantics { contentDescription = "Notifications" }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications",
                                tint = Purple500, modifier = Modifier.size(26.dp).align(Alignment.Center))
                        }
                    }
                }

                // NOW LARGE STEP COUNTER (TOP POSITION)
                item {
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Step Counter", style = MaterialTheme.typography.titleSmall, 
                                    fontWeight = FontWeight.Black, color = Purple500)
                                
                                if (stepProgress >= 1f) {
                                    Surface(color = GreenSuccess.copy(0.1f), shape = RoundedCornerShape(12.dp)) {
                                        Text("Goal Reached! 🏆", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall, color = GreenSuccess, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            Spacer(Modifier.height(24.dp))

                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                                CircularProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.size(200.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(0.06f),
                                    strokeWidth = 16.dp,
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                CircularProgressIndicator(
                                    progress = { stepProgress.coerceAtMost(1f) },
                                    modifier = Modifier.size(200.dp),
                                    color = Purple500,
                                    strokeWidth = 16.dp,
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("%,d".format(animSteps), 
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onBackground)
                                    Text("Steps Today", 
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground.copy(0.4f))
                                }
                            }
                            
                            Spacer(Modifier.height(32.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Flag, null, tint = RedError, modifier = Modifier.size(18.dp))
                                    Text("%,d".format(stepGoal), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                                    Text("Goal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(0.4f))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.AutoMirrored.Filled.DirectionsRun, null, tint = Cyan500, modifier = Modifier.size(18.dp))
                                    Text("${state.todaySteps / 1300} km", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                                    Text("Dist", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(0.4f))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.LocalFireDepartment, null, tint = Orange500, modifier = Modifier.size(18.dp))
                                    Text("$animBurned", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                                    Text("Burned", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(0.4f))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Purple500, modifier = Modifier.size(18.dp))
                                    Text("${(stepProgress * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                                    Text("Progress", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(0.4f))
                                }
                            }
                            
                            Spacer(Modifier.height(24.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.08f))
                            Spacer(Modifier.height(16.dp))

                            // Hydration Panel
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(42.dp).clip(CircleShape).background(GradWater[0].copy(0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.WaterDrop, null, tint = GradWater[0], modifier = Modifier.size(22.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Hydration", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        
                                        // Greeting logic with animation
                                        AnimatedContent(
                                            targetState = when {
                                                state.waterGlasses >= waterGoal -> "Great Job! 💧"
                                                state.waterGlasses >= 6 -> "Almost there! 😊"
                                                else -> null
                                            },
                                            transitionSpec = { fadeIn() + slideInVertically() togetherWith fadeOut() },
                                            label = "hydration_greet"
                                        ) { greet ->
                                            if (greet != null) {
                                                Surface(color = GradWater[0].copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                                                    Text(greet, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall, color = GradWater[0], fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                    LinearProgressIndicator(
                                        progress = { (state.waterGlasses.toFloat() / waterGoal.toFloat()).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                        color = GradWater[0],
                                        trackColor = MaterialTheme.colorScheme.outline.copy(0.06f)
                                    )
                                    Text("${state.waterGlasses}/$waterGoal glasses", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GradWater[0])
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.removeWaterGlass() }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Remove, null, tint = MaterialTheme.colorScheme.onBackground.copy(0.6f))
                                    }
                                    IconButton(onClick = { viewModel.addWaterGlass() }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Add, null, tint = GradWater[0])
                                    }
                                }
                            }
                        }
                    }
                }

                // NOW COMPACT NUTRITION BALANCE (BELOW POSITION)
                item {
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                                CircularProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.size(100.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(0.06f),
                                    strokeWidth = 10.dp,
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                CircularProgressIndicator(
                                    progress = { calProgress },
                                    modifier = Modifier.size(100.dp),
                                    color = Emerald500,
                                    strokeWidth = 10.dp,
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )

                                Text("${(calProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Nutrition Balance", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Emerald500)
                                    IconButton(onClick = { onNavigate(Routes.FOOD_LOG) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.ChevronRight, null, tint = Emerald500, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text("$animRemaining",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground)
                                Text("kcal remaining of $calGoal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                            }
                        }
                    }
                }

                // REDESIGNED DETAILED METRICS
                item {
                    SectionHeader("Detailed Metrics", isDark)
                    Spacer(Modifier.height(8.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricBox("Activity", "%,d".format(animBurned), "kcal", Icons.Default.LocalFireDepartment, GradCalorie, Modifier.weight(1f)) { onNavigate(Routes.ACTIVITY_LOG) }
                            MetricBox("Nutrition", "%,d".format(animConsumed), "kcal", Icons.Default.Restaurant, GradFood, Modifier.weight(1f)) { onNavigate(Routes.FOOD_LOG) }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricBox("Step Counter", "%,d".format(animSteps), "steps", Icons.AutoMirrored.Filled.DirectionsRun, GradSteps, Modifier.weight(1f))
                            MetricBox("Net Balance", "$netCalories", "kcal", Icons.Default.Analytics, GradActive, Modifier.weight(1f))
                        }
                    }
                }

                // Quick actions
                item {
                    SectionHeader("Quick Actions", isDark)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickActionCard("💪", "Workout", GradSteps, Modifier.weight(1f))
                            { onNavigate(Routes.ADD_ACTIVITY) }
                        QuickActionCard("🍽️", "Meal",    GradFood,  Modifier.weight(1f))
                            { onNavigate(Routes.ADD_FOOD) }
                        QuickActionCard("📊", "Analysis",     GradActive, Modifier.weight(1f))
                            { onNavigate(Routes.SUMMARY) }
                        QuickActionCard("🏆", "Plan",    GradNet,    Modifier.weight(1f))
                            { onNavigate(Routes.SUGGESTIONS) }
                    }
                }

                // Recent activities
                if (state.activities.isNotEmpty()) {
                    item { SectionHeader("Recent Activities", isDark, "View All") { onNavigate(Routes.ACTIVITY_LOG) } }
                    items(state.activities.take(3)) { activity ->
                        GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(activityEmoji(activity.activityType), fontSize = 28.sp)
                                    Column {
                                        Text(activity.activityType, fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground)
                                        Text("${activity.durationMinutes} min",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                                    }
                                }
                                Text("${activity.caloriesBurned} kcal",
                                    color = OrangeCal, fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.06f), RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp)
    ) {
        Column {
            Box(Modifier.size(32.dp).background(Brush.linearGradient(gradient), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                Text(" · $unit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(0.3f))
            }
        }
    }
}

@Composable
private fun QuickActionCard(icon: String, label: String, gradient: List<Color>, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(16.dp))
        .background(androidx.compose.ui.graphics.Brush.verticalGradient(gradient))
        .clickable { onClick() }.padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = Color.White, fontWeight = FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

private fun greeting(): String {
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when { h < 12 -> "Morning"; h < 17 -> "Afternoon"; else -> "Evening" }
}

fun activityEmoji(type: String) = when (type.lowercase()) {
    "running"         -> "🏃"; "walking"          -> "🚶"; "cycling"         -> "🚴"
    "swimming"        -> "🏊"; "weight training"   -> "🏋️"; "yoga"            -> "🧘"
    "hiit"            -> "⚡"; "basketball"        -> "🏀"; "football"        -> "⚽"
    "badminton"       -> "🏸"; else               -> "🤸"
}
