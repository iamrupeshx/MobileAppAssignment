package com.smartfit.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.smartfit.app.navigation.Routes
import com.smartfit.app.ui.components.*
import com.smartfit.app.ui.theme.*
import com.smartfit.app.util.CalorieUtil
import com.smartfit.app.viewmodel.AuthViewModel
import com.smartfit.app.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel, authViewModel: AuthViewModel,
    isDark: Boolean, onLogout: () -> Unit, onNavigate: (String) -> Unit
) {
    val state   by viewModel.uiState.collectAsState()
    val user     = state.user
    val context  = LocalContext.current
    
    // Use goals from state (which comes from the User object in DB)
    var stepGoalSlider    by remember(state.stepGoal) { mutableStateOf(state.stepGoal.toFloat()) }
    var waterGoalSlider   by remember(state.waterGoal) { mutableStateOf(state.waterGoal.toFloat()) }
    var calorieGoalSlider by remember(state.calorieGoal) { mutableStateOf(state.calorieGoal.toFloat()) }
    
    var showEditStats by remember { mutableStateOf(false) }
    var showEditDetails by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateProfileImage(context, it) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { SmartFitBottomNav(Routes.PROFILE, isDark, onNavigate) }
    ) { padding ->
        GradientBackground(isDark) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Large Modern Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "My Profile",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Manage your fitness identity",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.5f)
                        )
                    }
                    IconButton(onClick = { showEditDetails = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Orange500)
                    }
                }

                // Profile Card
                GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Orange500, Coral500)))
                                .clickable { photoLauncher.launch("image/*") },
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
                                    fontSize = 36.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            // Edit overlay hint
                            Box(
                                Modifier.fillMaxSize().background(Color.Black.copy(0.2f)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(16.dp).padding(bottom = 4.dp))
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                user?.fullName ?: "Athlete",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                user?.email ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Orange500.copy(0.15f)
                            ) {
                                Text(
                                    user?.gender?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Orange500,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // REDESIGNED BODY STATS SECTION
                if (user != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SectionHeader(
                            title = "Body Composition",
                            isDark = isDark,
                            actionText = "Edit",
                            onAction = { showEditStats = true }
                        )

                        // Stats Grid
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BodyStatCard("Age", "${user.age}", "yrs", "🎂", isDark, Modifier.weight(1f))
                            BodyStatCard("Weight", "%.1f".format(user.weightKg), "kg", "⚖️", isDark, Modifier.weight(1f))
                            BodyStatCard("Height", "%.0f".format(user.heightCm), "cm", "📏", isDark, Modifier.weight(1f))
                        }

                        // Metabolism and BMI Info
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val bmr = CalorieUtil.bmr(user.weightKg, user.heightCm, user.age, user.gender)
                            val bmi = (user.weightKg / ((user.heightCm/100)*(user.heightCm/100)))
                            
                            GlassCard(isDark = isDark, modifier = Modifier.weight(1f), usePurpleTint = true) {
                                Text("Daily BMR", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text("$bmr kcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Orange500)
                            }
                            
                            GlassCard(isDark = isDark, modifier = Modifier.weight(1f)) {
                                Text("Body BMI", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text("%.1f".format(bmi), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = GreenSuccess)
                            }
                        }
                    }
                }

                // Daily Goals Section
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionHeader(title = "Performance Goals", isDark = isDark)
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            GoalSliderItem(
                                label = "Step Target",
                                value = stepGoalSlider,
                                unit = "steps",
                                icon = "🚶",
                                range = 2000f..20_000f,
                                onValueChange = { stepGoalSlider = it; viewModel.updateStepGoal(it.toInt()) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.08f))
                            GoalSliderItem(
                                label = "Water Intake",
                                value = waterGoalSlider,
                                unit = "glasses",
                                icon = "💧",
                                range = 4f..18f,
                                onValueChange = { waterGoalSlider = it; viewModel.updateWaterGoal(it.toInt()) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.08f))
                            GoalSliderItem(
                                label = "Calorie Goal",
                                value = calorieGoalSlider,
                                unit = "kcal",
                                icon = "🍽️",
                                range = 1200f..4000f,
                                onValueChange = { calorieGoalSlider = it; viewModel.updateCalorieGoal(it.toInt()) }
                            )
                        }
                    }
                }

                // Settings / Preferences
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionHeader(title = "Preferences", isDark = isDark)
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                                    Text(if (isDark) "🌙" else "☀️", fontSize = 18.sp)
                                }
                                Column {
                                    Text(if (isDark) "Dark Theme" else "Light Theme", fontWeight = FontWeight.Bold)
                                    Text("Switch interface look", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                                }
                            }
                            Switch(
                                checked = state.isDarkTheme,
                                onCheckedChange = { viewModel.toggleTheme() },
                                colors = SwitchDefaults.colors(checkedTrackColor = Orange500)
                            )
                        }
                    }
                }

                // Danger Zone
                GradientButton(
                    text = "Log Out Account",
                    onClick = { authViewModel.logout(); onLogout() },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    gradient = listOf(RedError.copy(0.8f), RedError)
                )

                Spacer(Modifier.height(100.dp))
            }
        }
    }

    if (showEditStats && user != null) {
        EditStatsDialog(
            user = user,
            isDark = isDark,
            onDismiss = { showEditStats = false },
            onSave = { age, weight, height ->
                viewModel.updateBodyStats(age, weight, height)
                showEditStats = false
            }
        )
    }

    if (showEditDetails && user != null) {
        EditDetailsDialog(
            user = user,
            isDark = isDark,
            onDismiss = { showEditDetails = false },
            onSave = { name, email, gender ->
                viewModel.updateProfileDetails(name, email, gender)
                showEditDetails = false
            }
        )
    }
}

