package com.studybuddy.app.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.adaptive.LayoutType
import com.studybuddy.core.ui.navigation.navigateSafely

private val DRAWER_WIDTH = 240.dp

@Composable
fun StudyBuddyScaffold(
    navController: NavHostController,
    layoutType: LayoutType,
    content: @Composable (Modifier) -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val activeDestination = resolveActiveDestination(currentRoute)
    val isActiveSession = isActiveSessionRoute(currentRoute)

    // Session exit confirmation state
    var pendingNavigation by remember { mutableStateOf<NavDestination?>(null) }

    val onNavigate: (NavDestination) -> Unit = { destination ->
        if (isActiveSession && destination != activeDestination) {
            pendingNavigation = destination
        } else {
            navigateTopLevel(navController, destination)
        }
    }

    // Session exit dialog
    if (pendingNavigation != null) {
        SessionExitDialog(
            onConfirm = {
                pendingNavigation?.let { navigateTopLevel(navController, it) }
                pendingNavigation = null
            },
            onDismiss = { pendingNavigation = null },
        )
    }

    when (layoutType) {
        LayoutType.COMPACT -> {
            PhoneScaffold(
                currentRoute = currentRoute,
                activeDestination = activeDestination,
                onNavigate = onNavigate,
                content = content,
            )
        }
        LayoutType.MEDIUM -> {
            TabletRailScaffold(
                activeDestination = activeDestination,
                onNavigate = onNavigate,
                content = content,
            )
        }
        LayoutType.EXPANDED -> {
            TabletDrawerScaffold(
                activeDestination = activeDestination,
                onNavigate = onNavigate,
                content = content,
            )
        }
    }
}

// ─── Phone: Bottom NavigationBar ───────────────────────────────────────────────

@Composable
private fun PhoneScaffold(
    currentRoute: String?,
    activeDestination: NavDestination?,
    onNavigate: (NavDestination) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    val showBottomNav = currentRoute in PHONE_NAV_ROUTES

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                PhoneBottomNav(
                    activeDestination = activeDestination,
                    onNavigate = onNavigate,
                )
            }
        },
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}

@Composable
private fun PhoneBottomNav(
    activeDestination: NavDestination?,
    onNavigate: (NavDestination) -> Unit,
) {
    NavigationBar {
        PHONE_NAV_DESTINATIONS.forEach { destination ->
            val isSelected = activeDestination == destination
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) {
                            destination.selectedIcon
                        } else {
                            destination.unselectedIcon
                        },
                        contentDescription = stringResource(destination.labelResId),
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.labelResId),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

// ─── Tablet Medium: NavigationRail ─────────────────────────────────────────────

@Composable
private fun TabletRailScaffold(
    activeDestination: NavDestination?,
    onNavigate: (NavDestination) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        TabletNavRail(
            activeDestination = activeDestination,
            onNavigate = onNavigate,
        )
        content(Modifier.weight(1f))
    }
}

@Composable
private fun TabletNavRail(
    activeDestination: NavDestination?,
    onNavigate: (NavDestination) -> Unit,
) {
    NavigationRail {
        Spacer(Modifier.height(8.dp))
        TABLET_NAV_DESTINATIONS.forEach { destination ->
            val isSelected = activeDestination == destination
            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) {
                            destination.selectedIcon
                        } else {
                            destination.unselectedIcon
                        },
                        contentDescription = stringResource(destination.labelResId),
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.labelResId),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
        Spacer(Modifier.weight(1f))
    }
}

// ─── Tablet Expanded: PermanentNavigationDrawer ────────────────────────────────

@Composable
private fun TabletDrawerScaffold(
    activeDestination: NavDestination?,
    onNavigate: (NavDestination) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    PermanentNavigationDrawer(
        drawerContent = {
            TabletDrawerContent(
                activeDestination = activeDestination,
                onNavigate = onNavigate,
            )
        },
    ) {
        content(Modifier.fillMaxSize())
    }
}

@Composable
private fun TabletDrawerContent(
    activeDestination: NavDestination?,
    onNavigate: (NavDestination) -> Unit,
) {
    PermanentDrawerSheet(modifier = Modifier.width(DRAWER_WIDTH)) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp),
            ) {
                Icon(
                    imageVector = NavDestination.HOME.selectedIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "StudyBuddy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))

        // "Learn" section
        Text(
            text = stringResource(CoreUiR.string.nav_section_learn),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
        )

        LEARNING_DESTINATIONS.forEach { destination ->
            DrawerNavItem(
                destination = destination,
                isSelected = activeDestination == destination,
                onClick = { onNavigate(destination) },
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))

        // "More" section
        Text(
            text = stringResource(CoreUiR.string.nav_section_more),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
        )

        UTILITY_DESTINATIONS.forEach { destination ->
            DrawerNavItem(
                destination = destination,
                isSelected = activeDestination == destination,
                onClick = { onNavigate(destination) },
            )
        }

        Spacer(Modifier.weight(1f))

        // Settings pinned to bottom
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        NavigationDrawerItem(
            selected = activeDestination == NavDestination.SETTINGS,
            onClick = { onNavigate(NavDestination.SETTINGS) },
            icon = {
                Icon(
                    imageVector = if (activeDestination == NavDestination.SETTINGS) {
                        NavDestination.SETTINGS.selectedIcon
                    } else {
                        Icons.Outlined.Settings
                    },
                    contentDescription = null,
                )
            },
            label = { Text(stringResource(NavDestination.SETTINGS.labelResId)) },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun DrawerNavItem(
    destination: NavDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                contentDescription = null,
            )
        },
        label = { Text(stringResource(destination.labelResId)) },
        modifier = Modifier.padding(horizontal = 12.dp),
    )
}

// ─── Session Exit Dialog ───────────────────────────────────────────────────────

@Composable
private fun SessionExitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(CoreUiR.string.session_exit_title)) },
        text = { Text(stringResource(CoreUiR.string.session_exit_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(CoreUiR.string.session_exit_leave))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(CoreUiR.string.session_exit_stay))
            }
        },
    )
}

// ─── Shared Navigation Logic ───────────────────────────────────────────────────

private fun navigateTopLevel(
    navController: NavHostController,
    destination: NavDestination,
) {
    navController.navigateSafely(destination.route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
