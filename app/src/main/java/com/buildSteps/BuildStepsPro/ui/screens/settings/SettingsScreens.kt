@file:OptIn(ExperimentalMaterial3Api::class)
package com.buildSteps.BuildStepsPro.ui.screens.settings

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
import com.buildSteps.BuildStepsPro.data.model.AppNotification
import com.buildSteps.BuildStepsPro.data.model.TaskStatus
import com.buildSteps.BuildStepsPro.ui.components.*
import com.buildSteps.BuildStepsPro.ui.theme.*
import com.buildSteps.BuildStepsPro.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

// ── Notifications ─────────────────────────────────────────────────────────
@Composable
fun NotificationsScreen(vm: MainViewModel, onBack: () -> Unit) {
    val notifications by vm.notifications.collectAsState()
    val fmt = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            GradientTopBar("Notifications", onBack) {
                if (notifications.any { !it.read }) {
                    TextButton(onClick = { vm.markAllRead() }) {
                        Text("Mark all read", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("No notifications", "You're all caught up!", Icons.Default.NotificationsNone)
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 60.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications, key = { it.id }) { notif ->
                NotificationCard(notif, fmt, onClick = { vm.markNotificationRead(notif.id) })
            }
        }
    }
}

@Composable
private fun NotificationCard(notif: AppNotification, fmt: SimpleDateFormat, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notif.read) BluePrimary.copy(0.05f) else SurfaceLight
        ),
        elevation = CardDefaults.cardElevation(if (!notif.read) 2.dp else 0.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(40.dp).background(
                    if (!notif.read) BluePrimary.copy(0.12f) else CardLight,
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, null,
                    tint = if (!notif.read) BluePrimary else TextHint,
                    modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(notif.title, style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!notif.read) FontWeight.Bold else FontWeight.Normal,
                        color = TextPrimary)
                    if (!notif.read) {
                        Box(Modifier.size(8.dp).background(BluePrimary, CircleShape)
                            .align(Alignment.CenterVertically))
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(notif.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                Text(fmt.format(Date(notif.timestamp)),
                    style = MaterialTheme.typography.bodySmall, color = TextHint)
            }
        }
    }
}

// ── Profile ───────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(vm: MainViewModel, onBack: () -> Unit) {
    val profile by vm.profile.collectAsState()
    val projects by vm.projects.collectAsState()
    val tasks by vm.tasks.collectAsState()

    var name by remember(profile) { mutableStateOf(profile.name) }
    var email by remember(profile) { mutableStateOf(profile.email) }
    var editMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GradientTopBar("Profile", onBack) {
                TextButton(onClick = {
                    if (editMode) { vm.updateProfile(name, email) }
                    editMode = !editMode
                }) {
                    Text(if (editMode) "Save" else "Edit", color = Color.White)
                }
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Avatar
            Box(
                Modifier
                    .size(90.dp)
                    .background(
                        Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (name.isNotBlank()) name.first().uppercaseChar().toString() else "?",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White, fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(if (name.isNotBlank()) name else "Your Name",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = TextPrimary)
            if (email.isNotBlank()) {
                Text(email, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }

            Spacer(Modifier.height(24.dp))

            // Stats row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileStatCard(Modifier.weight(1f), "${projects.size}", "Projects")
                ProfileStatCard(Modifier.weight(1f), "${tasks.size}", "Tasks")
                ProfileStatCard(Modifier.weight(1f),
                    "${tasks.count { it.status == TaskStatus.DONE }}", "Completed")
            }

            Spacer(Modifier.height(24.dp))

            if (editMode) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    AppTextField(
                        value = name, onValueChange = { name = it },
                        label = "Full Name",
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = TextHint) }
                    )
                    AppTextField(
                        value = email, onValueChange = { email = it },
                        label = "Email",
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = TextHint) }
                    )
                    Spacer(Modifier.height(8.dp))
                    PrimaryButton("Save Changes", onClick = {
                        vm.updateProfile(name, email)
                        editMode = false
                    }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun ProfileStatCard(modifier: Modifier, value: String, label: String) {
    AppCard(modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = BluePrimary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

// ── Settings ──────────────────────────────────────────────────────────────
@Composable
fun SettingsScreen(vm: MainViewModel, onBack: () -> Unit, onProfileClick: () -> Unit) {
    val profile by vm.profile.collectAsState()
    var selectedUnits    by remember(profile) { mutableStateOf(profile.units) }
    var selectedCurrency by remember(profile) { mutableStateOf(profile.currency) }
    var notificationsOn  by remember { mutableStateOf(true) }

    val currencies = listOf("USD", "EUR", "GBP", "RUB", "UAH", "PLN", "TRY")
    val unitOptions = listOf("metric" to "Metric (m, cm, kg)", "imperial" to "Imperial (ft, in, lb)")

    Scaffold(
        topBar = { GradientTopBar("Settings", onBack) },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Profile link
            SectionHeader("Account")
            AppCard(modifier = Modifier.fillMaxWidth(), onClick = onProfileClick) {
                Row(Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(
                            Brush.linearGradient(listOf(GradientStart, GradientEnd)), CircleShape
                        ), contentAlignment = Alignment.Center) {
                            Text(
                                if (profile.name.isNotBlank()) profile.name.first().uppercaseChar().toString() else "?",
                                style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(if (profile.name.isNotBlank()) profile.name else "Set your name",
                                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Edit profile", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Icon(Icons.Default.ArrowForward, null, tint = TextHint, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Units
            SectionHeader("Measurements")
            unitOptions.forEach { (value, label) ->
                val selected = value == selectedUnits
                AppCard(modifier = Modifier.fillMaxWidth(), onClick = {
                    selectedUnits = value
                    vm.updateSettings(value, selectedCurrency)
                }) {
                    Row(Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        if (selected) Icon(Icons.Default.CheckCircle, null, tint = BluePrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Currency
            SectionHeader("Currency")
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    currencies.forEach { curr ->
                        val selected = curr == selectedCurrency
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCurrency = curr
                                    vm.updateSettings(selectedUnits, curr)
                                }
                                .background(if (selected) BluePrimary.copy(0.06f) else Color.Transparent)
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(curr, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            if (selected) Icon(Icons.Default.Check, null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                        }
                        if (curr != currencies.last()) HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Notifications toggle
            SectionHeader("Notifications")
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                        Column {
                            Text("Push Notifications", style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("Reminders and alerts", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Switch(
                        checked = notificationsOn,
                        onCheckedChange = { notificationsOn = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BluePrimary)
                    )
                }
            }

            // App info
            Spacer(Modifier.height(16.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Build Steps Pro", style = MaterialTheme.typography.labelMedium,
                    color = TextHint, fontWeight = FontWeight.SemiBold)
                Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = TextHint)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
