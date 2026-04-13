@file:OptIn(ExperimentalMaterial3Api::class)
package com.buildSteps.BuildStepsPro.ui.screens.reports

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
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.buildSteps.BuildStepsPro.data.model.*
import com.buildSteps.BuildStepsPro.ui.components.*
import com.buildSteps.BuildStepsPro.ui.theme.*
import com.buildSteps.BuildStepsPro.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

// ── Progress ──────────────────────────────────────────────────────────────
@Composable
fun ProgressScreen(vm: MainViewModel, projectId: String, onBack: () -> Unit) {
    val allTasks by vm.tasks.collectAsState()
    val zones    by vm.zones.collectAsState()
    val tasks = allTasks.filter { it.projectId == projectId }
    val progress = vm.getProgress(projectId)

    val todo   = tasks.count { it.status == TaskStatus.TODO }
    val inProg = tasks.count { it.status == TaskStatus.IN_PROGRESS }
    val done   = tasks.count { it.status == TaskStatus.DONE }

    Scaffold(
        topBar = { GradientTopBar("Progress", onBack) },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circular progress hero
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicatorLarge(progress)
            }

            // Status breakdown
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusStatCard(Modifier.weight(1f), "To Do",       "$todo",   TextHint)
                StatusStatCard(Modifier.weight(1f), "In Progress", "$inProg", BlueLight)
                StatusStatCard(Modifier.weight(1f), "Done",        "$done",   GreenSuccess)
            }

            // Per-zone progress
            val projectZones = zones.filter { it.projectId == projectId }
            if (projectZones.isNotEmpty()) {
                SectionHeader("Progress by Zone")
                projectZones.forEach { zone ->
                    val zoneTasks = tasks.filter { it.zoneId == zone.id }
                    if (zoneTasks.isNotEmpty()) {
                        val zoneProgress = zoneTasks.count { it.status == TaskStatus.DONE }.toFloat() / zoneTasks.size
                        ZoneProgressCard(zone, zoneProgress, zoneTasks.size,
                            zoneTasks.count { it.status == TaskStatus.DONE })
                    }
                }
            }

            // Category breakdown
            SectionHeader("By Category")
            TaskCategory.values().forEach { cat ->
                val catTasks = tasks.filter { it.category == cat }
                if (catTasks.isNotEmpty()) {
                    val catDone = catTasks.count { it.status == TaskStatus.DONE }.toFloat() / catTasks.size
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(8.dp).background(Color(cat.colorHex), CircleShape))
                            Column(Modifier.weight(1f)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(cat.label, style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Text("${(catDone * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(cat.colorHex), fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(6.dp))
                                AnimatedProgressBar(catDone, Modifier.fillMaxWidth(), Color(cat.colorHex))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularProgressIndicatorLarge(progress: Float) {
    val animProg by animateFloatAsState(targetValue = progress, animationSpec = tween(1200), label = "circ_prog")
    Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 16f
            val radius = size.minDimension / 2 - stroke
            // Track
            drawCircle(Color(0xFFE2E8F0), radius, style = Stroke(stroke))
            // Progress arc
            drawArc(
                brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                startAngle = -90f,
                sweepAngle = 360f * animProg,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(animProg * 100).toInt()}%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Complete", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun StatusStatCard(modifier: Modifier, label: String, value: String, color: Color) {
    AppCard(modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(8.dp).background(color, CircleShape))
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun ZoneProgressCard(zone: Zone, progress: Float, total: Int, done: Int) {
    val color = Color(zone.color)
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(zone.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("$done/$total", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(Modifier.height(8.dp))
            AnimatedProgressBar(progress, Modifier.fillMaxWidth(), color)
        }
    }
}

// ── Reports ───────────────────────────────────────────────────────────────
@Composable
fun ReportsScreen(vm: MainViewModel, projectId: String, onBack: () -> Unit,
                  onActivityClick: () -> Unit) {
    val allTasks by vm.tasks.collectAsState()
    val allMaterials by vm.materials.collectAsState()
    val profile by vm.profile.collectAsState()
    val tasks = allTasks.filter { it.projectId == projectId }
    val mats = allMaterials.filter { mat ->
        allTasks.any { it.id == mat.taskId && it.projectId == projectId }
    }
    val errors = vm.detectErrors(projectId)

    Scaffold(
        topBar = { GradientTopBar("Reports", onBack) },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SectionHeader("Summary")
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReportStatCard(Modifier.weight(1f), "Tasks", "${tasks.size}", Icons.Default.Assignment, BluePrimary)
                    ReportStatCard(Modifier.weight(1f), "Done", "${tasks.count { it.status == TaskStatus.DONE }}", Icons.Default.CheckCircle, GreenSuccess)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReportStatCard(Modifier.weight(1f), "Materials", "${mats.size}", Icons.Default.Inventory, PurpleCategory)
                    ReportStatCard(Modifier.weight(1f), "Issues", "${errors.size}", Icons.Default.Warning, if (errors.isEmpty()) TextHint else RedError)
                }
            }
            item {
                val totalCost = mats.sumOf { it.quantity * it.unitCost }
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachMoney, null, tint = AmberWarning, modifier = Modifier.size(22.dp))
                            Column {
                                Text("Total Material Cost", style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary)
                                Text("${profile.currency} ${"%.2f".format(totalCost)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        Icon(Icons.Default.ArrowForward, null, tint = TextHint, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Completion by category
            item { SectionHeader("Completion by Category") }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TaskCategory.values().forEach { cat ->
                            val catTasks = tasks.filter { it.category == cat }
                            if (catTasks.isNotEmpty()) {
                                val pct = catTasks.count { it.status == TaskStatus.DONE }.toFloat() / catTasks.size
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(8.dp).background(Color(cat.colorHex), CircleShape))
                                        Text(cat.label, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    AnimatedProgressBar(pct, Modifier.weight(2f), Color(cat.colorHex))
                                    Spacer(Modifier.width(8.dp))
                                    Text("${(pct * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary, modifier = Modifier.width(32.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Activity link
            item {
                AppCard(modifier = Modifier.fillMaxWidth(), onClick = onActivityClick) {
                    Row(Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                            Text("Activity History", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Icon(Icons.Default.ArrowForward, null, tint = TextHint, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportStatCard(modifier: Modifier, label: String, value: String,
                            icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    AppCard(modifier = modifier) {
        Row(Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(38.dp).background(color.copy(0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(value, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

// ── Activity History ──────────────────────────────────────────────────────
@Composable
fun ActivityHistoryScreen(vm: MainViewModel, projectId: String, onBack: () -> Unit) {
    val activities by vm.activities.collectAsState()
    val projectActivities = activities.filter { it.projectId == projectId }
    val fmt = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = { GradientTopBar("Activity History", onBack) },
        containerColor = BackgroundLight
    ) { padding ->
        if (projectActivities.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("No activity yet", "Actions on this project will appear here", Icons.Default.History)
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 60.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            itemsIndexed(projectActivities) { index, activity ->
                val (iconRes, color) = when (activity.type) {
                    ActivityType.TASK_ADDED      -> Icons.Default.Add to BlueLight
                    ActivityType.TASK_COMPLETED  -> Icons.Default.CheckCircle to GreenSuccess
                    ActivityType.PROJECT_CREATED -> Icons.Default.FolderOpen to BluePrimary
                    ActivityType.ZONE_ADDED      -> Icons.Default.GridView to PurpleCategory
                    ActivityType.ERROR_FIXED     -> Icons.Default.AutoFixHigh to AmberWarning
                    ActivityType.GENERAL         -> Icons.Default.Info to TextHint
                }
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(44.dp)) {
                        Box(
                            Modifier.size(32.dp).background(color.copy(0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(iconRes, null, tint = color, modifier = Modifier.size(16.dp))
                        }
                        if (index < projectActivities.lastIndex) {
                            Box(Modifier.width(2.dp).height(36.dp).background(BorderLight))
                        }
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(start = 12.dp, bottom = if (index < projectActivities.lastIndex) 28.dp else 0.dp)
                    ) {
                        Text(activity.description, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text(fmt.format(Date(activity.timestamp)),
                            style = MaterialTheme.typography.bodySmall, color = TextHint)
                    }
                }
            }
        }
    }
}

// ── Calendar ──────────────────────────────────────────────────────────────
@Composable
fun CalendarScreen(vm: MainViewModel, projectId: String, onBack: () -> Unit) {
    val allTasks by vm.tasks.collectAsState()
    val tasks = allTasks.filter { it.projectId == projectId }.sortedBy { it.order }

    Scaffold(
        topBar = { GradientTopBar("Calendar Plan", onBack) },
        containerColor = BackgroundLight
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("No tasks", "Add tasks and use Auto Plan to generate schedule", Icons.Default.DateRange)
            }
            return@Scaffold
        }

        // Simple week-based schedule visualization
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 60.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Estimated Schedule", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Based on task order and estimated days",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
            }

            var dayOffset = 0
            items(tasks) { task ->
                val startDay = dayOffset + 1
                val endDay = dayOffset + task.estimatedDays
                dayOffset += task.estimatedDays

                val catColor = Color(task.category.colorHex)
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Day indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(catColor.copy(0.12f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text("Day", style = MaterialTheme.typography.labelSmall, color = TextHint)
                            Text("$startDay", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold, color = catColor)
                            if (endDay > startDay) {
                                Text("–$endDay", style = MaterialTheme.typography.labelSmall, color = catColor)
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(task.name, style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Spacer(Modifier.height(3.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CategoryChip(task.category)
                                StatusChip(task.status.label, when (task.status) {
                                    TaskStatus.DONE -> GreenSuccess
                                    TaskStatus.IN_PROGRESS -> BlueLight
                                    else -> TextHint
                                })
                            }
                        }
                        Text("${task.estimatedDays}d",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary)
                    }
                }
            }
        }
    }
}
