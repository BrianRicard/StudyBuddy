package com.studybuddy.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.common.locale.SupportedLocale
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.domain.model.CharacterBody
import com.studybuddy.core.domain.model.RewardCatalog
import com.studybuddy.core.domain.model.RewardItem
import com.studybuddy.core.ui.components.AvatarComposite
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.components.StudyBuddyOutlinedButton
import com.studybuddy.core.ui.theme.StudyBuddyTheme

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

    OnboardingContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun OnboardingContent(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val pagerState = rememberPagerState(
        initialPage = state.currentStep,
        pageCount = { OnboardingViewModel.TOTAL_STEPS },
    )

    // Sync pager position with ViewModel state
    LaunchedEffect(state.currentStep) {
        if (pagerState.currentPage != state.currentStep) {
            pagerState.animateScrollToPage(state.currentStep)
        }
    }

    // Pager-to-ViewModel sync removed: userScrollEnabled = false means the pager
    // is only driven programmatically via the LaunchedEffect above. The previous
    // snapshotFlow on settledPage caused a feedback loop that skipped the avatar step.

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Step indicator dots
            StepIndicator(
                currentStep = state.currentStep,
                totalSteps = OnboardingViewModel.TOTAL_STEPS,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                userScrollEnabled = false,
            ) { page ->
                when (page) {
                    OnboardingViewModel.STEP_WELCOME -> WelcomeStep(
                        name = state.name,
                        nameError = state.nameError,
                        selectedLocale = state.selectedLocale,
                        onNameChange = { onIntent(OnboardingIntent.SetName(it)) },
                        onLocaleSelect = {
                            onIntent(OnboardingIntent.SelectLocale(it))
                        },
                        onNext = { onIntent(OnboardingIntent.NextStep) },
                    )
                    OnboardingViewModel.STEP_AVATAR -> AvatarStep(
                        avatarConfig = state.avatarConfig,
                        onSelectCharacter = {
                            onIntent(OnboardingIntent.SelectCharacter(it))
                        },
                        onSelectHat = {
                            onIntent(OnboardingIntent.SelectHat(it))
                        },
                        onSelectFace = {
                            onIntent(OnboardingIntent.SelectFace(it))
                        },
                        onBack = { onIntent(OnboardingIntent.PreviousStep) },
                        onNext = { onIntent(OnboardingIntent.NextStep) },
                    )
                    OnboardingViewModel.STEP_VOICE -> VoiceStep(
                        isCompleting = state.isCompleting,
                        onComplete = { onIntent(OnboardingIntent.Complete) },
                        onBack = { onIntent(OnboardingIntent.PreviousStep) },
                    )
                }
            }
        }
    }
}

// region Step Indicator

