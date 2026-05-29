package com.gymtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.gymtracker.ui.exercises.ExerciseDetailScreen
import com.gymtracker.ui.exercises.ExerciseListScreen
import com.gymtracker.ui.home.HomeScreen
import com.gymtracker.ui.photos.PhotosScreen
import com.gymtracker.ui.progress.ExerciseProgressScreen
import com.gymtracker.ui.progress.ProgressScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ExerciseList : Screen("exercises/{groupId}") {
        fun route(groupId: Long) = "exercises/$groupId"
    }
    object ExerciseDetail : Screen("exercise/{exerciseId}") {
        fun route(exerciseId: Long) = "exercise/$exerciseId"
    }
    object Progress : Screen("progress")
    object ExerciseProgress : Screen("progress/{exerciseId}") {
        fun route(exerciseId: Long) = "progress/$exerciseId"
    }
    object Photos : Screen("photos")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val topLevelRoutes = listOf(Screen.Home, Screen.Progress, Screen.Photos)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                topLevelRoutes.forEach { screen ->
                    val (label, icon) = when (screen) {
                        Screen.Home -> "Ejercicios" to Icons.Default.FitnessCenter
                        Screen.Progress -> "Progresión" to Icons.Default.BarChart
                        Screen.Photos -> "Fotos" to Icons.Default.PhotoLibrary
                        else -> return@forEach
                    }
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) {
                HomeScreen(innerPadding, onGroupClick = { groupId ->
                    navController.navigate(Screen.ExerciseList.route(groupId))
                })
            }
            composable(
                Screen.ExerciseList.route,
                arguments = listOf(navArgument("groupId") { type = NavType.LongType })
            ) { backStackEntry ->
                ExerciseListScreen(
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    groupId = backStackEntry.arguments!!.getLong("groupId"),
                    onBack = { navController.popBackStack() },
                    onExerciseClick = { exerciseId ->
                        navController.navigate(Screen.ExerciseDetail.route(exerciseId))
                    }
                )
            }
            composable(
                Screen.ExerciseDetail.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
            ) { backStackEntry ->
                ExerciseDetailScreen(
                    exerciseId = backStackEntry.arguments!!.getLong("exerciseId"),
                    onBack = { navController.popBackStack() },
                    bottomPadding = innerPadding.calculateBottomPadding()
                )
            }
            composable(Screen.Progress.route) {
                ProgressScreen(innerPadding, onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.ExerciseProgress.route(exerciseId))
                })
            }
            composable(
                Screen.ExerciseProgress.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
            ) { backStackEntry ->
                ExerciseProgressScreen(
                    exerciseId = backStackEntry.arguments!!.getLong("exerciseId"),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Photos.route) {
                PhotosScreen(innerPadding)
            }
        }
    }
}
