package com.sifu.mylocation.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sifu.mylocation.domain.usecasae.GetBranchesUseCase
import com.sifu.mylocation.presentation.state.BranchIntent
import com.sifu.mylocation.presentation.state.MapState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BranchViewModel(
    private val getBranchesUseCase: GetBranchesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    fun onIntent(intent: BranchIntent) {
        when (intent) {
            BranchIntent.LoadBranches -> loadBranches()
        }
    }

    private fun loadBranches() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            getBranchesUseCase()
                .onSuccess { list ->
                    _state.update {
                        it.copy(isLoading = false, branches = list)
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
        }
    }
}
