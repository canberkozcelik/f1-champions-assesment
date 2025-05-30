package com.f1champions.feature.racewinners.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.f1champions.feature.racewinners.R
import com.f1champions.feature.racewinners.model.RaceWinner
import com.f1champions.feature.racewinners.ui.ErrorType
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
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
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

@Composable
private fun ErrorContent(
    errorType: ErrorType,
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = when (errorType) {
                ErrorType.OFFLINE -> Icons.Default.CloudOff
                ErrorType.TIMEOUT -> Icons.Default.Timer
                ErrorType.SERVER_ERROR -> Icons.Default.Error
                ErrorType.RATE_LIMIT -> Icons.Default.Timer
                ErrorType.SEASON_NOT_FOUND -> Icons.Default.Info
                ErrorType.INVALID_SEASON -> Icons.Default.Info
                ErrorType.UNEXPECTED -> Icons.Default.Error
            },
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (canRetry) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
} 