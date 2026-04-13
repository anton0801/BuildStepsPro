@file:OptIn(ExperimentalMaterial3Api::class)
package com.buildSteps.BuildStepsPro.ui.screens.materials

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

// ── Materials List ────────────────────────────────────────────────────────
@Composable
fun MaterialsScreen(
    vm: MainViewModel,
    projectId: String,
    onBack: () -> Unit,
    onAddMaterial: () -> Unit
) {
    val allMaterials by vm.materials.collectAsState()
    val allTasks by vm.tasks.collectAsState()
    val mats = allMaterials.filter { mat ->
        allTasks.any { it.id == mat.taskId && it.projectId == projectId }
    }
    val profile by vm.profile.collectAsState()
    val totalCost = mats.sumOf { it.quantity * it.unitCost }

    Scaffold(
        topBar = { GradientTopBar("Materials", onBack) },
        floatingActionButton = { GradientFAB(onAddMaterial) },
        containerColor = BackgroundLight
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Summary card
            if (mats.isNotEmpty()) {
                Card(
                    Modifier.fillMaxWidth().padding(16.dp, 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BluePrimary.copy(0.07f))
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total Materials Cost", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                "${profile.currency} ${"%.2f".format(totalCost)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold, color = BluePrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${mats.size} items", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("${mats.count { it.purchased }} purchased",
                                style = MaterialTheme.typography.bodySmall, color = GreenSuccess)
                        }
                    }
                }
            }

            if (mats.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState("No materials yet", "Add materials to track costs and supplies", Icons.Default.Inventory)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(mats, key = { it.id }) { mat ->
                        val task = allTasks.find { it.id == mat.taskId }
                        MaterialCard(mat, task, profile.currency,
                            onTogglePurchased = { vm.updateMaterial(mat.copy(purchased = !mat.purchased)) },
                            onDelete = { vm.deleteMaterial(mat.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialCard(
    mat: Material,
    task: Task?,
    currency: String,
    onTogglePurchased: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val totalCost = mat.quantity * mat.unitCost

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Checkbox
            Checkbox(
                checked = mat.purchased,
                onCheckedChange = { onTogglePurchased() },
                colors = CheckboxDefaults.colors(checkedColor = GreenSuccess)
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(mat.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("${mat.quantity} ${mat.unit}" + if (task != null) " · ${task.name}" else "",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$currency ${"%.2f".format(totalCost)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("@${"%.2f".format(mat.unitCost)}/${mat.unit}",
                    style = MaterialTheme.typography.bodySmall, color = TextHint)
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(16.dp), tint = TextHint)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Delete", color = RedError) }, onClick = { onDelete(); showMenu = false })
                }
            }
        }
    }
}

// ── Add Material ──────────────────────────────────────────────────────────
@Composable
fun AddMaterialScreen(vm: MainViewModel, projectId: String, onBack: () -> Unit, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var qty  by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("pcs") }
    var cost by remember { mutableStateOf("") }

    val allTasks by vm.tasks.collectAsState()
    val projectTasks = allTasks.filter { it.projectId == projectId }
    var selectedTaskId by remember { mutableStateOf(projectTasks.firstOrNull()?.id ?: "") }

    val units = listOf("pcs", "m²", "m", "kg", "L", "pack", "roll", "bag", "box")

    Scaffold(
        topBar = { GradientTopBar("Add Material", onBack) },
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
                label = "Material name", placeholder = "e.g. Ceramic tiles",
                leadingIcon = { Icon(Icons.Default.Inventory, null, tint = TextHint) }
            )

            // Task selector
            if (projectTasks.isNotEmpty()) {
                Text("For Task", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, color = TextPrimary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(projectTasks) { task ->
                        FilterChip(
                            selected = task.id == selectedTaskId,
                            onClick = { selectedTaskId = task.id },
                            label = { Text(task.name) }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(
                    value = qty,
                    onValueChange = { qty = it.filter { c -> c.isDigit() || c == '.' } },
                    label = "Quantity",
                    modifier = Modifier.weight(1f)
                )
                // Unit selector
                var unitExpanded by remember { mutableStateOf(false) }
                Box(Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = unit, onValueChange = {},
                        label = { Text("Unit") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { unitExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary, unfocusedBorderColor = BorderLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        units.forEach { u ->
                            DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false })
                        }
                    }
                }
            }

            AppTextField(
                value = cost,
                onValueChange = { cost = it.filter { c -> c.isDigit() || c == '.' } },
                label = "Unit cost",
                leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = TextHint) }
            )

            // Preview total
            if (qty.toDoubleOrNull() != null && cost.toDoubleOrNull() != null) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardLight)
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Total cost", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("${"%.2f".format((qty.toDouble() * cost.toDouble()))}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = BluePrimary)
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            PrimaryButton(
                "Add Material",
                onClick = {
                    if (name.isNotBlank()) {
                        vm.addMaterial(
                            projectId, selectedTaskId, name.trim(),
                            qty.toDoubleOrNull() ?: 1.0, unit,
                            cost.toDoubleOrNull() ?: 0.0
                        )
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            )
        }
    }
}

// ── Budget ────────────────────────────────────────────────────────────────
@Composable
fun BudgetScreen(vm: MainViewModel, projectId: String, onBack: () -> Unit) {
    val allMaterials by vm.materials.collectAsState()
    val allTasks by vm.tasks.collectAsState()
    val profile by vm.profile.collectAsState()
    val activeProject by vm.activeProject.collectAsState()
    val project = vm.projects.value.find { it.id == projectId }

    val projectMats = allMaterials.filter { mat ->
        allTasks.any { it.id == mat.taskId && it.projectId == projectId }
    }
    val totalBudget = project?.budget ?: 0.0
    val spent = projectMats.sumOf { it.quantity * it.unitCost }
    val remaining = totalBudget - spent

    Scaffold(
        topBar = { GradientTopBar("Budget", onBack) },
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
            // Main budget card
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text("Project Budget", style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(0.7f))
                        Text(
                            if (totalBudget > 0) "${profile.currency} ${"%.0f".format(totalBudget)}"
                            else "Not set",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold, color = Color.White
                        )
                        Spacer(Modifier.height(16.dp))
                        if (totalBudget > 0) {
                            AnimatedProgressBar(
                                (spent / totalBudget).toFloat().coerceIn(0f, 1f),
                                Modifier.fillMaxWidth(),
                                Color.White
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Spent: ${"%.0f".format(spent)}", style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(0.8f))
                                Text("Left: ${"%.0f".format(remaining)}", style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(0.8f))
                            }
                        }
                    }
                }
            }

            // Stats row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BudgetStatCard(Modifier.weight(1f), "Spent",
                    "${profile.currency} ${"%.0f".format(spent)}", RedError)
                BudgetStatCard(Modifier.weight(1f), "Items",
                    "${projectMats.size}", BlueLight)
                BudgetStatCard(Modifier.weight(1f), "Purchased",
                    "${projectMats.count { it.purchased }}", GreenSuccess)
            }

            // Category breakdown
            SectionHeader("Breakdown by Category")
            val catTotals = TaskCategory.values().map { cat ->
                val catMats = projectMats.filter { mat ->
                    allTasks.find { it.id == mat.taskId }?.category == cat
                }
                Triple(cat, catMats.sumOf { it.quantity * it.unitCost }, catMats.size)
            }.filter { it.second > 0 }

            if (catTotals.isEmpty()) {
                EmptyState("No cost data", "Add materials with costs to see breakdown", Icons.Default.BarChart)
            } else {
                catTotals.forEach { (cat, total, count) ->
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).background(Color(cat.colorHex), CircleShape))
                                Column {
                                    Text(cat.label, style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Text("$count item${if (count != 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Text("${profile.currency} ${"%.0f".format(total)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetStatCard(modifier: Modifier, label: String, value: String, color: Color) {
    AppCard(modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

// ── Cost Per Task ─────────────────────────────────────────────────────────
@Composable
fun CostPerTaskScreen(vm: MainViewModel, projectId: String, onBack: () -> Unit) {
    val allTasks by vm.tasks.collectAsState()
    val allMaterials by vm.materials.collectAsState()
    val profile by vm.profile.collectAsState()
    val tasks = allTasks.filter { it.projectId == projectId }

    Scaffold(
        topBar = { GradientTopBar("Cost per Task", onBack) },
        containerColor = BackgroundLight
    ) { padding ->
        val taskCosts = tasks.map { task ->
            val mats = allMaterials.filter { it.taskId == task.id }
            task to mats.sumOf { it.quantity * it.unitCost }
        }.sortedByDescending { it.second }
        val maxCost = taskCosts.maxOfOrNull { it.second } ?: 1.0

        if (taskCosts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("No tasks yet", "Add tasks and materials to see costs", Icons.Default.AttachMoney)
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(taskCosts) { (task, cost) ->
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)) {
                                Box(Modifier.size(8.dp).background(Color(task.category.colorHex), CircleShape))
                                Text(task.name, style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                if (cost > 0) "${profile.currency} ${"%.0f".format(cost)}" else "—",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (cost > 0) BluePrimary else TextHint
                            )
                        }
                        if (cost > 0) {
                            Spacer(Modifier.height(8.dp))
                            AnimatedProgressBar(
                                (cost / maxCost).toFloat(),
                                Modifier.fillMaxWidth(),
                                Color(task.category.colorHex)
                            )
                        }
                    }
                }
            }
        }
    }
}
