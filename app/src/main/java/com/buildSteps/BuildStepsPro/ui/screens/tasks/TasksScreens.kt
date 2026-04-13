@file:OptIn(ExperimentalMaterial3Api::class)
package com.buildSteps.BuildStepsPro.ui.screens.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.buildSteps.BuildStepsPro.data.model.*
import com.buildSteps.BuildStepsPro.ui.components.*
import com.buildSteps.BuildStepsPro.ui.theme.*
import com.buildSteps.BuildStepsPro.viewmodel.MainViewModel

// ── Task List ─────────────────────────────────────────────────────────────
@Composable
fun TasksScreen(
    vm: MainViewModel,
    projectId: String,
    onBack: () -> Unit,
    onAddTask: () -> Unit,
    onTaskClick: (Task) -> Unit
) {
    val allTasks by vm.tasks.collectAsState()
    val tasks = allTasks.filter { it.projectId == projectId }.sortedBy { it.order }
    val zones  by vm.zones.collectAsState()
    var filterZone by remember { mutableStateOf<String?>(null) }
    var filterCat  by remember { mutableStateOf<TaskCategory?>(null) }

    val displayed = tasks
        .filter { filterZone == null || it.zoneId == filterZone }
        .filter { filterCat == null || it.category == filterCat }

    Scaffold(
        topBar = { GradientTopBar("Tasks", onBack) },
        floatingActionButton = { GradientFAB(onAddTask) },
        containerColor = BackgroundLight
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Filter chips
            val projectZones = zones.filter { it.projectId == projectId }
            if (projectZones.isNotEmpty() || TaskCategory.values().isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterZone == null && filterCat == null,
                            onClick = { filterZone = null; filterCat = null },
                            label = { Text("All") }
                        )
                    }
                    items(projectZones) { z ->
                        FilterChip(
                            selected = filterZone == z.id,
                            onClick = { filterZone = if (filterZone == z.id) null else z.id },
                            label = { Text(z.name) }
                        )
                    }
                    items(TaskCategory.values().toList()) { cat ->
                        FilterChip(
                            selected = filterCat == cat,
                            onClick = { filterCat = if (filterCat == cat) null else cat },
                            label = { Text(cat.label) },
                            leadingIcon = {
                                Box(Modifier.size(8.dp).background(Color(cat.colorHex), CircleShape))
                            }
                        )
                    }
                }
            }

            if (displayed.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState("No tasks yet", "Tap + to add your first task to this project", Icons.Default.Assignment)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayed, key = { it.id }) { task ->
                        val zoneName = zones.find { it.id == task.zoneId }?.name ?: ""
                        TaskListItem(task, zoneName,
                            onStatusChange = { vm.setTaskStatus(task.id, it) },
                            onClick = { onTaskClick(task) },
                            onDelete = { vm.deleteTask(task.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun TaskListItem(
    task: Task,
    zoneName: String,
    onStatusChange: (TaskStatus) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val catColor = Color(task.category.colorHex)
    val statusColor = when (task.status) {
        TaskStatus.TODO        -> TextHint
        TaskStatus.IN_PROGRESS -> BlueLight
        TaskStatus.DONE        -> GreenSuccess
    }

    AppCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Left color accent
            Box(Modifier.width(4.dp).height(48.dp).background(catColor, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(task.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryChip(task.category)
                    if (zoneName.isNotBlank()) {
                        Text("• $zoneName", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                if (task.dependsOn.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Link, null, modifier = Modifier.size(12.dp), tint = TextHint)
                        Text("${task.dependsOn.size} deps", style = MaterialTheme.typography.bodySmall, color = TextHint)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box {
                    Box(
                        Modifier
                            .background(statusColor.copy(0.12f), RoundedCornerShape(8.dp))
                            .clickable { showMenu = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(task.status.label, style = MaterialTheme.typography.labelSmall,
                            color = statusColor, fontWeight = FontWeight.SemiBold)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        TaskStatus.values().forEach { s ->
                            DropdownMenuItem(text = { Text(s.label) }, onClick = { onStatusChange(s); showMenu = false })
                        }
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("Delete", color = RedError) }, onClick = { onDelete(); showMenu = false })
                    }
                }
            }
        }
    }
}

// ── Add Task ──────────────────────────────────────────────────────────────
@Composable
fun AddTaskScreen(
    vm: MainViewModel,
    projectId: String,
    zoneId: String,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    var name     by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(TaskCategory.FINISH) }
    var notes    by remember { mutableStateOf("") }
    var days     by remember { mutableStateOf("1") }
    var cost     by remember { mutableStateOf("") }

    val zones by vm.zones.collectAsState()
    val projectZones = zones.filter { it.projectId == projectId }
    var selectedZoneId by remember { mutableStateOf(if (zoneId != "none") zoneId else projectZones.firstOrNull()?.id ?: "") }

    Scaffold(
        topBar = { GradientTopBar("Add Task", onBack) },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AppTextField(
                value = name, onValueChange = { name = it },
                label = "Task name", placeholder = "e.g. Install bathroom tiles",
                leadingIcon = { Icon(Icons.Default.Assignment, null, tint = TextHint) }
            )

            // Zone selector
            if (projectZones.isNotEmpty()) {
                Text("Zone", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(projectZones) { zone ->
                        val sel = zone.id == selectedZoneId
                        FilterChip(
                            selected = sel,
                            onClick = { selectedZoneId = zone.id },
                            label = { Text(zone.name) }
                        )
                    }
                }
            }

            // Category
            Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskCategory.values().forEach { cat ->
                    val sel = cat == category
                    val catColor = Color(cat.colorHex)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { category = cat }
                            .background(if (sel) catColor.copy(0.1f) else CardLight, RoundedCornerShape(12.dp))
                            .border(1.dp, if (sel) catColor else BorderLight, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).background(catColor, CircleShape))
                            Text(cat.label, style = MaterialTheme.typography.bodyMedium,
                                color = if (sel) catColor else TextPrimary,
                                fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
                        }
                        if (sel) Icon(Icons.Default.Check, null, tint = catColor, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(
                    value = days, onValueChange = { days = it.filter { c -> c.isDigit() } },
                    label = "Est. days", modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = TextHint) }
                )
                AppTextField(
                    value = cost, onValueChange = { cost = it.filter { c -> c.isDigit() || c == '.' } },
                    label = "Cost", modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = TextHint) }
                )
            }

            AppTextField(
                value = notes, onValueChange = { notes = it },
                label = "Notes (optional)", singleLine = false,
                modifier = Modifier.height(80.dp)
            )

            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                "Add Task",
                onClick = {
                    if (name.isNotBlank()) {
                        vm.addTask(projectId, selectedZoneId, name.trim(), category)
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            )
        }
    }
}

