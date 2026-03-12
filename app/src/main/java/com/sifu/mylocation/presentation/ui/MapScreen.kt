package com.sifu.mylocation.presentation.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.sifu.mylocation.presentation.viewmodel.MapViewModel

class MapScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<MapViewModel>()
        MapScreenContent(viewModel = viewModel)
    }
}