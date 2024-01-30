package com.example.smartvest

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartvest.ui.HomeScreen
import com.example.smartvest.ui.SettingsScreen

enum class AppScreen {
    Home,
    Settings
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreen.Home.name) {
        composable(AppScreen.Home.name) { HomeScreen(navController) }
        composable(AppScreen.Settings.name) { SettingsScreen(navController) }
    }
}