// ── Tasks Board (Kanban) ──────────────────────────────────────────────────
@Composable
fun TasksBoardScreen(vm: MainViewModel, projectId: String, onBack: () -> Unit) {
    val allTasks by vm.tasks.collectAsState()
    val tasks = allTasks.filter { it.projectId == projectId }

    val todo  = tasks.filter { it.status == TaskStatus.TODO }
    val inProg = tasks.filter { it.status == TaskStatus.IN_PROGRESS }
    val done  = tasks.filter { it.status == TaskStatus.DONE }

    Scaffold(
        topBar = { GradientTopBar("Tasks Board", onBack) },
        containerColor = BackgroundLight
    ) { padding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .horizontalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KanbanColumn("To Do", todo, TextHint, vm)
            KanbanColumn("In Progress", inProg, BlueLight, vm)
            KanbanColumn("Done", done, GreenSuccess, vm)
        }
    }
}

@Composable
private fun KanbanColumn(title: String, tasks: List<Task>, color: Color, vm: MainViewModel) {
    val nextStatus = when (title) {
        "To Do"       -> TaskStatus.IN_PROGRESS
        "In Progress" -> TaskStatus.DONE
        else          -> null
    }
    Column(
        Modifier
            .width(240.dp)
            .fillMaxHeight()
    ) {
        // Column header
        Row(
            Modifier
                .fillMaxWidth()
                .background(color.copy(0.1f), RoundedCornerShape(12.dp))
                .padding(12.dp, 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Box(Modifier.background(color.copy(0.2f), CircleShape).padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text("${tasks.size}", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tasks, key = { it.id }) { task ->
                KanbanCard(task, nextStatus, vm)
            }
        }
    }
}

@Composable
private fun KanbanCard(task: Task, nextStatus: TaskStatus?, vm: MainViewModel) {
    val catColor = Color(task.category.colorHex)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(task.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Box(Modifier.size(8.dp).background(catColor, CircleShape))
            }
            Spacer(Modifier.height(8.dp))
            CategoryChip(task.category)
            if (nextStatus != null) {
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = { vm.setTaskStatus(task.id, nextStatus) },
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Move to ${nextStatus.label}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
