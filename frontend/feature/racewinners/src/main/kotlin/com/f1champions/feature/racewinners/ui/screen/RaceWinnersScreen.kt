package com.f1champions.feature.racewinners.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.f1champions.core.ui.components.ErrorContent
import com.f1champions.core.ui.components.LoadingIndicator
import com.f1champions.feature.racewinners.R
import com.f1champions.feature.racewinners.model.RaceWinner
import com.f1champions.feature.racewinners.ui.RaceWinnersUiState
import com.f1champions.feature.racewinners.ui.RaceWinnersViewModel

/**
 * Screen that displays the list of race winners for a specific season.
 *
 * @param year The championship year to display race winners for
 * @param onNavigateBack Callback for handling back navigation
 * @param viewModel The view model for this screen, provided by Hilt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceWinnersScreen(
    year: Int,
    onNavigateBack: () -> Unit,
    viewModel: RaceWinnersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(year) {
        viewModel.loadRaceWinners(year)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$year Season Race Winners") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is RaceWinnersUiState.Loading -> {
                    LoadingIndicator()
                }

                is RaceWinnersUiState.Success -> {
                    val raceWinners = (uiState as RaceWinnersUiState.Success).raceWinners
                    RaceWinnersList(raceWinners = raceWinners)
                }

                is RaceWinnersUiState.Error -> {
                    val error = (uiState as RaceWinnersUiState.Error)
                    ErrorContent(
                        errorType = error.errorType,
                        message = error.message,
                        canRetry = error.canRetry,
                        onRetry = { viewModel.retry(year) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RaceWinnersList(
    raceWinners: List<RaceWinner>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(raceWinners) { raceWinner ->
            RaceWinnerCard(raceWinner = raceWinner)
        }
    }
}

@Composable
private fun RaceWinnerCard(
    raceWinner: RaceWinner,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (raceWinner.isSeasonChampionWinner) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = raceWinner.raceName,
                    style = MaterialTheme.typography.titleMedium
                )
                if (raceWinner.isSeasonChampionWinner) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Season Champion",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = raceWinner.date,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.winner_format,
                    raceWinner.winner.fullName,
                    raceWinner.winner.constructor
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 