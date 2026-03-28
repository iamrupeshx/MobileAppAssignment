package com.smartfit.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import com.smartfit.app.navigation.Routes
import com.smartfit.app.ui.theme.*
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════════════
// GRADIENT BACKGROUND — theme-aware
// ═══════════════════════════════════════════════════════════════════
@Composable
fun GradientBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val brush = if (isDark) {
        Brush.verticalGradient(listOf(Black0, Black1, Black2, Black3, Black2))
    } else {
        Brush.verticalGradient(listOf(Lavender0, White0, Lavender1, Lavender0))
    }
    Box(modifier = modifier.fillMaxSize().background(brush), content = content)
}

// ═══════════════════════════════════════════════════════════════════
// iOS GLASSMORPHISM CARD
// ═══════════════════════════════════════════════════════════════════
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    cornerRadius: Dp = 24.dp,
    usePurpleTint: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val bgColor = when {
        usePurpleTint && isDark  -> GlassPurple
        usePurpleTint && !isDark -> GlassLightPu
        isDark                   -> GlassDark
        else                     -> GlassLight
    }
    val borderBrush = if (isDark) {
        Brush.linearGradient(listOf(
            Color.White.copy(0.35f), Color.White.copy(0.05f),
            Purple500.copy(0.2f),   Color.White.copy(0.05f)
        ))
    } else {
        // High visibility for Light Mode
        Brush.linearGradient(listOf(
            Purple700.copy(0.25f), Color.Black.copy(0.12f),
            Purple500.copy(0.3f),  Color.Black.copy(0.12f)
        ))
    }
    // Stronger shadows for better definition
    val shadowColor = if (isDark) Purple500.copy(0.15f) else Color.Black.copy(0.08f)

    Box(
        modifier = modifier
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(cornerRadius),
                ambientColor = shadowColor, spotColor = shadowColor)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .border(width = 1.2.dp, brush = borderBrush, shape = RoundedCornerShape(cornerRadius))
    ) {
        // Top highlight line — the key iOS glass shimmer
        Box(modifier = Modifier.fillMaxWidth().height(1.dp)
            .background(Brush.horizontalGradient(listOf(
                Color.Transparent,
                Color.White.copy(if (isDark) 0.5f else 0.95f),
                Color.Transparent
            )))
        )
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

// ═══════════════════════════════════════════════════════════════════
// BOTTOM NAVIGATION BAR
// ═══════════════════════════════════════════════════════════════════
data class NavItem(val route: String, val icon: ImageVector, val label: String)

@Composable
fun SmartFitBottomNav(currentRoute: String?, isDark: Boolean, onNavigate: (String) -> Unit) {
    val items = listOf(
        NavItem(Routes.HOME,         Icons.Default.Home,          "Home"),
        NavItem(Routes.ACTIVITY_LOG, Icons.Default.FitnessCenter, "Workout"),
        NavItem(Routes.FOOD_LOG,     Icons.Default.Restaurant,    "Nutrition"),
        NavItem(Routes.SUMMARY,      Icons.Default.BarChart,      "Summary"),
        NavItem(Routes.PROFILE,      Icons.Default.Person,        "Profile"),
    )
    val barBg     = if (isDark) Color(0xE6000000) else Color(0xF0FFFFFF)
    // Darker border for light mode
    val barBorder = if (isDark) Color(0x30FFFFFF) else Color(0x20000000)

    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(barBg)
            .border(BorderStroke(1.2.dp, barBorder),
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .navigationBarsPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val iconColor by animateColorAsState(
                    if (selected) Purple500 else MaterialTheme.colorScheme.onSurface.copy(0.38f),
                    tween(250), label = "nav_color"
                )
                val bgAlpha by animateFloatAsState(
                    if (selected) 1f else 0f, tween(250), label = "nav_bg"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(item.route) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .semantics { contentDescription = "Navigate to ${item.label}" }
                ) {
                    Box(
                        modifier = Modifier.size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Purple500.copy(alpha = bgAlpha * 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.icon, contentDescription = item.label,
                            tint = iconColor, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(item.label, style = MaterialTheme.typography.labelSmall,
                        color = iconColor,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// CIRCULAR PROGRESS RING
// ═══════════════════════════════════════════════════════════════════
@Composable
fun AnimatedCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 12.dp,
    centerValue: String = "",
    centerLabel: String = "",
    isDark: Boolean = true,
    colors: List<Color> = listOf(Purple400, Purple500, Purple600)
) {
    val animProg by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        tween(1600, easing = FastOutSlowInEasing), label = "ring"
    )
    val trackColor = if (isDark) Color.White.copy(0.07f) else Purple700.copy(0.08f)

    Box(modifier = modifier.size(size)
        .semantics { contentDescription = "Goal progress: ${(progress*100).toInt()}%" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke  = strokeWidth.toPx()
            val diam    = this.size.minDimension - stroke
            val topLeft = Offset((this.size.width - diam) / 2f, (this.size.height - diam) / 2f)
            val arcSize = Size(diam, diam)
            val style   = Stroke(stroke, cap = StrokeCap.Round)
            drawArc(trackColor, -90f, 360f, false, topLeft, arcSize, style = style)
            if (animProg > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(colors + colors.first(),
                        Offset(this.size.width / 2f, this.size.height / 2f)),
                    startAngle = -90f, sweepAngle = 360f * animProg,
                    useCenter = false, topLeft = topLeft, size = arcSize, style = style
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (centerValue.isNotEmpty())
                Text(centerValue, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground)
            if (centerLabel.isNotEmpty())
                Text(centerLabel, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.55f))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// STAT CARD
// ═══════════════════════════════════════════════════════════════════
@Composable
fun StatCard(
    icon: String,
    label: String,
    value: String,
    unit: String = "",
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradientColors))
            .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp)
            .semantics { contentDescription = "$label $value $unit" }
    ) {
        Column {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(Modifier.height(2.dp))
            Text(if (unit.isNotEmpty()) "$label · $unit" else label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(0.78f))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// GLASS TEXT FIELD
// ═══════════════════════════════════════════════════════════════════
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    singleLine: Boolean = true,
    isDark: Boolean = true
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon, trailingIcon = trailingIcon,
        singleLine = singleLine, isError = isError,
        visualTransformation = if (isPassword) PasswordVisualTransformation()
            else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor     = Purple500,
            // Even more visible border for Light Mode
            unfocusedBorderColor   = if (isDark) Color.White.copy(0.18f) else Purple700.copy(0.6f),
            focusedLabelColor      = Purple500,
            unfocusedLabelColor    = MaterialTheme.colorScheme.onSurface.copy(0.5f),
            cursorColor            = Purple500,
            focusedTextColor       = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor     = MaterialTheme.colorScheme.onBackground,
            errorBorderColor       = RedError,
            errorLabelColor        = RedError,
            focusedLeadingIconColor   = Purple500,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(0.5f),
            focusedContainerColor    = if (isDark) GlassDarkMd else GlassLight.copy(0.5f),
            unfocusedContainerColor  = if (isDark) Color(0x08FFFFFF) else GlassLight.copy(0.3f),
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    )
}

// ═══════════════════════════════════════════════════════════════════
// GRADIENT BUTTON
// ═══════════════════════════════════════════════════════════════════
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    gradient: List<Color> = listOf(Purple500, Purple700)
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed || isLoading) 0.96f else 1f,
        spring(Spring.DampingRatioMediumBouncy), label = "btn_scale"
    )
    Box(
        modifier = modifier.scale(scale)
            .clip(RoundedCornerShape(50))
            .background(
                if (enabled) Brush.linearGradient(gradient)
                else Brush.linearGradient(listOf(Color.Gray.copy(0.3f), Color.Gray.copy(0.2f)))
            )
            .border(1.dp,
                if (enabled) Purple400.copy(0.45f) else Color.Gray.copy(0.2f),
                RoundedCornerShape(50))
            .clickable(enabled = enabled && !isLoading) { pressed = true; onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White,
                modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
        } else {
            Text(text, color = if (enabled) Color.White else Color.Gray.copy(0.6f),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
        }
    }
    LaunchedEffect(pressed) { if (pressed) { delay(150); pressed = false } }
}

// ═══════════════════════════════════════════════════════════════════
// ANIMATED FAB
// ═══════════════════════════════════════════════════════════════════
@Composable
fun AnimatedFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Add,
    contentDescription: String = "Add"
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) 0.85f else 1f,
        spring(Spring.DampingRatioMediumBouncy), label = "fab"
    )
    Box(
        modifier = modifier.scale(scale).size(58.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Purple500, Purple700)))
            .border(1.dp, Purple400.copy(0.4f), CircleShape)
            .clickable { pressed = true; onClick() }
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
    }
    LaunchedEffect(pressed) { if (pressed) { delay(150); pressed = false } }
}

