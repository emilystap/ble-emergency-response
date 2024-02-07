package com.example.smartvest.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class AppScreen(val route: String) {
    data object Home : AppScreen("home")
    data object Settings : AppScreen("settings")
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreen.Home.route) {
        composable(AppScreen.Home.route) { HomeScreen(navController) }
        composable(AppScreen.Settings.route) { SettingsScreen(navController) }
    }
}