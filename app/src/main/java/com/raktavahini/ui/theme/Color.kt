package com.raktavahini.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium Blood Red Palette
val Primary = Color(0xFFD32F2F)
val PrimaryDark = Color(0xFF9A0007)
val PrimaryLight = Color(0xFFFF6659)

// Sophisticated Accents
val AccentGreen = Color(0xFF00C853)
val AccentBlue = Color(0xFF2979FF)
val AccentGold = Color(0xFFFFD600)
val AccentRed = Color(0xFFFF5252)

// Modern Grays & Backgrounds
val Background = Color(0xFF0F0F0F) // Sleek Dark Mode by default
val Surface = Color(0xFF1E1E1E)
val SurfaceVariant = Color(0xFF2C2C2C)

// Content Colors
val OnPrimary = Color(0xFFFFFFFF)
val OnSurface = Color(0xFFF5F5F5)
val OnSurfaceVariant = Color(0xFFBDBDBD)

// Glassmorphism & Overlays
val GlassWhite = Color(0x1AFFFFFF)
val GlassPrimary = Color(0x33D32F2F)

// Premium Gradients
val PrimaryGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFF5252), Color(0xFFD32F2F))
)

val SuccessGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00E676), Color(0xFF00C853))
)

val SurfaceGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF2C2C2C), Color(0xFF1E1E1E))
)