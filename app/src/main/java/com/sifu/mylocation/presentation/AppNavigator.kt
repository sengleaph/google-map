package com.sifu.mylocation.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.sifu.mylocation.presentation.ui.MapScreen

@Composable
fun AppNavigator() {
    Navigator(
        screen = MapScreen(),
        onBackPressed = { currentScreen ->
            true // allow back press to pop
        }
    ) { navigator ->
        SlideTransition(navigator = navigator)
    }
}