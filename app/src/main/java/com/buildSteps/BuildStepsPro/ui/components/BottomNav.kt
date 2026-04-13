package com.buildSteps.BuildStepsPro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.buildSteps.BuildStepsPro.ui.theme.*

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Home",     Icons.Default.Dashboard,    Icons.Default.Home,    "dashboard"),
    BottomNavItem("Projects", Icons.Default.Folder,       Icons.Default.FolderOpen,   "projects"),
    BottomNavItem("Tasks",    Icons.Default.Assignment,   Icons.Default.Assignment,   "tasks_nav"),
    BottomNavItem("Reports",  Icons.Default.BarChart,     Icons.Default.BarChart,     "reports_nav"),
    BottomNavItem("Settings", Icons.Default.Settings,     Icons.Default.Settings,     "settings")
)

@Composable
fun AppBottomNav(
    currentRoute: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceLight)
    ) {
        // Top shadow line
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderLight)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route
                BottomNavTab(item, isSelected) { onItemClick(item.route) }
            }
        }
    }
}

@Composable
private fun BottomNavTab(item: BottomNavItem, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "nav_scale"
    )

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isSelected) {
                Box(
                    Modifier
                        .size(36.dp)
                        .background(BluePrimary.copy(0.1f), RoundedCornerShape(10.dp))
                )
            }
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = if (isSelected) BluePrimary else TextHint,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            item.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) BluePrimary else TextHint,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
