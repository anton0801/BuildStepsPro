package com.buildSteps.BuildStepsPro.ui.screens.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.buildSteps.BuildStepsPro.ui.components.*
import com.buildSteps.BuildStepsPro.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.*

// ── Welcome Screen ────────────────────────────────────────────────────────
@Composable
fun WelcomeScreen(onStart: () -> Unit, onLogin: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_anim")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "float"
    )

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(800), label = "alpha")
    val slideY by animateFloatAsState(targetValue = if (visible) 0f else 60f, animationSpec = spring(Spring.DampingRatioMediumBouncy), label = "slide")

    LaunchedEffect(Unit) { visible = true }

    Box(
        Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Decorative top blob
        Canvas(Modifier.fillMaxWidth().height(300.dp)) {
            drawBlobBackground(floatAnim)
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .alpha(alpha)
                .offset(y = slideY.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(80.dp))

            // Illustration
            Box(
                Modifier
                    .size(200.dp)
                    .background(
                        Brush.radialGradient(listOf(Color(0xFFDBEAFE), Color(0xFFF0F4FF))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(120.dp)) {
                    drawRepairIllustration()
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Smart Repair\nPlanning",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "Organize your renovation by zones,\ntrack dependencies, avoid mistakes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                Modifier.padding(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton("Get Started", onStart, Modifier.fillMaxWidth())
                OutlinedButton(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, BluePrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary)
                ) {
                    Text("Log In", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Onboarding ────────────────────────────────────────────────────────────
data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val illustrationType: Int,
    val accentColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = listOf(
        OnboardingPage("Avoid repair\nmistakes", "Set the correct order of work — don't redo what's already done", 0, Color(0xFF1D4ED8)),
        OnboardingPage("Plan work\nsequence", "Define zones, tasks and their order with drag & drop simplicity", 1, Color(0xFF8B5CF6)),
        OnboardingPage("Track\ndependencies", "Know exactly which task blocks another. Auto-detect conflicts", 2, Color(0xFF10B981))
    )
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(BackgroundLight)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            OnboardingPage(pages[page])
        }

        // Bottom controls
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { i ->
                    val selected = i == pagerState.currentPage
                    val width by animateDpAsState(targetValue = if (selected) 28.dp else 8.dp, label = "dot_w")
                    Box(
                        Modifier
                            .height(8.dp)
                            .width(width)
                            .background(
                                if (selected) BluePrimary else BorderLight,
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            if (pagerState.currentPage < pages.size - 1) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onFinished) {
                        Text("Skip", color = TextSecondary)
                    }
                    Button(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Next", fontWeight = FontWeight.SemiBold, color = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
                    }
                }
            } else {
                PrimaryButton("Start Planning", onFinished, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(500), label = "ob_alpha")
    val slideY by animateFloatAsState(targetValue = if (visible) 0f else 40f, animationSpec = spring(Spring.DampingRatioMediumBouncy), label = "ob_slide")
    LaunchedEffect(Unit) { visible = true }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .alpha(alpha)
            .offset(y = slideY.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(80.dp))

        // Illustration
        Box(
            Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(listOf(page.accentColor.copy(0.08f), Color.Transparent)),
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(200.dp)) {
                when (page.illustrationType) {
                    0 -> drawMistakeIllustration(page.accentColor)
                    1 -> drawSequenceIllustration(page.accentColor)
                    else -> drawDependencyIllustration(page.accentColor)
                }
            }
        }

        Spacer(Modifier.height(40.dp))

        Text(
            page.title,
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            page.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

// ── Canvas Illustrations ──────────────────────────────────────────────────
private fun drawBlobBackground(t: Float) {
    // Placeholder - drawn in a Canvas composable
}

private fun DrawScope.drawRepairIllustration() {
    val w = size.width; val h = size.height
    // Floor
    drawRoundRect(Color(0xFFE2E8F0), topLeft = Offset(w * 0.05f, h * 0.7f), size = Size(w * 0.9f, h * 0.25f), cornerRadius = CornerRadius(4f))
    // Wall
    drawRect(Color(0xFFCBD5E1), topLeft = Offset(w * 0.1f, h * 0.2f), size = Size(w * 0.8f, h * 0.52f))
    // Toolbox
    drawRoundRect(Color(0xFF3B82F6), topLeft = Offset(w * 0.35f, h * 0.55f), size = Size(w * 0.3f, h * 0.18f), cornerRadius = CornerRadius(6f))
    drawRoundRect(Color(0xFF1D4ED8), topLeft = Offset(w * 0.42f, h * 0.52f), size = Size(w * 0.16f, h * 0.06f), cornerRadius = CornerRadius(4f))
    // Checkmark
    drawCircle(Color(0xFF10B981), radius = w * 0.13f, center = Offset(w * 0.75f, h * 0.25f))
    val ck = Path().apply {
        moveTo(w * 0.67f, h * 0.25f); lineTo(w * 0.73f, h * 0.31f); lineTo(w * 0.83f, h * 0.19f)
    }
    drawPath(ck, Color.White, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawMistakeIllustration(color: Color) {
    val w = size.width; val h = size.height
    val nodes = listOf(Offset(w * 0.2f, h * 0.3f), Offset(w * 0.5f, h * 0.5f), Offset(w * 0.8f, h * 0.3f), Offset(w * 0.8f, h * 0.7f))
    nodes.forEachIndexed { i, n ->
        if (i < nodes.size - 1) drawLine(color.copy(0.4f), n, nodes[i + 1], strokeWidth = 3f)
    }
    drawLine(Color(0xFFEF4444), nodes[2], nodes[1], strokeWidth = 3f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 4f)))
    nodes.forEach { n ->
        drawCircle(color, radius = 12f, center = n)
        drawCircle(Color.White, radius = 6f, center = n)
    }
    // X mark
    val x = nodes[2]
    drawLine(Color(0xFFEF4444), Offset(x.x - 15f, x.y - 15f), Offset(x.x + 15f, x.y + 15f), strokeWidth = 4f, cap = StrokeCap.Round)
    drawLine(Color(0xFFEF4444), Offset(x.x + 15f, x.y - 15f), Offset(x.x - 15f, x.y + 15f), strokeWidth = 4f, cap = StrokeCap.Round)
}

private fun DrawScope.drawSequenceIllustration(color: Color) {
    val w = size.width; val h = size.height
    val items = listOf(0.2f, 0.4f, 0.6f, 0.8f)
    items.forEachIndexed { i, y ->
        val yPos = h * y
        drawRoundRect(color.copy(0.15f), topLeft = Offset(w * 0.1f, yPos - 20f), size = Size(w * 0.8f, 40f), cornerRadius = CornerRadius(8f))
        drawCircle(color, radius = 14f, center = Offset(w * 0.2f, yPos))
        drawCircle(Color.White, radius = 7f, center = Offset(w * 0.2f, yPos))
        if (i < items.size - 1) {
            drawLine(color.copy(0.3f), Offset(w * 0.2f, yPos + 14f), Offset(w * 0.2f, h * items[i + 1] - 14f), strokeWidth = 2f)
        }
    }
}

private fun DrawScope.drawDependencyIllustration(color: Color) {
    val w = size.width; val h = size.height
    val a = Offset(w * 0.2f, h * 0.5f)
    val b = Offset(w * 0.55f, h * 0.25f)
    val c = Offset(w * 0.55f, h * 0.75f)
    val d = Offset(w * 0.85f, h * 0.5f)
    listOf(a to b, a to c, b to d, c to d).forEach { (s, e) ->
        drawLine(color.copy(0.5f), s, e, strokeWidth = 3f)
    }
    listOf(a, b, c, d).forEach { n ->
        drawCircle(color, radius = 18f, center = n)
        drawCircle(Color.White, radius = 10f, center = n)
    }
}

private fun DrawScope.drawBlobBackground(t: Float) {
    val cx = size.width / 2
    val cy = size.height * 0.35f
    val r = size.width * 0.7f
    val path = Path()
    val points = 8
    for (i in 0..points) {
        val angle = (i.toFloat() / points) * 2 * PI.toFloat()
        val offset = sin(t * 2 * PI.toFloat() + i * 0.8f) * 20f
        val x = cx + (r + offset) * cos(angle)
        val y = cy + (r * 0.6f + offset) * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, brush = Brush.radialGradient(listOf(Color(0xFF3B82F6).copy(0.15f), Color(0xFF06B6D4).copy(0.05f)), Offset(cx, cy), r))
}
