package com.f1champions.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * A reusable loading indicator that can be used across different screens.
 * Displays a centered circular progress indicator.
 *
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "Loading indicator" },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
} 