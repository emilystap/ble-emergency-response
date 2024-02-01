package com.example.smartvest

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartvest.ui.HomeScreen
import com.example.smartvest.ui.SettingsScreen
import com.example.smartvest.util.PermissionHandler

sealed class AppScreen(val route: String) {
    data object Home : AppScreen("home")
    data object Settings : AppScreen("settings")
}

@Composable
fun AppNav(permissionHandler: PermissionHandler) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreen.Home.route) {
        composable(AppScreen.Home.route) { HomeScreen(navController, permissionHandler) }
        composable(AppScreen.Settings.route) { SettingsScreen(navController, permissionHandler) }
    }
}
