package com.buildSteps.BuildStepsPro.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.buildSteps.BuildStepsPro.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("build_steps_pro", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_PROJECTS = "projects"
        private const val KEY_ZONES = "zones"
        private const val KEY_TASKS = "tasks"
        private const val KEY_MATERIALS = "materials"
        private const val KEY_ACTIVITIES = "activities"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_PROFILE = "user_profile"
        private const val KEY_ACTIVE_PROJECT = "active_project_id"
    }

    // ── Projects ──────────────────────────────────────────────────────────
    fun saveProjects(projects: List<Project>) {
        prefs.edit().putString(KEY_PROJECTS, gson.toJson(projects)).apply()
    }

    fun loadProjects(): List<Project> {
        val json = prefs.getString(KEY_PROJECTS, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<Project>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }

    // ── Zones ─────────────────────────────────────────────────────────────
    fun saveZones(zones: List<Zone>) {
        prefs.edit().putString(KEY_ZONES, gson.toJson(zones)).apply()
    }

    fun loadZones(): List<Zone> {
        val json = prefs.getString(KEY_ZONES, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<Zone>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }

    // ── Tasks ─────────────────────────────────────────────────────────────
    fun saveTasks(tasks: List<Task>) {
        prefs.edit().putString(KEY_TASKS, gson.toJson(tasks)).apply()
    }

    fun loadTasks(): List<Task> {
        val json = prefs.getString(KEY_TASKS, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<Task>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }

    // ── Materials ─────────────────────────────────────────────────────────
    fun saveMaterials(materials: List<Material>) {
        prefs.edit().putString(KEY_MATERIALS, gson.toJson(materials)).apply()
    }

    fun loadMaterials(): List<Material> {
        val json = prefs.getString(KEY_MATERIALS, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<Material>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }

    // ── Activities ────────────────────────────────────────────────────────
    fun saveActivities(activities: List<ActivityItem>) {
        prefs.edit().putString(KEY_ACTIVITIES, gson.toJson(activities)).apply()
    }

    fun loadActivities(): List<ActivityItem> {
        val json = prefs.getString(KEY_ACTIVITIES, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<ActivityItem>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }

    // ── Notifications ─────────────────────────────────────────────────────
    fun saveNotifications(notifications: List<AppNotification>) {
        prefs.edit().putString(KEY_NOTIFICATIONS, gson.toJson(notifications)).apply()
    }

    fun loadNotifications(): List<AppNotification> {
        val json = prefs.getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<AppNotification>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }

    // ── Profile ───────────────────────────────────────────────────────────
    fun saveProfile(profile: UserProfile) {
        prefs.edit().putString(KEY_PROFILE, gson.toJson(profile)).apply()
    }

    fun loadProfile(): UserProfile {
        val json = prefs.getString(KEY_PROFILE, null) ?: return UserProfile()
        return try {
            gson.fromJson(json, UserProfile::class.java)
        } catch (e: Exception) { UserProfile() }
    }

    // ── Active Project ────────────────────────────────────────────────────
    fun saveActiveProjectId(id: String?) {
        prefs.edit().putString(KEY_ACTIVE_PROJECT, id).apply()
    }

    fun loadActiveProjectId(): String? = prefs.getString(KEY_ACTIVE_PROJECT, null)
}
