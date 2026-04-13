package com.buildSteps.BuildStepsPro.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.navigation.*
import androidx.navigation.compose.*
import com.buildSteps.BuildStepsPro.ui.components.*
import com.buildSteps.BuildStepsPro.ui.screens.dashboard.DashboardScreen
import com.buildSteps.BuildStepsPro.ui.screens.dependencies.DependenciesScreen
import com.buildSteps.BuildStepsPro.ui.screens.dependencies.DependencyGraphScreen
import com.buildSteps.BuildStepsPro.ui.screens.errors.ErrorsScreen
import com.buildSteps.BuildStepsPro.ui.screens.errors.FixSuggestionsScreen
import com.buildSteps.BuildStepsPro.ui.screens.materials.*
import com.buildSteps.BuildStepsPro.ui.screens.projects.AddProjectScreen
import com.buildSteps.BuildStepsPro.ui.screens.projects.ProjectsScreen
import com.buildSteps.BuildStepsPro.ui.screens.reports.*
import com.buildSteps.BuildStepsPro.ui.screens.settings.*
import com.buildSteps.BuildStepsPro.ui.screens.splash.SplashScreen
import com.buildSteps.BuildStepsPro.ui.screens.tasks.*
import com.buildSteps.BuildStepsPro.ui.screens.timeline.*
import com.buildSteps.BuildStepsPro.ui.screens.welcome.OnboardingScreen
import com.buildSteps.BuildStepsPro.ui.screens.welcome.WelcomeScreen
import com.buildSteps.BuildStepsPro.ui.screens.zones.AddZoneScreen
import com.buildSteps.BuildStepsPro.ui.screens.zones.ZonesScreen
import com.buildSteps.BuildStepsPro.viewmodel.MainViewModel

private val BOTTOM_NAV_ROUTES = setOf("dashboard", "projects", "tasks_nav", "reports_nav", "settings")

