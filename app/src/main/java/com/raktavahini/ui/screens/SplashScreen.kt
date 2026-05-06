package com.raktavahini.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktavahini.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import com.raktavahini.R

@Composable
fun SplashScreen(
    onNavigate: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500)
        onNavigate()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Primary.copy(alpha = 0.4f),
                        Background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background elements
        BackgroundOrbs()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Heartbeat pulse circles
                PulseCircles()

                Icon(
                    imageVector = Icons.Default.Bloodtype,
                    contentDescription = null,
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scaleAnim * pulseScale),
                    tint = Primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = OnSurface.copy(alpha = alphaAnim)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.emergency_blood_search),
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant.copy(alpha = alphaAnim * 0.7f)
            )

            Spacer(modifier = Modifier.height(64.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = Primary,
                strokeWidth = 4.dp
            )
        }
    }
}

@Composable
fun PulseCircles() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_circles")
    val radius by infiniteTransition.animateFloat(
        initialValue = 100f,
        targetValue = 250f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Canvas(modifier = Modifier.size(200.dp)) {
        drawCircle(
            color = Primary,
            radius = radius,
            alpha = alpha,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun BackgroundOrbs() {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Primary.copy(alpha = 0.05f),
                radius = 400f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.2f)
            )
            drawCircle(
                color = AccentBlue.copy(alpha = 0.05f),
                radius = 300f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.8f)
            )
        }
    }
}