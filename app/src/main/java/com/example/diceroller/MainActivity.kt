package com.example.diceroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.diceroller.ui.theme.DiceRollerTheme

class MainActivity : ComponentActivity() {

    // Shared ViewModel – survives configuration changes
    private val viewModel: DiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Re-reads isDarkTheme whenever it changes, re-applying the Material theme
            DiceRollerTheme(darkTheme = viewModel.isDarkTheme) {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}

/**
 * Simple in-process navigation between the two screens.
 * No extra navigation dependency required.
 */
@Composable
fun AppNavigation(viewModel: DiceViewModel) {
    var screen by remember { mutableStateOf(Screen.Main) }

    when (screen) {
        Screen.Main -> MainScreen(
            viewModel            = viewModel,
            onNavigateToSettings = { screen = Screen.Settings }
        )
        Screen.Settings -> SettingsScreen(
            viewModel      = viewModel,
            onNavigateBack = { screen = Screen.Main }
        )
    }
}

private enum class Screen { Main, Settings }