@file:OptIn(ExperimentalMaterial3Api::class)
package com.buildSteps.BuildStepsPro.ui.screens.projects

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

@Composable
fun ProjectsScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onAddProject: () -> Unit,
    onProjectClick: (String) -> Unit
) {
    val projects by vm.projects.collectAsState()
    val activeId by vm.activeProjectId.collectAsState()

    Scaffold(
        topBar = { GradientTopBar("Projects", onBack) },
        floatingActionButton = { GradientFAB(onAddProject) },
        containerColor = BackgroundLight
    ) { padding ->
        if (projects.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("No projects yet", "Tap + to create your first renovation project", Icons.Default.FolderOpen)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(projects) { project ->
                    ProjectCard(
                        project = project,
                        isActive = project.id == activeId,
                        progress = vm.getProgress(project.id),
                        taskCount = vm.tasks.value.count { it.projectId == project.id },
                        onClick = { onProjectClick(project.id) },
                        onSetActive = { vm.setActiveProject(project.id) },
                        onDelete = { vm.deleteProject(project.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    isActive: Boolean,
    progress: Float,
    taskCount: Int,
    onClick: () -> Unit,
    onSetActive: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val borderColor = if (isActive) BluePrimary else Color.Transparent
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isActive) 2.dp else 0.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .background(BluePrimary.copy(0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Business, null, tint = BluePrimary)
                    }
                    Column {
                        Text(project.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(project.type.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isActive) {
                        Box(Modifier.background(GreenSuccess.copy(0.12f), RoundedCornerShape(8.dp)).padding(6.dp, 3.dp)) {
                            Text("Active", style = MaterialTheme.typography.labelSmall, color = GreenSuccess, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(18.dp), tint = TextSecondary)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            if (!isActive) DropdownMenuItem(text = { Text("Set Active") }, onClick = { onSetActive(); showMenu = false })
                            DropdownMenuItem(text = { Text("Delete", color = RedError) }, onClick = { onDelete(); showMenu = false })
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$taskCount tasks", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = BluePrimary, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(6.dp))
            AnimatedProgressBar(progress, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun AddProjectScreen(vm: MainViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ProjectType.APARTMENT) }

    Scaffold(
        topBar = { GradientTopBar("New Project", onBack) },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AppTextField(
                value = name, onValueChange = { name = it },
                label = "Project name", placeholder = "e.g. Apartment Renovation 2025",
                leadingIcon = { Icon(Icons.Default.Business, null, tint = TextHint) }
            )

            Text("Project Type", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            ProjectType.values().forEach { type ->
                val selected = type == selectedType
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { selectedType = type }
                        .background(
                            if (selected) BluePrimary.copy(0.08f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, if (selected) BluePrimary else BorderLight, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(type.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selected) BluePrimary else TextPrimary,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                    if (selected) Icon(Icons.Default.CheckCircle, null, tint = BluePrimary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.weight(1f))

            PrimaryButton(
                "Create Project",
                onClick = {
                    if (name.isNotBlank()) {
                        vm.addProject(name.trim(), selectedType)
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            )
        }
    }
}
