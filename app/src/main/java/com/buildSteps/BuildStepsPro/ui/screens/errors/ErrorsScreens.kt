@file:OptIn(ExperimentalMaterial3Api::class)
package com.buildSteps.BuildStepsPro.ui.screens.errors

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
fun ErrorsScreen(
    vm: MainViewModel,
    projectId: String,
    onBack: () -> Unit,
    onFixSuggestions: () -> Unit
) {
    val errors by vm.errors.collectAsState()
    val allTasks by vm.tasks.collectAsState()
    val projectErrors = vm.detectErrors(projectId)

    Scaffold(
        topBar = {
            GradientTopBar("Errors", onBack) {
                if (projectErrors.isNotEmpty()) {
                    IconButton(onClick = onFixSuggestions) {
                        Icon(Icons.Default.AutoFixHigh, "Fix Suggestions", tint = Color.White)
                    }
                }
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (projectErrors.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(80.dp).background(GreenSuccess.copy(0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = GreenSuccess, modifier = Modifier.size(44.dp))
                    }
                    Text("No errors detected!", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Your repair plan looks correct.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = RedError.copy(0.08f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Warning, null, tint = RedError, modifier = Modifier.size(24.dp))
                        Column {
                            Text("${projectErrors.size} issue${if (projectErrors.size > 1) "s" else ""} found",
                                style = MaterialTheme.typography.titleSmall, color = RedError, fontWeight = FontWeight.Bold)
                            Text("Review and fix before proceeding", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            items(projectErrors) { error ->
                ErrorCard(error, allTasks)
            }

            item {
                Spacer(Modifier.height(8.dp))
                PrimaryButton(
                    "View Fix Suggestions",
                    onClick = onFixSuggestions,
                    modifier = Modifier.fillMaxWidth(),
                    icon = { Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(error: RepairError, tasks: List<Task>) {
    val (bgColor, iconColor, typeLabel) = when (error.type) {
        ErrorType.CIRCULAR_DEPENDENCY -> Triple(AmberWarning.copy(0.08f), AmberWarning, "Circular Dependency")
        ErrorType.WRONG_ORDER         -> Triple(RedError.copy(0.08f), RedError, "Wrong Order")
        ErrorType.CONFLICT            -> Triple(PurpleCategory.copy(0.08f), PurpleCategory, "Conflict")
        ErrorType.MISSING_DEPENDENCY  -> Triple(BlueLight.copy(0.08f), BlueLight, "Missing Dependency")
    }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, iconColor.copy(0.3f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, null, tint = iconColor, modifier = Modifier.size(20.dp))
                    Text(typeLabel, style = MaterialTheme.typography.titleSmall,
                        color = iconColor, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.background(iconColor.copy(0.15f), RoundedCornerShape(6.dp)).padding(6.dp, 3.dp)) {
                    Text("❌", style = MaterialTheme.typography.labelSmall)
                }
            }

            Text(error.description, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)

            // Affected tasks
            if (error.affectedTaskIds.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Affected tasks:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    error.affectedTaskIds.forEach { taskId ->
                        val task = tasks.find { it.id == taskId }
                        if (task != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(Modifier.size(6.dp).background(Color(task.category.colorHex), CircleShape))
                                Text(task.name, style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Fix Suggestions ────────────────────────────────────────────────────────
@Composable
fun FixSuggestionsScreen(vm: MainViewModel, projectId: String, onBack: () -> Unit) {
    val projectErrors = vm.detectErrors(projectId)
    val allTasks by vm.tasks.collectAsState()

    Scaffold(
        topBar = { GradientTopBar("Fix Suggestions", onBack) },
        containerColor = BackgroundLight
    ) { padding ->
        if (projectErrors.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("No issues to fix!", "Your project plan is error-free", Icons.Default.CheckCircle)
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Here's how to fix the detected issues:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp))
            }

            itemsIndexed(projectErrors) { index, error ->
                FixCard(index + 1, error, allTasks, vm)
            }

            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BluePrimary.copy(0.06f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.AutoFixHigh, null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                        Column {
                            Text("Use Auto Plan", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold, color = BluePrimary)
                            Text("Auto Plan can automatically resolve ordering issues",
                                style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FixCard(num: Int, error: RepairError, tasks: List<Task>, vm: MainViewModel) {
    var applied by remember { mutableStateOf(false) }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (applied) GreenSuccess.copy(0.06f) else SurfaceLight
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(28.dp).background(
                            if (applied) GreenSuccess else BluePrimary, CircleShape
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (applied) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text("$num", style = MaterialTheme.typography.labelMedium,
                                color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        when (error.type) {
                            ErrorType.WRONG_ORDER        -> "Fix wrong order"
                            ErrorType.CIRCULAR_DEPENDENCY -> "Break circular dependency"
                            ErrorType.CONFLICT           -> "Resolve conflict"
                            ErrorType.MISSING_DEPENDENCY -> "Add missing dependency"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (applied) GreenSuccess else TextPrimary
                    )
                }
            }

            // Suggestion text
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("👉", style = MaterialTheme.typography.bodyMedium)
                Text(error.suggestion, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }

            // Quick fix button for wrong order
            if (!applied && error.type == ErrorType.WRONG_ORDER && error.affectedTaskIds.size == 2) {
                val taskA = tasks.find { it.id == error.affectedTaskIds[0] }
                val taskB = tasks.find { it.id == error.affectedTaskIds[1] }
                if (taskA != null && taskB != null) {
                    OutlinedButton(
                        onClick = {
                            // Swap orders
                            val tmpOrder = taskA.order
                            vm.updateTask(taskA.copy(order = taskB.order))
                            vm.updateTask(taskB.copy(order = tmpOrder))
                            applied = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.SwapVert, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Swap: '${taskA.name}' ↔ '${taskB.name}'",
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
