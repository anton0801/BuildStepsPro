@file:OptIn(ExperimentalMaterial3Api::class)
package com.buildSteps.BuildStepsPro.ui.screens.dependencies

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.buildSteps.BuildStepsPro.data.model.*
import com.buildSteps.BuildStepsPro.ui.components.*
import com.buildSteps.BuildStepsPro.ui.theme.*
import com.buildSteps.BuildStepsPro.viewmodel.MainViewModel
import kotlin.math.*

// ── Dependencies Editor ────────────────────────────────────────────────────
@Composable
fun DependenciesScreen(
    vm: MainViewModel,
    projectId: String,
    onBack: () -> Unit,
    onGraphClick: () -> Unit
) {
    val allTasks by vm.tasks.collectAsState()
    val tasks = allTasks.filter { it.projectId == projectId }

    var selectedTaskId by remember { mutableStateOf<String?>(null) }
    val selectedTask = tasks.find { it.id == selectedTaskId }

    Scaffold(
        topBar = {
            GradientTopBar("Dependencies", onBack) {
                IconButton(onClick = onGraphClick) {
                    Icon(Icons.Default.AccountCircle, "View Graph", tint = Color.White)
                }
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("No tasks yet", "Add tasks to the project to define dependencies", Icons.Default.Link)
            }
            return@Scaffold
        }

        Row(Modifier.fillMaxSize().padding(padding)) {
            // Left panel: task list
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(BackgroundLight)
            ) {
                Text(
                    "Select Task A",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp)
                )
                LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(tasks, key = { it.id }) { task ->
                        val isSelected = task.id == selectedTaskId
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { selectedTaskId = task.id }
                                .background(if (isSelected) BluePrimary.copy(0.08f) else Color.Transparent)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(Modifier.size(8.dp).background(Color(task.category.colorHex), CircleShape))
                            Text(
                                task.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) BluePrimary else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (task.dependsOn.isNotEmpty()) {
                                Text("${task.dependsOn.size}", style = MaterialTheme.typography.labelSmall,
                                    color = TextHint)
                            }
                        }
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
                    }
                }
            }

            Box(
                Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(BorderLight)
            )

            // Right panel: dependency targets
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(BackgroundLight)
            ) {
                if (selectedTask == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ArrowBack, null, tint = TextHint, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Select a task", style = MaterialTheme.typography.bodyMedium, color = TextHint)
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(CardLight)
                                .padding(16.dp, 12.dp)
                        ) {
                            Column {
                                Text(selectedTask.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("depends on →",
                                    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                            items(tasks.filter { it.id != selectedTask.id }, key = { it.id }) { depTask ->
                                val isLinked = depTask.id in selectedTask.dependsOn
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isLinked) vm.removeDependency(selectedTask.id, depTask.id)
                                            else vm.addDependency(selectedTask.id, depTask.id)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(Modifier.size(8.dp).background(Color(depTask.category.colorHex), CircleShape))
                                        Text(depTask.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isLinked) BluePrimary else TextPrimary)
                                    }
                                    Icon(
                                        if (isLinked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                        null,
                                        tint = if (isLinked) BluePrimary else TextHint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Dependency Graph ───────────────────────────────────────────────────────
@Composable
fun DependencyGraphScreen(vm: MainViewModel, projectId: String, onBack: () -> Unit) {
    val allTasks by vm.tasks.collectAsState()
    val tasks = allTasks.filter { it.projectId == projectId }

    // Compute layout positions
    val positions = remember(tasks) { computeGraphPositions(tasks) }

    var scale     by remember { mutableStateOf(1f) }
    var offsetX   by remember { mutableStateOf(0f) }
    var offsetY   by remember { mutableStateOf(0f) }

    Scaffold(
        topBar = {
            GradientTopBar("Dependency Graph", onBack) {
                IconButton(onClick = { scale = 1f; offsetX = 0f; offsetY = 0f }) {
                    Icon(Icons.Default.CenterFocusWeak, "Reset", tint = Color.White)
                }
            }
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("No tasks", "Add tasks and dependencies to see the graph", Icons.Default.AccountCircle)
            }
            return@Scaffold
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.3f, 3f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale; scaleY = scale
                        translationX = offsetX; translationY = offsetY
                    }
            ) {
                // Draw edges
                tasks.forEach { task ->
                    val fromPos = positions[task.id] ?: return@forEach
                    task.dependsOn.forEach { depId ->
                        val toPos = positions[depId] ?: return@forEach
                        drawArrow(fromPos, toPos)
                    }
                }
                // Draw nodes
                tasks.forEach { task ->
                    val pos = positions[task.id] ?: return@forEach
                    drawTaskNode(task, pos)
                }
            }

            // Legend
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(0.6f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text("Legend", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
                Spacer(Modifier.height(6.dp))
                TaskCategory.values().forEach { cat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(Modifier.size(8.dp).background(Color(cat.colorHex), CircleShape))
                        Text(cat.label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.8f))
                    }
                }
            }

            // Pinch hint
            Text(
                "Pinch to zoom · Drag to pan",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(0.4f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            )
        }
    }
}

private fun DrawScope.drawArrow(from: Offset, to: Offset) {
    val dx = to.x - from.x; val dy = to.y - from.y
    val len = sqrt(dx * dx + dy * dy)
    if (len < 1f) return
    val nx = dx / len; val ny = dy / len
    val startOff = 36f; val endOff = 36f
    val start = Offset(from.x + nx * startOff, from.y + ny * startOff)
    val end   = Offset(to.x - nx * endOff, to.y - ny * endOff)
    val paint = Color(0xFF3B82F6).copy(0.6f)
    drawLine(paint, start, end, strokeWidth = 2.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f)))
    // arrowhead
    val ax = -nx * 12f + ny * 6f; val ay = -ny * 12f - nx * 6f
    val bx = -nx * 12f - ny * 6f; val by = -ny * 12f + nx * 6f
    val path = Path().apply {
        moveTo(end.x, end.y)
        lineTo(end.x + ax, end.y + ay)
        lineTo(end.x + bx, end.y + by)
        close()
    }
    drawPath(path, Color(0xFF06B6D4))
}

