package com.smartfit.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import com.smartfit.app.ui.components.*
import com.smartfit.app.ui.theme.*
import com.smartfit.app.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel, isDark: Boolean,
    onRegisterSuccess: () -> Unit, onLoginClick: () -> Unit
) {
    val uiState         by viewModel.uiState.collectAsState()
    var fullName        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var age             by remember { mutableStateOf("") }
    var weightKg        by remember { mutableStateOf("") }
    var heightCm        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPass        by remember { mutableStateOf(false) }
    var stepGoal        by remember { mutableStateOf(10000f) }
    var selectedGender  by remember { mutableStateOf("male") }
    var visible         by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { kotlinx.coroutines.delay(60); visible = true }
    LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) { viewModel.resetSuccess(); onRegisterSuccess() } }

    GradientBackground(isDark) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ///Spacer(Modifier.height(52.dp))

            AnimatedVisibility(visible, enter = slideInVertically { -it } + fadeIn()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🚀", fontSize = 52.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Create Account", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Start your fitness journey today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.55f))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Error banner
            AnimatedVisibility(uiState.errorMessage != null,
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut()
            ) {
                Column {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(RedError.copy(0.12f))
                        .border(1.dp, RedError.copy(0.35f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Error",
                            tint = RedError, modifier = Modifier.size(18.dp))
                        Text(uiState.errorMessage ?: "", color = RedError,
                            style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }

            // Personal Info
            AnimatedVisibility(visible, enter = slideInVertically(tween(500, 100)) { it/2 } + fadeIn(tween(500, 100))) {
                GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                    Text("Personal Info", color = Purple500, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(14.dp))
                    GlassTextField(fullName, { fullName = it }, "Full Name", isDark = isDark,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name", tint = Purple500) })
                    Spacer(Modifier.height(12.dp))
                    GlassTextField(email, { email = it }, "Email Address",
                        keyboardType = KeyboardType.Email, isDark = isDark,
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Purple500) })
                    Spacer(Modifier.height(12.dp))
                    GlassTextField(age, { age = it }, "Age",
                        keyboardType = KeyboardType.Number, isDark = isDark,
                        leadingIcon = { Icon(Icons.Default.Cake, contentDescription = "Age", tint = Purple500) })
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassTextField(weightKg, { weightKg = it }, "Weight (kg)",
                            keyboardType = KeyboardType.Decimal, isDark = isDark, modifier = Modifier.weight(1f))
                        GlassTextField(heightCm, { heightCm = it }, "Height (cm)",
                            keyboardType = KeyboardType.Decimal, isDark = isDark, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Gender", color = Purple500, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("male" to "👨 Male", "female" to "👩 Female").forEach { (v, label) ->
                            val sel = selectedGender == v
                            Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(if (sel) Purple500.copy(0.2f) else Color.Transparent)
                                .border(if (sel) 2.dp else 1.dp,
                                    if (sel) Purple500 else MaterialTheme.colorScheme.outline.copy(0.4f),
                                    RoundedCornerShape(12.dp))
                                .clickable { selectedGender = v }.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            // Step goal

            Spacer(Modifier.height(14.dp))

            // Security
            AnimatedVisibility(visible, enter = slideInVertically(tween(500, 300)) { it/2 } + fadeIn(tween(500, 300))) {
                GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                    Text("Security", color = Purple500, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(14.dp))
                    GlassTextField(password, { password = it }, "Password (min 6 chars)",
                        isPassword = !showPass, isDark = isDark,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = Purple500) },
                        trailingIcon = {
                            IconButton({ showPass = !showPass }) {
                                Icon(if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                            }
                        })
                    Spacer(Modifier.height(12.dp))
                    GlassTextField(confirmPassword, { confirmPassword = it }, "Confirm Password",
                        isPassword = !showPass, isDark = isDark,
                        isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                        leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = "Confirm password", tint = Purple500) })
                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Spacer(Modifier.height(4.dp))
                        Text("Passwords do not match", color = RedError, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(22.dp))
                    GradientButton("CREATE ACCOUNT", {
                        viewModel.register(fullName, email, password, confirmPassword,
                            age.toIntOrNull() ?: 0, weightKg.toFloatOrNull() ?: 0f,
                            heightCm.toFloatOrNull() ?: 0f, selectedGender, stepGoal.toInt())
                    }, Modifier.fillMaxWidth(), isLoading = uiState.isLoading)
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = MaterialTheme.colorScheme.onBackground.copy(0.55f),
                    style = MaterialTheme.typography.bodyMedium)
                Text("Login Here", color = Purple500, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.clickable { onLoginClick() })
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}
