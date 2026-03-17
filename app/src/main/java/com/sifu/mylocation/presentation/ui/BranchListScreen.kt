package com.sifu.mylocation.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sifu.mylocation.data.dto.BranchDto
import com.sifu.mylocation.presentation.state.BranchIntent
import com.sifu.mylocation.presentation.viewmodel.BranchViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun BranchListScreen(
    viewModel: BranchViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Fire the Intent once on entry
    LaunchedEffect(Unit) {
        viewModel.onIntent(BranchIntent.LoadBranches)
    }

    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.errorMessage != null -> {
            Text(text = "Error: ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
        }
        else -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.branches, key = { it.id }) { branch ->
                    BranchItem(branch = branch)
                }
            }
        }
    }
}

@Composable
fun BranchItem(branch: BranchDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = branch.name)
            Text(text = branch.address)
            Text(text = "📞 ${branch.tel}")
            Text(text = "🏙️ ${branch.province}")
        }
    }
}