@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(totalSteps) { index ->
            val color by animateColorAsState(
                targetValue = if (index <= currentStep) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                label = "step-dot-$index",
            )
            Box(
                modifier = Modifier
                    .size(if (index == currentStep) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

// endregion

// region Step 1 — Welcome

@Composable
private fun WelcomeStep(
    name: String,
    nameError: String?,
    selectedLocale: String,
    onNameChange: (String) -> Unit,
    onLocaleSelect: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Wave emoji hero
        Text(
            text = "\uD83D\uDC4B",
            fontSize = 72.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to StudyBuddy!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "What's your name?",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your name") },
            singleLine = true,
            isError = nameError != null,
            supportingText = nameError?.let { error ->
                { Text(text = error) }
            },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Choose your language",
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
            text = "Next",
            onClick = onNext,
            enabled = name.isNotBlank(),
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
    val flag = when (locale) {
        SupportedLocale.FRENCH -> "\uD83C\uDDEB\uD83C\uDDF7"
        SupportedLocale.ENGLISH -> "\uD83C\uDDEC\uD83C\uDDE7"
        SupportedLocale.GERMAN -> "\uD83C\uDDE9\uD83C\uDDEA"
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
            Text(
                text = flag,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
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

// region Step 2 — Choose Your Buddy

@Composable
private fun AvatarStep(
    avatarConfig: AvatarConfig,
    onSelectCharacter: (String) -> Unit,
    onSelectHat: (String) -> Unit,
    onSelectFace: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose Your Buddy",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Live avatar preview
        AvatarComposite(
            config = avatarConfig,
            size = 120.dp,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Character grid — 4 columns, 8 characters
        Text(
            text = "Pick a character",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start),
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(CHARACTER_GRID_COLUMNS),
            modifier = Modifier
                .fillMaxWidth()
                .height(CHARACTER_GRID_HEIGHT),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = RewardCatalog.characters,
                key = { it.id },
            ) { character ->
                CharacterCard(
                    character = character,
                    isSelected = character.id == avatarConfig.bodyId,
                    onClick = { onSelectCharacter(character.id) },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Free hats row
        Text(
            text = "Pick a hat",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start),
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val freeHats = RewardCatalog.hats.filter { it.cost == 0 }
            items(items = freeHats, key = { it.id }) { hat ->
                AccessoryChip(
                    item = hat,
                    isSelected = hat.id == avatarConfig.hatId,
                    onClick = { onSelectHat(hat.id) },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Free face accessories row
        Text(
            text = "Pick a face accessory",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start),
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val freeFaces = RewardCatalog.faceAccessories.filter { it.cost == 0 }
            items(items = freeFaces, key = { it.id }) { face ->
                AccessoryChip(
                    item = face,
                    isSelected = face.id == avatarConfig.faceId,
                    onClick = { onSelectFace(face.id) },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Info banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.tertiaryContainer,
        ) {
            Text(
                text = "\u2B50 Earn stars to unlock more!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 10.dp,
                ),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Back + Next buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StudyBuddyOutlinedButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f),
            )
            StudyBuddyButton(
                text = "Next",
                onClick = onNext,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CharacterCard(
    character: CharacterBody,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "char-border-${character.id}",
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
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = character.emoji,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = character.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AccessoryChip(
    item: RewardItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "accessory-border-${item.id}",
    )

    Card(
        onClick = onClick,
        modifier = modifier.width(72.dp),
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
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (item.icon.isNotEmpty()) item.icon else "\u2796",
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// endregion

// region Step 3 — Voice Setup

@Composable
private fun VoiceStep(
    isCompleting: Boolean,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Speaker emoji hero
        Text(
            text = "\uD83D\uDD0A",
            fontSize = 72.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Voice Setup",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Download voices for offline dict\u00E9e",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Language voice cards (placeholder — show "Ready" for now)
        SupportedLocale.entries.forEach { locale ->
            VoiceLanguageCard(
                locale = locale,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // "Skip for now" text link
        TextButton(onClick = onComplete) {
            Text(
                text = "Skip for now",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Back + Let's Go! buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StudyBuddyOutlinedButton(
                text = "Back",
                onClick = onBack,
                enabled = !isCompleting,
                modifier = Modifier.weight(1f),
            )
            StudyBuddyButton(
                text = if (isCompleting) "Saving..." else "Let's Go!",
                onClick = onComplete,
                enabled = !isCompleting,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun VoiceLanguageCard(
    locale: SupportedLocale,
    modifier: Modifier = Modifier,
) {
    val flag = when (locale) {
        SupportedLocale.FRENCH -> "\uD83C\uDDEB\uD83C\uDDF7"
        SupportedLocale.ENGLISH -> "\uD83C\uDDEC\uD83C\uDDE7"
        SupportedLocale.GERMAN -> "\uD83C\uDDE9\uD83C\uDDEA"
    }

    StudyBuddyCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = flag,
                    fontSize = 28.sp,
                )
                Column {
                    Text(
                        text = locale.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Voice pack",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Placeholder status — ready indicator
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = "\u2705 Ready",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 4.dp,
                    ),
                )
            }
        }
    }
}

// endregion

// region Constants

private const val CHARACTER_GRID_COLUMNS = 4
private val CHARACTER_GRID_HEIGHT = 170.dp

// endregion

// region Previews

@Preview(showBackground = true)
@Composable
private fun OnboardingWelcomePreview() {
    StudyBuddyTheme {
        OnboardingContent(
            state = OnboardingState(
                currentStep = OnboardingViewModel.STEP_WELCOME,
                name = "",
                selectedLocale = "fr",
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
                currentStep = OnboardingViewModel.STEP_WELCOME,
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
                currentStep = OnboardingViewModel.STEP_WELCOME,
                name = "",
                selectedLocale = "fr",
                nameError = "Please enter your name",
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingAvatarPreview() {
    StudyBuddyTheme {
        OnboardingContent(
            state = OnboardingState(
                currentStep = OnboardingViewModel.STEP_AVATAR,
                name = "Sophie",
                avatarConfig = AvatarConfig(
                    bodyId = "unicorn",
                    hatId = "hat_tophat",
                    faceId = "face_shades",
                    outfitId = "default",
                    petId = "none",
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingVoicePreview() {
    StudyBuddyTheme {
        OnboardingContent(
            state = OnboardingState(
                currentStep = OnboardingViewModel.STEP_VOICE,
                name = "Sophie",
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingVoiceCompletingPreview() {
    StudyBuddyTheme {
        OnboardingContent(
            state = OnboardingState(
                currentStep = OnboardingViewModel.STEP_VOICE,
                name = "Sophie",
                isCompleting = true,
            ),
            onIntent = {},
        )
    }
}

// endregion
