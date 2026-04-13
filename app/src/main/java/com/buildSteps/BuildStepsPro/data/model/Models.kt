package com.buildSteps.BuildStepsPro.data.model

import java.util.UUID

// ─── Project ───────────────────────────────────────────────────────────────
data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: ProjectType = ProjectType.APARTMENT,
    val createdAt: Long = System.currentTimeMillis(),
    val zones: List<String> = emptyList(), // zone ids
    val status: ProjectStatus = ProjectStatus.PLANNING,
    val budget: Double = 0.0,
    val currency: String = "USD"
)

enum class ProjectType { APARTMENT, HOUSE, OFFICE, COMMERCIAL }
enum class ProjectStatus { PLANNING, IN_PROGRESS, COMPLETED, ON_HOLD }

// ─── Zone ──────────────────────────────────────────────────────────────────
data class Zone(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val name: String = "",
    val icon: String = "room",
    val color: Long = 0xFF3B82F6,
    val taskIds: List<String> = emptyList()
)

// ─── Task ──────────────────────────────────────────────────────────────────
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val zoneId: String = "",
    val name: String = "",
    val category: TaskCategory = TaskCategory.FINISH,
    val status: TaskStatus = TaskStatus.TODO,
    val order: Int = 0,
    val estimatedDays: Int = 1,
    val cost: Double = 0.0,
    val dependsOn: List<String> = emptyList(), // task ids this depends on
    val notes: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null
)

enum class TaskCategory(val label: String, val icon: String, val colorHex: Long) {
    ELECTRICAL("Electrical", "bolt", 0xFFF59E0B),
    PLUMBING("Plumbing", "water_drop", 0xFF3B82F6),
    WALLS("Walls", "format_paint", 0xFF8B5CF6),
    FLOOR("Floor", "layers", 0xFF10B981),
    FINISH("Finish", "auto_fix_high", 0xFFEC4899)
}

enum class TaskStatus(val label: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    DONE("Done")
}

// ─── Dependency ────────────────────────────────────────────────────────────
data class Dependency(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val taskId: String = "",          // Task A
    val dependsOnTaskId: String = ""  // depends on Task B
)

// ─── Material ──────────────────────────────────────────────────────────────
data class Material(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val taskId: String = "",
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = "pcs",
    val unitCost: Double = 0.0,
    val purchased: Boolean = false
)

// ─── Error/Conflict ────────────────────────────────────────────────────────
data class RepairError(
    val id: String = UUID.randomUUID().toString(),
    val type: ErrorType,
    val description: String,
    val affectedTaskIds: List<String>,
    val suggestion: String
)

enum class ErrorType { WRONG_ORDER, CIRCULAR_DEPENDENCY, MISSING_DEPENDENCY, CONFLICT }

// ─── Activity ──────────────────────────────────────────────────────────────
data class ActivityItem(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: ActivityType = ActivityType.GENERAL
)

enum class ActivityType { TASK_ADDED, TASK_COMPLETED, PROJECT_CREATED, ZONE_ADDED, ERROR_FIXED, GENERAL }

// ─── Notification ──────────────────────────────────────────────────────────
data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val projectId: String = ""
)

// ─── User Profile ──────────────────────────────────────────────────────────
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val units: String = "metric",
    val currency: String = "USD",
    val onboardingCompleted: Boolean = false
)
