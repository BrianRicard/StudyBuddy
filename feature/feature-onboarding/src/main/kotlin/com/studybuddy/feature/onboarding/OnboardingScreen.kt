package com.studybuddy.feature.onboarding

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.common.locale.SupportedLocale
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.theme.StudyBuddyTheme
import java.util.Locale

/**
 * Entry-point composable for the Onboarding flow.
 * Wires [OnboardingViewModel] to the stateless [OnboardingContent].
 */
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OnboardingEffect.NavigateToHome -> onNavigateToHome()
                is OnboardingEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    val localeOverride = remember(state.selectedLocale) {
        val locale = Locale(state.selectedLocale)
        Configuration(android.content.res.Resources.getSystem().configuration).apply {
            setLocale(locale)
        }
    }

    CompositionLocalProvider(LocalConfiguration provides localeOverride) {
        OnboardingContent(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = modifier,
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
private fun OnboardingContent(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        WelcomeStep(
            name = state.name,
            nameError = state.nameError,
            selectedLocale = state.selectedLocale,
            isCompleting = state.isCompleting,
            onNameChange = { onIntent(OnboardingIntent.SetName(it)) },
            onLocaleSelect = {
                onIntent(OnboardingIntent.SelectLocale(it))
            },
            onComplete = { onIntent(OnboardingIntent.Complete) },
            modifier = Modifier.padding(padding),
        )
    }
}

// region Welcome

@Composable
private fun WelcomeStep(
    name: String,
    nameError: String?,
    selectedLocale: String,
    isCompleting: Boolean,
    onNameChange: (String) -> Unit,
    onLocaleSelect: (String) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Professor Goose greeting
        Image(
            painter = painterResource(CoreUiR.drawable.profgoose_hi),
            contentDescription = stringResource(CoreUiR.string.onboarding_welcome_title),
            modifier = Modifier.size(120.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(CoreUiR.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(CoreUiR.string.onboarding_whats_name),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(CoreUiR.string.onboarding_your_name)) },
            singleLine = true,
            isError = nameError != null,
            supportingText = nameError?.let { error ->
                { Text(text = error) }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (name.isNotBlank()) onComplete() }),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(CoreUiR.string.onboarding_choose_language),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Language selector row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            SupportedLocale.entries.forEach { locale ->
                LanguageCard(
                    locale = locale,
                    isSelected = locale.code == selectedLocale,
                    onClick = { onLocaleSelect(locale.code) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        StudyBuddyButton(
            text = if (isCompleting) {
                stringResource(CoreUiR.string.onboarding_saving)
            } else {
                stringResource(CoreUiR.string.onboarding_lets_go)
            },
            onClick = onComplete,
            enabled = name.isNotBlank() && !isCompleting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        )
    }
}

@Composable
private fun LanguageCard(
    locale: SupportedLocale,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val flagRes = when (locale) {
        SupportedLocale.FRENCH -> CoreUiR.drawable.ic_flag_fr
        SupportedLocale.ENGLISH -> CoreUiR.drawable.ic_flag_gb
        SupportedLocale.GERMAN -> CoreUiR.drawable.ic_flag_de
    }

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "locale-border-${locale.code}",
    )

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor,
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(flagRes),
                contentDescription = locale.displayName,
                modifier = Modifier.size(36.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = locale.displayName,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// endregion

// region Previews

@Preview(showBackground = true)
@Composable
private fun OnboardingWelcomePreview() {
    StudyBuddyTheme {
        OnboardingContent(
            state = OnboardingState(
                name = "",
                selectedLocale = "en",
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingWelcomeFilledPreview() {
    StudyBuddyTheme {
        OnboardingContent(
            state = OnboardingState(
                name = "Sophie",
                selectedLocale = "en",
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingWelcomeErrorPreview() {
    StudyBuddyTheme {
        OnboardingContent(
            state = OnboardingState(
                name = "",
                selectedLocale = "en",
                nameError = "Please enter your name",
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingCompletingPreview() {
    StudyBuddyTheme {
        OnboardingContent(
            state = OnboardingState(
                name = "Sophie",
                selectedLocale = "fr",
                isCompleting = true,
            ),
            onIntent = {},
        )
    }
}

// endregion
