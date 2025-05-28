package com.f1champions.feature.racewinners

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.f1champions.feature.racewinners.ui.RaceWinnersScreen

/**
 * Feature object for the Race Winners screen.
 * Provides navigation and screen composable for displaying race winners.
 */
object RaceWinnersFeature {
    private const val YEAR_ARG = "year"
    private const val ROUTE = "race_winners/{$YEAR_ARG}"

    /**
     * Creates a navigation route for the Race Winners screen.
     *
     * @param year The championship year to display race winners for
     * @return The navigation route string
     */
    fun createRoute(year: Int) = "race_winners/$year"

    /**
     * Adds the Race Winners screen to the navigation graph.
     *
     * @param onBackClick Callback for handling back navigation
     */
    fun NavGraphBuilder.screen(
        onBackClick: () -> Unit
    ) {
        composable(
            route = ROUTE,
            arguments = listOf(
                navArgument(YEAR_ARG) {
                    type = NavType.IntType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt(YEAR_ARG) ?: return@composable
            
            RaceWinnersScreen(
                year = year,
                onBackClick = onBackClick
            )
        }
    }
} 