private fun DrawScope.drawTaskNode(task: Task, pos: Offset) {
    val catColor = Color(task.category.colorHex)
    val nodeR = 30f
    // shadow
    drawCircle(Color.Black.copy(0.3f), nodeR + 6f, Offset(pos.x + 3f, pos.y + 3f))
    // background
    drawCircle(Color(0xFF1F2937), nodeR, pos)
    // colored ring
    drawCircle(catColor, nodeR, pos, style = Stroke(width = 3f))
    // status dot
    val statusColor = when (task.status) {
        TaskStatus.TODO        -> Color(0xFF94A3B8)
        TaskStatus.IN_PROGRESS -> Color(0xFF3B82F6)
        TaskStatus.DONE        -> Color(0xFF10B981)
    }
    drawCircle(statusColor, 6f, Offset(pos.x + nodeR * 0.65f, pos.y - nodeR * 0.65f))
}

private fun computeGraphPositions(tasks: List<Task>): Map<String, Offset> {
    if (tasks.isEmpty()) return emptyMap()
    // Topological level assignment
    val levels = mutableMapOf<String, Int>()
    fun getLevel(id: String): Int {
        levels[id]?.let { return it }
        val task = tasks.find { it.id == id } ?: return 0
        val level = if (task.dependsOn.isEmpty()) 0
        else task.dependsOn.maxOf { getLevel(it) } + 1
        levels[id] = level
        return level
    }
    tasks.forEach { getLevel(it.id) }

    val columnWidth = 200f
    val rowHeight   = 120f
    val paddingX    = 100f
    val paddingY    = 100f
    val byLevel = tasks.groupBy { levels[it.id] ?: 0 }

    val positions = mutableMapOf<String, Offset>()
    byLevel.forEach { (level, levelTasks) ->
        levelTasks.forEachIndexed { row, task ->
            val totalRows = levelTasks.size
            val startY = -(totalRows - 1) * rowHeight / 2f
            positions[task.id] = Offset(
                paddingX + level * columnWidth,
                paddingY + startY + row * rowHeight
            )
        }
    }
    return positions
}