@Composable
private fun BodyStatCard(label: String, value: String, unit: String, icon: String, isDark: Boolean, modifier: Modifier) {
    val bg = if (isDark) Color.White.copy(0.06f) else Color.Black.copy(0.03f)
    Box(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column {
            Box(Modifier.size(32.dp).background(Orange500.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 16.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text("$label ($unit)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
        }
    }
}

@Composable
private fun GoalSliderItem(label: String, value: Float, unit: String, icon: String, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(icon, fontSize = 20.sp)
                Text(label, fontWeight = FontWeight.Bold)
            }
            Text("${value.toInt()} $unit", color = Orange500, fontWeight = FontWeight.Black)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = Orange500, activeTrackColor = Orange500)
        )
    }
}

@Composable
fun EditStatsDialog(
    user: com.smartfit.app.data.model.User,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSave: (Int, Float, Float) -> Unit
) {
    var age by remember { mutableStateOf(user.age.toString()) }
    var weight by remember { mutableStateOf(user.weightKg.toString()) }
    var height by remember { mutableStateOf(user.heightCm.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
            Text("Update Stats", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Enter your current body measurements", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
            Spacer(Modifier.height(24.dp))
            
            GlassTextField(value = age, onValueChange = { age = it }, label = "Age", keyboardType = androidx.compose.ui.text.input.KeyboardType.Number, isDark = isDark)
            Spacer(Modifier.height(12.dp))
            GlassTextField(value = weight, onValueChange = { weight = it }, label = "Weight (kg)", keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal, isDark = isDark)
            Spacer(Modifier.height(12.dp))
            GlassTextField(value = height, onValueChange = { height = it }, label = "Height (cm)", keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal, isDark = isDark)
            
            Spacer(Modifier.height(30.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
                }
                GradientButton(text = "Save Changes", onClick = {
                    val a = age.toIntOrNull() ?: user.age
                    val w = weight.toFloatOrNull() ?: user.weightKg
                    val h = height.toFloatOrNull() ?: user.heightCm
                    onSave(a, w, h)
                }, modifier = Modifier.weight(1.5f))
            }
        }
    }
}

@Composable
fun EditDetailsDialog(
    user: com.smartfit.app.data.model.User,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(user.fullName) }
    var email by remember { mutableStateOf(user.email) }
    var gender by remember { mutableStateOf(user.gender) }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
            Text("Edit Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Update your personal information", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
            Spacer(Modifier.height(24.dp))
            
            GlassTextField(value = name, onValueChange = { name = it }, label = "Full Name", isDark = isDark)
            Spacer(Modifier.height(12.dp))
            GlassTextField(value = email, onValueChange = { email = it }, label = "Email", keyboardType = androidx.compose.ui.text.input.KeyboardType.Email, isDark = isDark)
            Spacer(Modifier.height(12.dp))
            
            Text("Gender", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("male", "female", "other").forEach { g ->
                    val selected = gender == g
                    Surface(
                        onClick = { gender = g },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) Orange500 else (if (isDark) Color.White.copy(0.05f) else Color.Black.copy(0.05f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            g.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onBackground,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(30.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
                }
                GradientButton(text = "Update", onClick = {
                    onSave(name, email, gender)
                }, modifier = Modifier.weight(1.5f))
            }
        }
    }
}