@Composable
fun AppNavGraph(vm: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in BOTTOM_NAV_ROUTES

    val profile by vm.profile.collectAsState()
    val activeProjectId by vm.activeProjectId.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(280, easing = EaseOutCubic)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(280, easing = EaseOutCubic)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(280, easing = EaseOutCubic)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(280, easing = EaseOutCubic)
                )
            }
        ) {

            // ── Splash ─────────────────────────────────────────────────
            composable(Routes.SPLASH) {
                SplashScreen(onFinished = {
                    val dest = if (!profile.onboardingCompleted) Routes.WELCOME else "dashboard"
                    navController.navigate(dest) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                })
            }

            // ── Welcome ────────────────────────────────────────────────
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onStart = { navController.navigate(Routes.ONBOARDING) },
                    onLogin = { navController.navigate(Routes.ONBOARDING) }
                )
            }

            // ── Onboarding ─────────────────────────────────────────────
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onFinished = {
                    vm.completeOnboarding()
                    navController.navigate("dashboard") {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                })
            }

            // ── Dashboard ──────────────────────────────────────────────
            composable("dashboard") {
                DashboardScreen(
                    vm = vm,
                    onProjectsClick    = { navController.navigate("projects") },
                    onTasksClick       = { pid -> navController.navigate(Routes.tasks(pid)) },
                    onErrorsClick      = { pid -> navController.navigate(Routes.errors(pid)) },
                    onTimelineClick    = { pid -> navController.navigate(Routes.timeline(pid)) },
                    onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) }
                )
            }

            // ── Projects ───────────────────────────────────────────────
            composable("projects") {
                ProjectsScreen(
                    vm = vm,
                    onBack        = { navController.navigate("dashboard") { launchSingleTop = true } },
                    onAddProject  = { navController.navigate(Routes.ADD_PROJECT) },
                    onProjectClick = { pid ->
                        vm.setActiveProject(pid)
                        navController.navigate(Routes.zones(pid))
                    }
                )
            }

            composable(Routes.ADD_PROJECT) {
                AddProjectScreen(vm, onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() })
            }

            // ── Zones ──────────────────────────────────────────────────
            composable(Routes.ZONES) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                ZonesScreen(vm, pid,
                    onBack = { navController.popBackStack() },
                    onAddZone = { navController.navigate(Routes.addZone(pid)) },
                    onZoneClick = { navController.navigate(Routes.tasks(pid)) }
                )
            }

            composable(Routes.ADD_ZONE) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                AddZoneScreen(vm, pid,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() })
            }

            // ── Tasks ──────────────────────────────────────────────────
            composable("tasks_nav") {
                val pid = activeProjectId ?: ""
                if (pid.isBlank()) {
                    ProjectsScreen(vm,
                        onBack = { navController.navigate("dashboard") { launchSingleTop = true } },
                        onAddProject = { navController.navigate(Routes.ADD_PROJECT) },
                        onProjectClick = { p -> vm.setActiveProject(p); navController.navigate(Routes.tasks(p)) }
                    )
                } else {
                    TasksScreen(vm, pid,
                        onBack = { navController.navigate("dashboard") { launchSingleTop = true } },
                        onAddTask = { navController.navigate(Routes.addTask(pid)) },
                        onTaskClick = {}
                    )
                }
            }

            composable(Routes.TASKS) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                TasksScreen(vm, pid,
                    onBack = { navController.popBackStack() },
                    onAddTask = { navController.navigate(Routes.addTask(pid)) },
                    onTaskClick = {}
                )
            }

            composable(Routes.ADD_TASK) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                val zid = entry.arguments?.getString("zoneId") ?: "none"
                AddTaskScreen(vm, pid, zid,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() })
            }

            composable(Routes.TASKS_BOARD) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                TasksBoardScreen(vm, pid, onBack = { navController.popBackStack() })
            }

            // ── Dependencies ────────────────────────────────────────────
            composable(Routes.DEPENDENCIES) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                DependenciesScreen(vm, pid,
                    onBack = { navController.popBackStack() },
                    onGraphClick = { navController.navigate(Routes.depGraph(pid)) })
            }

            composable(Routes.DEP_GRAPH) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                DependencyGraphScreen(vm, pid, onBack = { navController.popBackStack() })
            }

            // ── Timeline ────────────────────────────────────────────────
            composable(Routes.TIMELINE) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                TimelineScreen(vm, pid,
                    onBack = { navController.popBackStack() },
                    onAutoPlan = { navController.navigate(Routes.autoPlan(pid)) })
            }

            composable(Routes.AUTO_PLAN) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                AutoPlanScreen(vm, pid, onBack = { navController.popBackStack() })
            }

            // ── Errors ──────────────────────────────────────────────────
            composable(Routes.ERRORS) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                ErrorsScreen(vm, pid,
                    onBack = { navController.popBackStack() },
                    onFixSuggestions = { navController.navigate(Routes.fixSuggestions(pid)) })
            }

            composable(Routes.FIX_SUGGESTIONS) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                FixSuggestionsScreen(vm, pid, onBack = { navController.popBackStack() })
            }

            // ── Materials ────────────────────────────────────────────────
            composable(Routes.MATERIALS) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                MaterialsScreen(vm, pid,
                    onBack = { navController.popBackStack() },
                    onAddMaterial = { navController.navigate(Routes.addMaterial(pid)) })
            }

            composable(Routes.ADD_MATERIAL) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                AddMaterialScreen(vm, pid,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() })
            }

            composable(Routes.BUDGET) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                BudgetScreen(vm, pid, onBack = { navController.popBackStack() })
            }

            composable(Routes.COST_PER_TASK) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                CostPerTaskScreen(vm, pid, onBack = { navController.popBackStack() })
            }

            // ── Progress & Reports ────────────────────────────────────────
            composable(Routes.PROGRESS) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                ProgressScreen(vm, pid, onBack = { navController.popBackStack() })
            }

            composable("reports_nav") {
                val pid = activeProjectId ?: ""
                if (pid.isBlank()) {
                    ProjectsScreen(vm,
                        onBack = { navController.navigate("dashboard") { launchSingleTop = true } },
                        onAddProject = { navController.navigate(Routes.ADD_PROJECT) },
                        onProjectClick = { p -> vm.setActiveProject(p) }
                    )
                } else {
                    ReportsScreen(vm, pid,
                        onBack = { navController.navigate("dashboard") { launchSingleTop = true } },
                        onActivityClick = { navController.navigate(Routes.activity(pid)) })
                }
            }

            composable(Routes.REPORTS) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                ReportsScreen(vm, pid,
                    onBack = { navController.popBackStack() },
                    onActivityClick = { navController.navigate(Routes.activity(pid)) })
            }

            composable(Routes.ACTIVITY) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                ActivityHistoryScreen(vm, pid, onBack = { navController.popBackStack() })
            }

            composable(Routes.CALENDAR) { entry ->
                val pid = entry.arguments?.getString("projectId") ?: return@composable
                CalendarScreen(vm, pid, onBack = { navController.popBackStack() })
            }

            // ── Notifications, Profile, Settings ─────────────────────────
            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(vm, onBack = { navController.popBackStack() })
            }

            composable(Routes.PROFILE) {
                ProfileScreen(vm, onBack = { navController.popBackStack() })
            }

            composable("settings") {
                SettingsScreen(vm,
                    onBack = { navController.navigate("dashboard") { launchSingleTop = true } },
                    onProfileClick = { navController.navigate(Routes.PROFILE) })
            }
        }

        // Bottom Nav overlay
        if (showBottomBar) {
            AppBottomNav(
                currentRoute = currentRoute ?: "dashboard",
                onItemClick = { route ->
                    navController.navigate(route) {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

private val EaseOutCubic = androidx.compose.animation.core.CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
