package com.smartfit.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.smartfit.app.R
import com.smartfit.app.ui.components.GradientBackground
import com.smartfit.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigate: () -> Unit) {
    var trigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); trigger = true; delay(2600); onNavigate() }

    val scale by animateFloatAsState(if (trigger) 1f else 0.2f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "scale")
    val alpha by animateFloatAsState(if (trigger) 1f else 0f, tween(800), label = "alpha")
    val tagAlpha by animateFloatAsState(if (trigger) 1f else 0f, tween(800, delayMillis = 500), label = "tag")
    val lineAlpha by animateFloatAsState(if (trigger) 1f else 0f, tween(600, delayMillis = 900), label = "line")

    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(1f, 1.06f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pulse")

    GradientBackground(isDark = true) {
        Column(Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High-quality App Logo with animation
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale * pulseScale)
                    .clip(CircleShape)

                    ,
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "SmartFit Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            //Spacer(Modifier.height(28.dp))
            
            Text("SMARTFIT", style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black, color = Color.White.copy(alpha = alpha),
                letterSpacing = 10.sp)
            
            Spacer(Modifier.height(8.dp))
            
            Text("Track · Move · Fuel · Win",
                style = MaterialTheme.typography.titleSmall,
                color = Purple400.copy(alpha = tagAlpha), letterSpacing = 2.sp)
            
            Spacer(Modifier.height(56.dp))

            CircularProgressIndicator(color = Purple500.copy(alpha = lineAlpha),
                modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
        }
    }
}