// ═══════════════════════════════════════════════════════════════════
// SECTION HEADER
// ═══════════════════════════════════════════════════════════════════
@Composable
fun SectionHeader(title: String, isDark: Boolean, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        if (actionText != null && onAction != null) {
            Text(actionText, style = MaterialTheme.typography.labelMedium,
                color = Purple500, modifier = Modifier.clickable { onAction() })
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// SHIMMER LOADING PLACEHOLDER
// ═══════════════════════════════════════════════════════════════════
@Composable
fun ShimmerBox(modifier: Modifier = Modifier, isDark: Boolean = true) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        0f, 1000f, infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer_x"
    )
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))
        .background(Brush.linearGradient(
            listOf(
                if (isDark) Color.White.copy(0.05f) else Color.Black.copy(0.04f),
                if (isDark) Color.White.copy(0.12f) else Color.Black.copy(0.10f),
                if (isDark) Color.White.copy(0.05f) else Color.Black.copy(0.04f),
            ),
            start = Offset(shimmerX - 300f, 0f),
            end   = Offset(shimmerX, 0f)
        ))
    )
}

// ═══════════════════════════════════════════════════════════════════
// WATER RING PROGRESS
// ═══════════════════════════════════════════════════════════════════
@Composable
fun WaterProgressRing(current: Int, goal: Int, isDark: Boolean, modifier: Modifier = Modifier) {
    val progress = (current.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    AnimatedCircularProgress(
        progress     = progress,
        modifier     = modifier,
        size         = 130.dp,
        strokeWidth  = 10.dp,
        centerValue  = "$current",
        centerLabel  = "/ $goal glasses",
        isDark       = isDark,
        colors       = listOf(Color(0xFF0EA5E9), Color(0xFF38BDF8), Color(0xFF0284C7))
    )
}
