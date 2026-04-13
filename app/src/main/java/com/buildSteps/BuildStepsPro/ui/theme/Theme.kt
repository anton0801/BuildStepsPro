package com.buildSteps.BuildStepsPro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand Colors ──────────────────────────────────────────────────────────
val BluePrimary      = Color(0xFF1D4ED8)
val BlueLight        = Color(0xFF3B82F6)
val BlueDark         = Color(0xFF1E3A8A)
val CyanAccent       = Color(0xFF06B6D4)
val IndigoDeep       = Color(0xFF312E81)

val BackgroundLight  = Color(0xFFF8FAFF)
val SurfaceLight     = Color(0xFFFFFFFF)
val CardLight        = Color(0xFFF1F5FD)
val BorderLight      = Color(0xFFDEE7F5)

val TextPrimary      = Color(0xFF0F172A)
val TextSecondary    = Color(0xFF475569)
val TextHint         = Color(0xFF94A3B8)

val GreenSuccess     = Color(0xFF10B981)
val AmberWarning     = Color(0xFFF59E0B)
val RedError         = Color(0xFFEF4444)
val PurpleCategory   = Color(0xFF8B5CF6)

val GradientStart    = Color(0xFF1D4ED8)
val GradientMid      = Color(0xFF3B82F6)
val GradientEnd      = Color(0xFF06B6D4)

val DarkBackground   = Color(0xFF0A0F1E)
val DarkSurface      = Color(0xFF111827)
val DarkCard         = Color(0xFF1F2937)

private val LightColors = lightColorScheme(
    primary          = BluePrimary,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = BlueDark,
    secondary        = CyanAccent,
    onSecondary      = Color.White,
    background       = BackgroundLight,
    onBackground     = TextPrimary,
    surface          = SurfaceLight,
    onSurface        = TextPrimary,
    surfaceVariant   = CardLight,
    onSurfaceVariant = TextSecondary,
    error            = RedError,
    onError          = Color.White,
    outline          = BorderLight
)

@Composable
fun BuildStepsProTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = AppTypography,
        content     = content
    )
}
