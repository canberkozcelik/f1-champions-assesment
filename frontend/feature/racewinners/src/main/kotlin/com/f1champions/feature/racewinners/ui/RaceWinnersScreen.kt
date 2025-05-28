package com.f1champions.feature.racewinners.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.f1champions.feature.racewinners.R
import com.f1champions.feature.racewinners.model.RaceWinner

/**
 * Screen that displays the list of race winners for a specific season.
 *
 * @param year The championship year to display race winners for
 * @param onBackClick Callback for handling back navigation
 * @param viewModel The view model for this screen, provided by Hilt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceWinnersScreen(
    year: Int,
    onBackClick: () -> Unit,
    viewModel: RaceWinnersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(year) {
        viewModel.loadRaceWinners(year)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.race_winners_title, year)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is RaceWinnersUiState.Success -> {
                    val raceWinners = (uiState as RaceWinnersUiState.Success).raceWinners
                    RaceWinnersList(raceWinners = raceWinners)
                }
                is RaceWinnersUiState.Error -> {
                    val errorMessage = (uiState as RaceWinnersUiState.Error).message
                    ErrorMessage(
                        message = errorMessage,
                        modifier = Modifier.align(Alignment.Center)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = raceWinner.raceName,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = raceWinner.date,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.winner_format,
                    raceWinner.winner,
                    raceWinner.winner.constructor
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
} 