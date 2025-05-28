package com.f1champions.feature.seasonslist.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.f1champions.domain.model.SeasonChampionInfo

/**
 * Screen that displays the list of Formula 1 World Champions.
 *
 * @param onSeasonClick Callback when a season is clicked, providing the selected year
 * @param viewModel ViewModel that manages the screen's state and data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonsListScreen(
    onSeasonClick: (Int) -> Unit,
    viewModel: SeasonsListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Formula 1 World Champions") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is SeasonsListUiState.Initial -> {
                    // Initial state is handled by Loading state
                    LoadingIndicator()
                }
                is SeasonsListUiState.Loading -> {
                    LoadingIndicator()
                }
                is SeasonsListUiState.Success -> {
                    val seasons = (uiState as SeasonsListUiState.Success).seasons
                    SeasonsList(
                        seasons = seasons,
                        onSeasonClick = onSeasonClick
                    )
                }
                is SeasonsListUiState.Error -> {
                    ErrorContent(
                        message = (uiState as SeasonsListUiState.Error).message,
                        onRetry = viewModel::retry
                    )
                }
            }
        }
    }
}

@Composable
private fun SeasonsList(
    seasons: List<SeasonChampionInfo>,
    onSeasonClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(seasons) { season ->
            SeasonItem(
                season = season,
                onClick = { onSeasonClick(season.year) }
            )
        }
    }
}

@Composable
private fun SeasonItem(
    season: SeasonChampionInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = season.year.toString(),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = season.championName,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
} 