package com.buildSteps.BuildStepsPro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildSteps.BuildStepsPro.data.model.*
import com.buildSteps.BuildStepsPro.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(val repository: AppRepository) : ViewModel() {

    val projects      = repository.projects
    val zones         = repository.zones
    val tasks         = repository.tasks
    val materials     = repository.materials
    val activities    = repository.activities
    val notifications = repository.notifications
    val profile       = repository.profile
    val activeProjectId = repository.activeProjectId

    // ── Derived ────────────────────────────────────────────────────────
    val activeProject: StateFlow<Project?> = combine(projects, activeProjectId) { projs, id ->
        projs.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val errors: StateFlow<List<RepairError>> = combine(tasks, activeProjectId) { _, id ->
        if (id != null) repository.detectErrors(id) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = notifications.map { list ->
        list.count { !it.read }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── Project actions ────────────────────────────────────────────────
    fun addProject(name: String, type: ProjectType) {
        repository.addProject(Project(name = name, type = type))
    }
    fun deleteProject(id: String) = repository.deleteProject(id)
    fun setActiveProject(id: String) = repository.setActiveProject(id)

    // ── Zone actions ───────────────────────────────────────────────────
    fun addZone(projectId: String, name: String, icon: String = "room") {
        repository.addZone(Zone(projectId = projectId, name = name, icon = icon))
    }
    fun deleteZone(id: String) = repository.deleteZone(id)

    // ── Task actions ───────────────────────────────────────────────────
    fun addTask(projectId: String, zoneId: String, name: String, category: TaskCategory) {
        val order = tasks.value.filter { it.projectId == projectId }.size
        repository.addTask(Task(projectId = projectId, zoneId = zoneId, name = name, category = category, order = order))
    }
    fun updateTask(task: Task) = repository.updateTask(task)
    fun deleteTask(id: String) = repository.deleteTask(id)
    fun addDependency(taskId: String, depId: String) = repository.addDependency(taskId, depId)
    fun removeDependency(taskId: String, depId: String) = repository.removeDependency(taskId, depId)

    fun setTaskStatus(taskId: String, status: TaskStatus) {
        val task = tasks.value.find { it.id == taskId } ?: return
        repository.updateTask(task.copy(status = status))
    }

    // ── Material actions ───────────────────────────────────────────────
    fun addMaterial(projectId: String, taskId: String, name: String, qty: Double, unit: String, cost: Double) {
        repository.addMaterial(Material(projectId = projectId, taskId = taskId, name = name, quantity = qty, unit = unit, unitCost = cost))
    }
    fun updateMaterial(mat: Material) = repository.updateMaterial(mat)
    fun deleteMaterial(id: String) = repository.deleteMaterial(id)

    // ── Auto plan ──────────────────────────────────────────────────────
    fun buildAutoPlan(projectId: String): List<Task> = repository.buildAutoPlan(projectId)

    // ── Stats ──────────────────────────────────────────────────────────
    fun getProgress(projectId: String) = repository.getProjectProgress(projectId)
    fun getBudgetUsed(projectId: String) = repository.getProjectBudgetUsed(projectId)

    // ── Profile ────────────────────────────────────────────────────────
    fun updateProfile(name: String, email: String) {
        repository.updateProfile(profile.value.copy(name = name, email = email))
    }
    fun completeOnboarding() = repository.completeOnboarding()
    fun updateSettings(units: String, currency: String) {
        repository.updateProfile(profile.value.copy(units = units, currency = currency))
    }

    // ── Notifications ──────────────────────────────────────────────────
    fun markNotificationRead(id: String) = repository.markNotificationRead(id)
    fun markAllRead() = repository.markAllNotificationsRead()

    // ── Errors ─────────────────────────────────────────────────────────
    fun detectErrors(projectId: String) = repository.detectErrors(projectId)
}
