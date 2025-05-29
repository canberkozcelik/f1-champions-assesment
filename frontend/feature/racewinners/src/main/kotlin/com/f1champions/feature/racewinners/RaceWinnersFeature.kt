package com.f1champions.feature.racewinners

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.f1champions.feature.racewinners.ui.RaceWinnersScreen

/**
 * Feature object for the Race Winners screen.
 * Provides navigation and screen composable for displaying race winners for a specific season.
 */
object RaceWinnersFeature {
    private const val YEAR_ARG = "year"
    const val ROUTE = "race_winners/{$YEAR_ARG}"
    
    /**
     * Adds the Race Winners screen to the navigation graph.
     * The screen requires a year parameter.
     *
     * @param onNavigateBack Callback for handling back navigation
     */
    fun NavGraphBuilder.screen(
        onNavigateBack: () -> Unit
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
                onNavigateBack = onNavigateBack
            )
        }
    }
} 