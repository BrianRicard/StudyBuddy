package com.studybuddy.feature.reading.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import com.studybuddy.feature.reading.R

@Composable
fun ReadingResultsScreen(
    onNavigateToPassage: (String) -> Unit,
    onNavigateHome: () -> Unit,
    viewModel: ReadingResultsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ReadingResultsEffect.NavigateToPassage ->
                    onNavigateToPassage(effect.passageId)
                is ReadingResultsEffect.NavigateHome -> onNavigateHome()
            }
        }
    }

    ReadingResultsContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun ReadingResultsContent(
    state: ReadingResultsState,
    onIntent: (ReadingResultsIntent) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.reading_results_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${state.score}/${state.totalQuestions}",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.reading_correct_answers),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    text = "+${state.pointsEarned}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            if (state.allCorrectFirstTry) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.reading_first_try_bonus),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { onIntent(ReadingResultsIntent.ReadAgain) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.reading_read_again))
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onIntent(ReadingResultsIntent.GoHome) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.reading_go_home))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadingResultsPreview() {
    StudyBuddyTheme {
        ReadingResultsContent(
            state = ReadingResultsState(
                passageTitle = "The Tortoise and the Hare",
                score = 3,
                totalQuestions = 3,
                pointsEarned = 20,
                allCorrectFirstTry = true,
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}
