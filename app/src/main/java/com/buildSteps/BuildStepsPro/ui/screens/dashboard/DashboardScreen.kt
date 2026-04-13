@file:OptIn(ExperimentalMaterial3Api::class)
package com.buildSteps.BuildStepsPro.ui.screens.dashboard

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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    vm: MainViewModel,
    onProjectsClick: () -> Unit,
    onTasksClick: (String) -> Unit,
    onErrorsClick: (String) -> Unit,
    onTimelineClick: (String) -> Unit,
    onNotificationsClick: () -> Unit
) {
    val projects by vm.projects.collectAsState()
    val activeProject by vm.activeProject.collectAsState()
    val tasks by vm.tasks.collectAsState()
    val errors by vm.errors.collectAsState()
    val unread by vm.unreadNotificationCount.collectAsState()

    val activeTasks = tasks.filter { it.projectId == activeProject?.id && it.status == TaskStatus.IN_PROGRESS }
    val nextTasks = tasks.filter { it.projectId == activeProject?.id && it.status == TaskStatus.TODO }.take(3)
    val progress = activeProject?.let { vm.getProgress(it.id) } ?: 0f

    Scaffold(
        topBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                    .statusBarsPadding()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Build Steps Pro", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.75f))
                        Text("Dashboard", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (unread > 0) {
                            BadgedBox(badge = { Badge { Text("$unread") } }) {
                                IconButton(onClick = onNotificationsClick) {
                                    Icon(Icons.Default.Notifications, null, tint = Color.White)
                                }
                            }
                        } else {
                            IconButton(onClick = onNotificationsClick) {
                                Icon(Icons.Default.NotificationsNone, null, tint = Color.White)
                            }
                        }
                    }
                }
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Active Project Card
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader("Active Project", "All Projects", onProjectsClick)
                Spacer(Modifier.height(8.dp))
                if (activeProject != null) {
                    ActiveProjectCard(activeProject!!, progress, tasks.filter { it.projectId == activeProject!!.id })
                } else {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        AppCard(modifier = Modifier.fillMaxWidth(), onClick = onProjectsClick) {
                            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Add, null, tint = BluePrimary, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Create your first project", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Quick Stats
            item {
                Spacer(Modifier.height(20.dp))
                SectionHeader("Overview")
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickStatCard(
                        Modifier.weight(1f),
                        "${tasks.filter { it.projectId == activeProject?.id && it.status == TaskStatus.DONE }.size}",
                        "Done",
                        Icons.Default.CheckCircle,
                        GreenSuccess
                    )
                    QuickStatCard(
                        Modifier.weight(1f),
                        "${activeTasks.size}",
                        "Active",
                        Icons.Default.PlayArrow,
                        BlueLight
                    )
                    QuickStatCard(
                        Modifier.weight(1f),
                        "${errors.size}",
                        "Errors",
                        Icons.Default.Warning,
                        if (errors.isEmpty()) TextHint else RedError
                    )
                }
            }

            // Errors Banner
            if (errors.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    activeProject?.let { proj ->
                        ErrorsBanner(errors.size, onClick = { onErrorsClick(proj.id) })
                    }
                }
            }

            // Next Steps
            item {
                Spacer(Modifier.height(20.dp))
                SectionHeader("Next Steps", if (activeProject != null) "View All" else null, {
                    activeProject?.let { onTasksClick(it.id) }
                })
            }

            if (nextTasks.isEmpty()) {
                item {
                    EmptyState(
                        if (activeProject == null) "No project selected" else "All tasks done!",
                        if (activeProject == null) "Select or create a project first" else "Great job — everything is finished",
                        if (activeProject == null) Icons.Default.Folder else Icons.Default.CheckCircle
                    )
                }
            } else {
                items(nextTasks) { task ->
                    NextTaskItem(task, Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                }
            }

            // Quick Actions
            item {
                Spacer(Modifier.height(20.dp))
                SectionHeader("Quick Actions")
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (activeProject != null) {
                        QuickActionButton(Modifier.weight(1f), "Timeline", Icons.Default.List) { onTimelineClick(activeProject!!.id) }
                        QuickActionButton(Modifier.weight(1f), "Projects", Icons.Default.FolderOpen) { onProjectsClick() }
                    } else {
                        QuickActionButton(Modifier.weight(1f), "New Project", Icons.Default.Add) { onProjectsClick() }
                        QuickActionButton(Modifier.weight(1f), "Projects", Icons.Default.FolderOpen) { onProjectsClick() }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveProjectCard(project: Project, progress: Float, tasks: List<Task>) {
    val doneTasks = tasks.count { it.status == TaskStatus.DONE }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF0284C7))))
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column {
                        Text(project.name, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(project.type.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f))
                    }
                    Box(
                        Modifier
                            .background(Color.White.copy(0.15f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelLarge,
                            color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                AnimatedProgressBar(progress, Modifier.fillMaxWidth(), Color.White)
                Spacer(Modifier.height(10.dp))
                Text("$doneTasks / ${tasks.size} tasks completed",
                    style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.75f))
            }
        }
    }
}

@Composable
private fun QuickStatCard(modifier: Modifier, value: String, label: String,
                          icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    AppCard(modifier = modifier) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun ErrorsBanner(count: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Warning, null, tint = RedError, modifier = Modifier.size(22.dp))
                Column {
                    Text("$count error${if (count > 1) "s" else ""} detected", style = MaterialTheme.typography.titleSmall,
                        color = RedError, fontWeight = FontWeight.SemiBold)
                    Text("Tap to view and fix", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            Icon(Icons.Default.ArrowForward, null, tint = RedError, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun NextTaskItem(task: Task, modifier: Modifier) {
    val catColor = Color(task.category.colorHex)
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .background(catColor.copy(0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Task, null, tint = catColor, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(task.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(task.category.label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            CategoryChip(task.category)
        }
    }
}

@Composable
private fun QuickActionButton(modifier: Modifier, label: String,
                               icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    AppCard(modifier = modifier, onClick = onClick) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = BluePrimary, modifier = Modifier.size(26.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        }
    }
}
