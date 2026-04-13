@file:OptIn(ExperimentalMaterial3Api::class)
package com.buildSteps.BuildStepsPro.ui.screens.zones

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.buildSteps.BuildStepsPro.data.model.Zone
import com.buildSteps.BuildStepsPro.ui.components.*
import com.buildSteps.BuildStepsPro.ui.theme.*
import com.buildSteps.BuildStepsPro.viewmodel.MainViewModel

private val ZONE_PRESETS = listOf(
    Triple("Kitchen",     "kitchen",    0xFF3B82F6L),
    Triple("Bathroom",    "bathtub",    0xFF06B6D4L),
    Triple("Living Room", "weekend",    0xFF8B5CF6L),
    Triple("Bedroom",     "bed",        0xFF10B981L),
    Triple("Hallway",     "door_front", 0xFFF59E0BL),
    Triple("Balcony",     "deck",       0xFFEC4899L)
)

private val ZONE_ICONS = mapOf(
    "kitchen"    to Icons.Default.Restaurant,
    "bathtub"    to Icons.Default.WaterDrop,
    "weekend"    to Icons.Default.AirlineSeatReclineNormal,
    "bed"        to Icons.Default.Hotel,
    "door_front" to Icons.Default.Sensors,
    "deck"       to Icons.Default.OpenWith,
    "room"       to Icons.Default.Room
)

@Composable
fun ZonesScreen(
    vm: MainViewModel,
    projectId: String,
    onBack: () -> Unit,
    onAddZone: () -> Unit,
    onZoneClick: (String) -> Unit
) {
    val allZones by vm.zones.collectAsState()
    val zones = allZones.filter { it.projectId == projectId }
    val tasks  by vm.tasks.collectAsState()

    Scaffold(
        topBar = { GradientTopBar("Zones", onBack) },
        floatingActionButton = { GradientFAB(onAddZone) },
        containerColor = BackgroundLight
    ) { padding ->
        if (zones.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("No zones yet", "Divide your project into zones like Kitchen, Bathroom…", Icons.Default.GridView)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(zones.size) { i ->
                    val zone = zones[i]
                    val taskCount = tasks.count { it.zoneId == zone.id }
                    ZoneCard(zone, taskCount, onClick = { onZoneClick(zone.id) },
                        onDelete = { vm.deleteZone(zone.id) })
                }
            }
        }
    }
}

@Composable
private fun ZoneCard(zone: Zone, taskCount: Int, onClick: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val color = Color(zone.color)
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            // Color bar at top
            Box(Modifier.fillMaxWidth().height(6.dp).background(color, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)))

            Column(
                Modifier.fillMaxSize().padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(
                        Modifier.size(44.dp).background(color.copy(0.12f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(ZONE_ICONS[zone.icon] ?: Icons.Default.Room, null, tint = color, modifier = Modifier.size(22.dp))
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(16.dp), tint = TextHint)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Delete", color = RedError) }, onClick = { onDelete(); showMenu = false })
                        }
                    }
                }
                Column {
                    Text(zone.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("$taskCount task${if (taskCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun AddZoneScreen(vm: MainViewModel, projectId: String, onBack: () -> Unit, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("room") }
    var selectedColor by remember { mutableStateOf(0xFF3B82F6L) }

    Scaffold(
        topBar = { GradientTopBar("Add Zone", onBack) },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AppTextField(
                value = name, onValueChange = { name = it },
                label = "Zone name", placeholder = "e.g. Kitchen",
                leadingIcon = { Icon(Icons.Default.GridView, null, tint = TextHint) }
            )

            Text("Quick Presets", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(220.dp)
            ) {
                items(ZONE_PRESETS.size) { i ->
                    val (pName, pIcon, pColor) = ZONE_PRESETS[i]
                    val selected = name == pName && selectedIcon == pIcon
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { name = pName; selectedIcon = pIcon; selectedColor = pColor }
                            .background(
                                if (selected) Color(pColor).copy(0.15f) else CardLight,
                                RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, if (selected) Color(pColor) else BorderLight, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(ZONE_ICONS[pIcon] ?: Icons.Default.Room, null,
                            tint = Color(pColor), modifier = Modifier.size(18.dp))
                        Text(pName, style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color(pColor) else TextPrimary)
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            PrimaryButton(
                "Add Zone",
                onClick = { if (name.isNotBlank()) { vm.addZone(projectId, name.trim(), selectedIcon); onDone() } },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            )
        }
    }
}
