package com.f1champions.feature.seasonslist.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.f1champions.core.ui.components.ErrorContent
import com.f1champions.core.ui.components.ErrorType
import com.f1champions.core.ui.components.LoadingIndicator
import com.f1champions.domain.model.SeasonChampionInfo
import com.f1champions.feature.seasonslist.ui.SeasonsListUiState
import com.f1champions.feature.seasonslist.ui.SeasonsListViewModel

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

    // Load seasons when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadSeasons()
    }

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
                    val error = uiState as SeasonsListUiState.Error
                    ErrorContent(
                        errorType = error.errorType,
                        message = error.message,
                        canRetry = error.canRetry,
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