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
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.smartfit.app.ui.components.*
import com.smartfit.app.ui.theme.*
import com.smartfit.app.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    viewModel: AuthViewModel, isDark: Boolean,
    onLoginSuccess: () -> Unit, onRegisterClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var trigger  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(60); trigger = true }
    LaunchedEffect(email, password) { if (uiState.errorMessage != null) viewModel.clearError() }
    LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) { viewModel.resetSuccess(); onLoginSuccess() } }

    val offsetY by animateIntAsState(if (trigger) 0 else 80, tween(700, easing = FastOutSlowInEasing), label = "y")
    val alpha   by animateFloatAsState(if (trigger) 1f else 0f, tween(700), label = "a")

    GradientBackground(isDark) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(24.dp).offset(y = offsetY.dp).alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))
            Text("💪", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text("Welcome Back", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
            Text("Sign in to continue your journey",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.55f))
            Spacer(Modifier.height(36.dp))

            AnimatedVisibility(uiState.errorMessage != null,
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut()
            ) {
                Column {
                    Row(Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
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

            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                GlassTextField(email, { email = it }, "Email Address",
                    keyboardType = KeyboardType.Email, isDark = isDark,
                    isError = uiState.errorMessage != null,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Purple500) })
                Spacer(Modifier.height(14.dp))
                GlassTextField(password, { password = it }, "Password",
                    isPassword = !showPass, isDark = isDark,
                    isError = uiState.errorMessage != null,
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = Purple500) },
                    trailingIcon = {
                        IconButton({ showPass = !showPass }) {
                            Icon(if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPass) "Hide password" else "Show password",
                                tint = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                    })
                Spacer(Modifier.height(22.dp))
                GradientButton("SIGN IN", { viewModel.login(email.trim(), password) },
                    Modifier.fillMaxWidth(), isLoading = uiState.isLoading)
            }

            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", color = MaterialTheme.colorScheme.onBackground.copy(0.55f),
                    style = MaterialTheme.typography.bodyMedium)
                Text("Register Here", color = Purple500, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { onRegisterClick() })
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
