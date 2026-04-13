package com.buildSteps.BuildStepsPro.data.repository

import com.buildSteps.BuildStepsPro.data.model.*
import com.buildSteps.BuildStepsPro.data.preferences.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppRepository(private val prefs: AppPreferences) {

    private val _projects = MutableStateFlow(prefs.loadProjects())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _zones = MutableStateFlow(prefs.loadZones())
    val zones: StateFlow<List<Zone>> = _zones.asStateFlow()

    private val _tasks = MutableStateFlow(prefs.loadTasks())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _materials = MutableStateFlow(prefs.loadMaterials())
    val materials: StateFlow<List<Material>> = _materials.asStateFlow()

    private val _activities = MutableStateFlow(prefs.loadActivities())
    val activities: StateFlow<List<ActivityItem>> = _activities.asStateFlow()

    private val _notifications = MutableStateFlow(prefs.loadNotifications())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _profile = MutableStateFlow(prefs.loadProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _activeProjectId = MutableStateFlow(prefs.loadActiveProjectId())
    val activeProjectId: StateFlow<String?> = _activeProjectId.asStateFlow()

    // ── Projects ──────────────────────────────────────────────────────────
    fun addProject(project: Project) {
        val updated = _projects.value + project
        _projects.value = updated
        prefs.saveProjects(updated)
        addActivity(ActivityItem(projectId = project.id, description = "Project '${project.name}' created", type = ActivityType.PROJECT_CREATED))
        if (_activeProjectId.value == null) setActiveProject(project.id)
    }

    fun updateProject(project: Project) {
        val updated = _projects.value.map { if (it.id == project.id) project else it }
        _projects.value = updated
        prefs.saveProjects(updated)
    }

    fun deleteProject(projectId: String) {
        val updated = _projects.value.filter { it.id != projectId }
        _projects.value = updated
        prefs.saveProjects(updated)
        if (_activeProjectId.value == projectId) setActiveProject(updated.firstOrNull()?.id)
    }

    fun setActiveProject(id: String?) {
        _activeProjectId.value = id
        prefs.saveActiveProjectId(id)
    }

    // ── Zones ─────────────────────────────────────────────────────────────
    fun addZone(zone: Zone) {
        val updated = _zones.value + zone
        _zones.value = updated
        prefs.saveZones(updated)
        addActivity(ActivityItem(projectId = zone.projectId, description = "Zone '${zone.name}' added", type = ActivityType.ZONE_ADDED))
    }

    fun updateZone(zone: Zone) {
        val updated = _zones.value.map { if (it.id == zone.id) zone else it }
        _zones.value = updated
        prefs.saveZones(updated)
    }

    fun deleteZone(zoneId: String) {
        val updated = _zones.value.filter { it.id != zoneId }
        _zones.value = updated
        prefs.saveZones(updated)
    }

    // ── Tasks ─────────────────────────────────────────────────────────────
    fun addTask(task: Task) {
        val updated = _tasks.value + task
        _tasks.value = updated
        prefs.saveTasks(updated)
        addActivity(ActivityItem(projectId = task.projectId, description = "Task '${task.name}' added", type = ActivityType.TASK_ADDED))
    }

    fun updateTask(task: Task) {
        val updated = _tasks.value.map { if (it.id == task.id) task else it }
        _tasks.value = updated
        prefs.saveTasks(updated)
        if (task.status == TaskStatus.DONE) {
            addActivity(ActivityItem(projectId = task.projectId, description = "Task '${task.name}' completed", type = ActivityType.TASK_COMPLETED))
        }
    }

    fun deleteTask(taskId: String) {
        // Remove task and its dependencies from other tasks
        val updated = _tasks.value.filter { it.id != taskId }.map { t ->
            t.copy(dependsOn = t.dependsOn.filter { it != taskId })
        }
        _tasks.value = updated
        prefs.saveTasks(updated)
    }

    fun addDependency(taskId: String, dependsOnTaskId: String) {
        val task = _tasks.value.find { it.id == taskId } ?: return
        if (dependsOnTaskId !in task.dependsOn) {
            val updated = task.copy(dependsOn = task.dependsOn + dependsOnTaskId)
            updateTask(updated)
        }
    }

    fun removeDependency(taskId: String, dependsOnTaskId: String) {
        val task = _tasks.value.find { it.id == taskId } ?: return
        val updated = task.copy(dependsOn = task.dependsOn.filter { it != dependsOnTaskId })
        updateTask(updated)
    }

    // ── Materials ─────────────────────────────────────────────────────────
    fun addMaterial(material: Material) {
        val updated = _materials.value + material
        _materials.value = updated
        prefs.saveMaterials(updated)
    }

    fun updateMaterial(material: Material) {
        val updated = _materials.value.map { if (it.id == material.id) material else it }
        _materials.value = updated
        prefs.saveMaterials(updated)
    }

    fun deleteMaterial(materialId: String) {
        val updated = _materials.value.filter { it.id != materialId }
        _materials.value = updated
        prefs.saveMaterials(updated)
    }

    // ── Activities ────────────────────────────────────────────────────────
    private fun addActivity(activity: ActivityItem) {
        val updated = listOf(activity) + _activities.value.take(99)
        _activities.value = updated
        prefs.saveActivities(updated)
    }

    // ── Notifications ─────────────────────────────────────────────────────
    fun addNotification(notification: AppNotification) {
        val updated = listOf(notification) + _notifications.value
        _notifications.value = updated
        prefs.saveNotifications(updated)
    }

    fun markNotificationRead(id: String) {
        val updated = _notifications.value.map { if (it.id == id) it.copy(read = true) else it }
        _notifications.value = updated
        prefs.saveNotifications(updated)
    }

    fun markAllNotificationsRead() {
        val updated = _notifications.value.map { it.copy(read = true) }
        _notifications.value = updated
        prefs.saveNotifications(updated)
    }

    // ── Profile ───────────────────────────────────────────────────────────
    fun updateProfile(profile: UserProfile) {
        _profile.value = profile
        prefs.saveProfile(profile)
    }

    fun completeOnboarding() {
        updateProfile(_profile.value.copy(onboardingCompleted = true))
    }

    // ── Dependency/Error Analysis ─────────────────────────────────────────
    fun detectErrors(projectId: String): List<RepairError> {
        val projectTasks = _tasks.value.filter { it.projectId == projectId }
        val errors = mutableListOf<RepairError>()

        // Check for circular dependencies
        val circular = findCircularDependencies(projectTasks)
        circular.forEach { cycle ->
            errors.add(RepairError(
                type = ErrorType.CIRCULAR_DEPENDENCY,
                description = "Circular dependency detected: ${cycle.joinToString(" → ") { taskName(it, projectTasks) }}",
                affectedTaskIds = cycle,
                suggestion = "Break the cycle by removing one of the dependencies in the chain."
            ))
        }

        // Check wrong order (task done before its dependencies)
        projectTasks.filter { it.status == TaskStatus.DONE }.forEach { doneTask ->
            doneTask.dependsOn.forEach { depId ->
                val dep = projectTasks.find { it.id == depId }
                if (dep != null && dep.status != TaskStatus.DONE) {
                    errors.add(RepairError(
                        type = ErrorType.WRONG_ORDER,
                        description = "'${doneTask.name}' is marked done but depends on '${dep.name}' which is not finished",
                        affectedTaskIds = listOf(doneTask.id, depId),
                        suggestion = "Complete '${dep.name}' first, or update the dependency relationship."
                    ))
                }
            }
        }

        // Category conflict checks
        val categorizedByZone = projectTasks.groupBy { it.zoneId }
        categorizedByZone.forEach { (_, zoneTasks) ->
            val electrical = zoneTasks.filter { it.category == TaskCategory.ELECTRICAL }
            val walls = zoneTasks.filter { it.category == TaskCategory.WALLS }
            electrical.forEach { elTask ->
                walls.forEach { wTask ->
                    if (wTask.order < elTask.order && wTask.dependsOn.isEmpty() && elTask.dependsOn.isEmpty()) {
                        errors.add(RepairError(
                            type = ErrorType.CONFLICT,
                            description = "Wall work '${wTask.name}' may be scheduled before electrical '${elTask.name}' — this could mean walls need to be opened again",
                            affectedTaskIds = listOf(wTask.id, elTask.id),
                            suggestion = "Set electrical work as a dependency for wall finishing tasks."
                        ))
                    }
                }
            }
        }

        return errors
    }

    private fun findCircularDependencies(tasks: List<Task>): List<List<String>> {
        val cycles = mutableListOf<List<String>>()
        val visited = mutableSetOf<String>()
        val stack = mutableSetOf<String>()

        fun dfs(taskId: String, path: List<String>) {
            if (taskId in stack) {
                val cycleStart = path.indexOf(taskId)
                if (cycleStart >= 0) cycles.add(path.subList(cycleStart, path.size))
                return
            }
            if (taskId in visited) return
            visited.add(taskId)
            stack.add(taskId)
            val task = tasks.find { it.id == taskId } ?: return
            task.dependsOn.forEach { dfs(it, path + taskId) }
            stack.remove(taskId)
        }

        tasks.forEach { if (it.id !in visited) dfs(it.id, emptyList()) }
        return cycles
    }

    private fun taskName(id: String, tasks: List<Task>) = tasks.find { it.id == id }?.name ?: id

    // ── Auto Plan (Topological Sort) ──────────────────────────────────────
    fun buildAutoPlan(projectId: String): List<Task> {
        val projectTasks = _tasks.value.filter { it.projectId == projectId }
        val sorted = topologicalSort(projectTasks) ?: projectTasks
        sorted.forEachIndexed { index, task ->
            updateTask(task.copy(order = index))
        }
        addActivity(ActivityItem(projectId = projectId, description = "Auto-plan generated for project", type = ActivityType.GENERAL))
        addNotification(AppNotification(title = "Auto Plan Ready", message = "Your repair plan has been auto-generated!", projectId = projectId))
        return sorted
    }

    private fun topologicalSort(tasks: List<Task>): List<Task>? {
        val inDegree = mutableMapOf<String, Int>()
        val adjList = mutableMapOf<String, MutableList<String>>()
        tasks.forEach { t -> inDegree[t.id] = 0; adjList[t.id] = mutableListOf() }
        tasks.forEach { t ->
            t.dependsOn.forEach { dep ->
                adjList[dep]?.add(t.id)
                inDegree[t.id] = (inDegree[t.id] ?: 0) + 1
            }
        }
        val queue = ArrayDeque<String>()
        inDegree.filter { it.value == 0 }.keys.forEach { queue.add(it) }
        val result = mutableListOf<String>()
        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            result.add(curr)
            adjList[curr]?.forEach { next ->
                inDegree[next] = (inDegree[next] ?: 1) - 1
                if (inDegree[next] == 0) queue.add(next)
            }
        }
        if (result.size != tasks.size) return null // cycle exists
        return result.mapNotNull { id -> tasks.find { it.id == id } }
    }

    // ── Statistics ────────────────────────────────────────────────────────
    fun getProjectProgress(projectId: String): Float {
        val projectTasks = _tasks.value.filter { it.projectId == projectId }
        if (projectTasks.isEmpty()) return 0f
        return projectTasks.count { it.status == TaskStatus.DONE }.toFloat() / projectTasks.size
    }

    fun getProjectBudgetUsed(projectId: String): Double {
        val mats = _materials.value.filter { mat ->
            _tasks.value.any { it.id == mat.taskId && it.projectId == projectId }
        }
        return mats.sumOf { it.quantity * it.unitCost }
    }
}
