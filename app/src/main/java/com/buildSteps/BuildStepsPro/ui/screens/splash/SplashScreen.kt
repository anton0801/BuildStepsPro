package com.buildSteps.BuildStepsPro.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.buildSteps.BuildStepsPro.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var animStarted by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (animStarted) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "logo_scale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (animStarted) 1f else 0f,
        animationSpec = tween(700), label = "logo_alpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (animStarted) 1f else 0f,
        animationSpec = tween(700, delayMillis = 400), label = "text_alpha"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (animStarted) 1f else 0f,
        animationSpec = tween(600, delayMillis = 700), label = "subtitle_alpha"
    )

    // Particle animation
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "particle_orbit"
    )

    LaunchedEffect(Unit) {
        animStarted = true
        delay(2400)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D1B4B),
                        Color(0xFF1D4ED8),
                        Color(0xFF06B6D4)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background circles / particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawParticles(particleOffset, size)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo container
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                        ),
                        CircleShape
                    )
                    .border(2.dp, Color.White.copy(0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(72.dp)) {
                    drawBuildStepsLogo()
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Build Steps Pro",
                modifier = Modifier.alpha(textAlpha),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Plan your repair correctly",
                modifier = Modifier.alpha(subtitleAlpha),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    letterSpacing = 0.3.sp
                ),
                textAlign = TextAlign.Center
            )
        }

        // Bottom loading dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .alpha(subtitleAlpha),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { i ->
                val dotAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        tween(600, delayMillis = i * 200),
                        RepeatMode.Reverse
                    ),
                    label = "dot_$i"
                )
                Box(
                    Modifier
                        .size(8.dp)
                        .alpha(dotAlpha)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

private fun DrawScope.drawBuildStepsLogo() {
    val nodeColor = Color.White
    val lineColor = Color.White.copy(alpha = 0.7f)
    val w = size.width
    val h = size.height

    val nodes = listOf(
        Offset(w * 0.15f, h * 0.5f),
        Offset(w * 0.45f, h * 0.2f),
        Offset(w * 0.45f, h * 0.8f),
        Offset(w * 0.8f, h * 0.5f)
    )

    drawLine(lineColor, nodes[0], nodes[1], strokeWidth = 3f)
    drawLine(lineColor, nodes[0], nodes[2], strokeWidth = 3f)
    drawLine(lineColor, nodes[1], nodes[3], strokeWidth = 3f)
    drawLine(lineColor, nodes[2], nodes[3], strokeWidth = 3f)

    nodes.forEachIndexed { i, n ->
        drawCircle(if (i == 3) Color(0xFF06B6D4) else nodeColor, radius = if (i == 0) 8f else 7f, center = n)
    }

    drawArrowTip(nodes[1], nodes[3], lineColor)
    drawArrowTip(nodes[2], nodes[3], lineColor)
}

private fun DrawScope.drawArrowTip(from: Offset, to: Offset, color: Color) {
    val dx = to.x - from.x
    val dy = to.y - from.y
    val len = sqrt(dx * dx + dy * dy)
    if (len == 0f) return
    val nx = dx / len; val ny = dy / len
    val tip = to
    val back = Offset(tip.x - nx * 12f, tip.y - ny * 12f)
    val perp1 = Offset(back.x - ny * 6f, back.y + nx * 6f)
    val perp2 = Offset(back.x + ny * 6f, back.y - nx * 6f)
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(perp1.x, perp1.y)
        lineTo(perp2.x, perp2.y)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawParticles(angle: Float, canvasSize: Size) {
    val cx = canvasSize.width / 2
    val cy = canvasSize.height / 2
    val radii = listOf(180f, 280f, 380f)
    radii.forEachIndexed { i, r ->
        val a = ((angle + i * 120f) * PI / 180.0)
        val x = cx + r * cos(a).toFloat()
        val y = cy + r * sin(a).toFloat()
        drawCircle(Color.White.copy(alpha = 0.08f - i * 0.015f), radius = 40f + i * 10f, center = Offset(x, y))
    }
    // Subtle grid dots
    for (xi in 0..6) {
        for (yi in 0..12) {
            drawCircle(
                Color.White.copy(alpha = 0.04f),
                radius = 2f,
                center = Offset(xi * (canvasSize.width / 6), yi * (canvasSize.height / 12))
            )
        }
    }
}
