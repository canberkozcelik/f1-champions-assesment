package com.f1champions.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.f1champions.app.theme.F1ChampionsTheme
import com.f1champions.feature.racewinners.navigation.RaceWinnersFeature.screen
import com.f1champions.feature.seasonslist.navigation.SeasonsListFeature
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            F1ChampionsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }
}

@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = SeasonsListFeature.ROUTE
    ) {
        // Seasons List Screen
        composable(SeasonsListFeature.ROUTE) {
            SeasonsListFeature.Screen(navController = navController)
        }

        // Race Winners Screen
        screen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}