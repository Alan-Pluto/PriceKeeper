package com.pricekeeper.app.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pricekeeper.app.core.navigation.AppNavGraph
import com.pricekeeper.app.core.navigation.BottomNavBar
import com.pricekeeper.app.core.navigation.Route

/**
 * Root screen with bottom navigation bar and nav host.
 * Hides bottom bar on detail screens.
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val bottomBarRoutes = setOf(Route.HOME, Route.GOODS, Route.STORE, Route.PROFILE)
    val selectedBottomRoute = navBackStackEntry
        ?.destination
        ?.hierarchy
        ?.firstNotNullOfOrNull { destination ->
            destination.route?.takeIf { it in bottomBarRoutes }
        }
    val showBottomBar = selectedBottomRoute != null
    var lastSelectedBottomRoute by remember { mutableStateOf(Route.HOME) }
    if (selectedBottomRoute != null) {
        lastSelectedBottomRoute = selectedBottomRoute
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = selectedBottomRoute ?: lastSelectedBottomRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
