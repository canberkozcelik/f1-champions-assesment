package com.f1champions.feature.seasonslist

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.f1champions.feature.seasonslist.ui.SeasonsListScreen

/**
 * Entry point for the Seasons List feature.
 * This class provides the public API for integrating the feature into the app.
 */
object SeasonsListFeature {
    /**
     * Route for the seasons list screen in the navigation graph.
     */
    const val ROUTE = "seasons_list"

    /**
     * Composable function that renders the seasons list screen.
     *
     * @param navController Navigation controller for handling navigation events
     */
    @Composable
    fun Screen(
        navController: NavHostController
    ) {
        SeasonsListScreen(
            onSeasonClick = { year ->
                // Navigate to race winners screen for the selected season
                navController.navigate("race_winners/$year")
            }
        )
    }
} 