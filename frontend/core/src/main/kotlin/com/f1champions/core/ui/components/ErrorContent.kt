package com.f1champions.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Common error types that can occur across different features.
 */
enum class ErrorType {
    /**
     * Network is offline or unavailable
     */
    OFFLINE,

    /**
     * Connection timed out
     */
    TIMEOUT,

    /**
     * Server returned an error
     */
    SERVER_ERROR,

    /**
     * Rate limit exceeded
     */
    RATE_LIMIT,

    /**
     * Resource not found
     */
    NOT_FOUND,

    /**
     * Invalid input or state
     */
    INVALID,

    /**
     * Unexpected error occurred
     */
    UNEXPECTED
}

/**
 * A reusable error content component that displays an error message with an icon and optional retry button.
 *
 * @param errorType The type of error that occurred
 * @param message The error message to display
 * @param canRetry Whether the operation can be retried
 * @param onRetry Callback when the retry button is clicked
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun ErrorContent(
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
                ErrorType.NOT_FOUND -> Icons.Default.Info
                ErrorType.INVALID -> Icons.Default.Info
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