@file:OptIn(ExperimentalMaterial3Api::class)
package com.buildSteps.BuildStepsPro.ui.screens.timeline

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.buildSteps.BuildStepsPro.data.model.*
import com.buildSteps.BuildStepsPro.ui.components.*
import com.buildSteps.BuildStepsPro.ui.theme.*
import com.buildSteps.BuildStepsPro.viewmodel.MainViewModel

// ── Timeline ──────────────────────────────────────────────────────────────
@Composable
fun TimelineScreen(
    vm: MainViewModel,
    projectId: String,
    onBack: () -> Unit,
    onAutoPlan: () -> Unit
) {
    val allTasks by vm.tasks.collectAsState()
    val zones    by vm.zones.collectAsState()
    val tasks = allTasks
        .filter { it.projectId == projectId }
        .sortedBy { it.order }

    Scaffold(
        topBar = {
            GradientTopBar("Timeline", onBack) {
                IconButton(onClick = onAutoPlan) {
                    Icon(Icons.Default.AutoFixHigh, "Auto Plan", tint = Color.White)
                }
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("No tasks in timeline", "Add tasks and use Auto Plan to generate order", Icons.Default.List)
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 100.dp)
        ) {
            item {
                // Gantt-style legend row
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Work Sequence", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = TextPrimary)
                    TextButton(onClick = onAutoPlan) {
                        Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Auto Plan")
                    }
                }
            }

            itemsIndexed(tasks) { index, task ->
                val zone = zones.find { it.id == task.zoneId }
                TimelineItem(index + 1, task, zone, isLast = index == tasks.lastIndex)
            }
        }
    }
}

@Composable
private fun TimelineItem(stepNum: Int, task: Task, zone: Zone?, isLast: Boolean) {
    val catColor = Color(task.category.colorHex)
    val statusColor = when (task.status) {
        TaskStatus.TODO        -> TextHint
        TaskStatus.IN_PROGRESS -> BlueLight
        TaskStatus.DONE        -> GreenSuccess
    }

    var visible by remember { mutableStateOf(false) }
    val slideX by animateFloatAsState(
        targetValue = if (visible) 0f else -40f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "timeline_slide"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300, delayMillis = stepNum * 40),
        label = "timeline_alpha"
    )
    LaunchedEffect(Unit) { visible = true }

    Row(
        Modifier
            .fillMaxWidth()
            .offset(x = slideX.dp)
            .alpha(alpha),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Timeline column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .background(
                        if (task.status == TaskStatus.DONE) GreenSuccess else catColor,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.status == TaskStatus.DONE) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text("$stepNum", style = MaterialTheme.typography.labelMedium,
                        color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            if (!isLast) {
                Box(
                    Modifier
                        .width(2.dp)
                        .height(if (task.dependsOn.isEmpty()) 60.dp else 80.dp)
                        .background(
                            Brush.verticalGradient(listOf(catColor.copy(0.5f), Color.Transparent))
                        )
                )
            }
        }

        // Card
        AppCard(modifier = Modifier
            .weight(1f)
            .padding(start = 12.dp, bottom = 12.dp)) {
            Column(Modifier.padding(14.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(task.name, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        if (zone != null) {
                            Spacer(Modifier.height(2.dp))
                            Text(zone.name, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        StatusChip(task.status.label, statusColor)
                        CategoryChip(task.category)
                    }
                }
                if (task.dependsOn.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Link, null, modifier = Modifier.size(12.dp), tint = TextHint)
                        Text("Depends on ${task.dependsOn.size} task${if (task.dependsOn.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall, color = TextHint)
                    }
                }
                if (task.estimatedDays > 0) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(12.dp), tint = TextHint)
                        Text("~${task.estimatedDays} day${if (task.estimatedDays > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall, color = TextHint)
                    }
                }
            }
        }
    }
}

// ── Auto Plan Screen ───────────────────────────────────────────────────────
@Composable
fun AutoPlanScreen(vm: MainViewModel, projectId: String, onBack: () -> Unit) {
    var planned by remember { mutableStateOf<List<Task>?>(null) }
    var isRunning by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        0f, 360f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "spin_rot"
    )

    Scaffold(
        topBar = { GradientTopBar("Auto Plan", onBack) },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Hero illustration
            Box(
                Modifier
                    .size(140.dp)
                    .background(
                        Brush.radialGradient(listOf(BluePrimary.copy(0.12f), Color.Transparent)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isRunning) {
                    Icon(Icons.Default.AutoFixHigh, null,
                        tint = BluePrimary,
                        modifier = Modifier.size(64.dp).rotate(rotation))
                } else {
                    Icon(Icons.Default.AutoFixHigh, null, tint = BluePrimary, modifier = Modifier.size(64.dp))
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "AI-Powered Auto Plan",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = TextPrimary
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Automatically sorts your tasks based on dependencies using topological ordering. Electrical → Plumbing → Walls → Floor → Finish.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(32.dp))

            // Feature points
            listOf(
                "Respects all task dependencies" to Icons.Default.Link,
                "Detects and reports conflicts" to Icons.Default.Warning,
                "Generates optimal work order" to Icons.Default.Sort,
                "Notifies you when ready" to Icons.Default.Notifications
            ).forEach { (text, icon) ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier.size(34.dp).background(GreenSuccess.copy(0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = GreenSuccess, modifier = Modifier.size(18.dp))
                    }
                    Text(text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
            }

            Spacer(Modifier.weight(1f))

            if (planned != null) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenSuccess.copy(0.08f))
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = GreenSuccess, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Plan Generated!", style = MaterialTheme.typography.titleSmall,
                                color = GreenSuccess, fontWeight = FontWeight.Bold)
                            Text("${planned!!.size} tasks sorted. Go to Timeline to view.",
                                style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            PrimaryButton(
                text = if (isRunning) "Planning…" else if (planned != null) "Re-generate Plan" else "Generate Auto Plan",
                onClick = {
                    isRunning = true
                    planned = vm.buildAutoPlan(projectId)
                    isRunning = false
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning,
                icon = { Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp)) }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
