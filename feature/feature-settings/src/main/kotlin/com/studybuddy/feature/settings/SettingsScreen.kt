package com.studybuddy.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.common.locale.SupportedLocale
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.ui.components.AvatarComposite
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.components.StudyBuddyOutlinedButton
import com.studybuddy.core.ui.theme.StudyBuddyTheme

/**
 * Entry-point composable for the Settings screen.
 * Wires [SettingsViewModel] to the stateless [SettingsContent].
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit = {},
    onAppReset: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.NavigateTo -> onNavigate(effect.route)
                is SettingsEffect.ShowToast -> {
                    // Toast can be wired via SnackbarHostState from caller
                }
                is SettingsEffect.AppReset -> onAppReset()
            }
        }
    }

    SettingsContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // -- Profile Card --
                item {
                    ProfileCard(
                        name = state.profileName,
                        avatarConfig = state.avatarConfig,
                        onClick = { onIntent(SettingsIntent.NavigateToAvatarCloset) },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // -- General Section --
                item { SectionHeader(title = "General") }
                item {
                    LanguageSettingRow(
                        currentLocale = state.locale,
                        onLocaleSelected = { onIntent(SettingsIntent.SetLocale(it)) },
                    )
                }
                item {
                    SwitchSettingRow(
                        label = "Sound Effects",
                        isChecked = state.isSoundEnabled,
                        onToggle = { onIntent(SettingsIntent.ToggleSound) },
                    )
                }
                item {
                    SwitchSettingRow(
                        label = "Haptic Feedback",
                        isChecked = state.isHapticEnabled,
                        onToggle = { onIntent(SettingsIntent.ToggleHaptic) },
                    )
                }

                // -- Learning Section --
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionHeader(title = "Learning")
                }
                item {
                    DailyGoalSettingRow(
                        currentGoal = state.dailyGoal,
                        onGoalSelected = { onIntent(SettingsIntent.SetDailyGoal(it)) },
                    )
                }
                item {
                    AccentModeSettingRow(
                        isStrict = state.isAccentStrict,
                        onToggle = { onIntent(SettingsIntent.ToggleAccentStrict) },
                    )
                }

                // -- Parent Zone Section --
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionHeader(title = "Parent Zone")
                }
                item {
                    ParentZoneLockRow(
                        isUnlocked = state.showParentZone,
                        onClick = { onIntent(SettingsIntent.OpenParentZone) },
                    )
                }
                if (state.showParentZone) {
                    item {
                        NavigationSettingRow(
                            label = "Progress & Stats",
                            onClick = { onIntent(SettingsIntent.NavigateToStats) },
                        )
                    }
                    item {
                        NavigationSettingRow(
                            label = "Backup & Export",
                            onClick = { onIntent(SettingsIntent.NavigateToBackup) },
                        )
                    }
                    item {
                        DisabledSettingRow(
                            label = "Cloud Sync",
                            subtitle = "Coming Soon",
                        )
                    }
                    item {
                        ResetSettingRow(
                            onClick = { onIntent(SettingsIntent.RequestReset) },
                        )
                    }
                }

                // Bottom spacer for scroll padding
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    // -- Dialogs --
    if (state.showPinDialog) {
        PinEntryDialog(
            isNewPin = !state.parentPinSet,
            error = state.pinError,
            onSubmit = { pin ->
                if (state.parentPinSet) {
                    onIntent(SettingsIntent.SubmitPin(pin))
                } else {
                    onIntent(SettingsIntent.SetNewPin(pin))
                }
            },
            onDismiss = { onIntent(SettingsIntent.DismissPinDialog) },
        )
    }

    if (state.showResetDialog) {
        ResetConfirmationDialog(
            onConfirm = { text -> onIntent(SettingsIntent.ConfirmReset(text)) },
            onDismiss = { onIntent(SettingsIntent.DismissResetDialog) },
        )
    }
}

// region Profile Card

@Composable
private fun ProfileCard(
    name: String,
    avatarConfig: AvatarConfig?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StudyBuddyCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarComposite(
                config = avatarConfig ?: AvatarConfig.default(),
                size = 50.dp,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.ifEmpty { "Student" },
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Tap to edit avatar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go to avatar closet",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// endregion

// region Section Header

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 8.dp),
    )
}

// endregion

// region Setting Rows

@Composable
private fun SwitchSettingRow(
    label: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun LanguageSettingRow(
    currentLocale: String,
    onLocaleSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }
    val displayName = SupportedLocale.fromCode(currentLocale).displayName

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Language",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

    if (showDialog) {
        LocalePickerDialog(
            currentLocale = currentLocale,
            onLocaleSelected = { locale ->
                onLocaleSelected(locale)
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun DailyGoalSettingRow(
    currentGoal: Int,
    onGoalSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Daily Goal",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "$currentGoal activities",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

    if (showDialog) {
        DailyGoalPickerDialog(
            currentGoal = currentGoal,
            onGoalSelected = { goal ->
                onGoalSelected(goal)
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun AccentModeSettingRow(
    isStrict: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Accent Mode",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = if (isStrict) "Strict" else "Lenient",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = isStrict,
            onCheckedChange = { onToggle() },
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun NavigationSettingRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate to $label",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun DisabledSettingRow(
    label: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun ParentZoneLockRow(
    isUnlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
            contentDescription = if (isUnlocked) "Parent zone unlocked" else "Parent zone locked",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (isUnlocked) "Parent Zone (Unlocked)" else "Unlock Parent Zone",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun ResetSettingRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Reset All Data",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

// endregion

// region Dialogs

@Composable
private fun LocalePickerDialog(
    currentLocale: String,
    onLocaleSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language") },
        text = {
            Column {
                SupportedLocale.entries.forEach { locale ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLocaleSelected(locale.code) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = locale.code == currentLocale,
                            onClick = { onLocaleSelected(locale.code) },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = locale.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun DailyGoalPickerDialog(
    currentGoal: Int,
    onGoalSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Daily Goal") },
        text = {
            Column {
                DAILY_GOAL_OPTIONS.forEach { goal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGoalSelected(goal) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = goal == currentGoal,
                            onClick = { onGoalSelected(goal) },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$goal activities per day",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun PinEntryDialog(
    isNewPin: Boolean,
    error: String?,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNewPin) "Create Parent PIN" else "Enter Parent PIN",
            )
        },
        text = {
            Column {
                Text(
                    text = if (isNewPin) {
                        "Set a 4-digit PIN to protect parent settings."
                    } else {
                        "Enter your 4-digit PIN to access parent settings."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { value ->
                        if (value.length <= SettingsViewModel.PIN_LENGTH && value.all { it.isDigit() }) {
                            pin = value
                        }
                    },
                    label = { Text("PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = error != null,
                    supportingText = error?.let { { Text(text = it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            StudyBuddyButton(
                text = if (isNewPin) "Set PIN" else "Unlock",
                onClick = { onSubmit(pin) },
                enabled = pin.length == SettingsViewModel.PIN_LENGTH,
            )
        },
        dismissButton = {
            StudyBuddyOutlinedButton(
                text = "Cancel",
                onClick = onDismiss,
            )
        },
    )
}

@Composable
private fun ResetConfirmationDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var confirmText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reset All Data?",
                color = MaterialTheme.colorScheme.error,
            )
        },
        text = {
            Column {
                Text(
                    text = "This will permanently delete all profiles, " +
                        "progress, rewards, and settings. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Type RESET to confirm:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it },
                    singleLine = true,
                    placeholder = { Text("RESET") },
                    isError = confirmText.isNotEmpty() &&
                        confirmText != SettingsViewModel.RESET_CONFIRMATION_TEXT,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            StudyBuddyButton(
                text = "Reset Everything",
                onClick = { onConfirm(confirmText) },
                enabled = confirmText == SettingsViewModel.RESET_CONFIRMATION_TEXT,
            )
        },
        dismissButton = {
            StudyBuddyOutlinedButton(
                text = "Cancel",
                onClick = onDismiss,
            )
        },
    )
}

// endregion

// region Constants

private val DAILY_GOAL_OPTIONS = listOf(3, 5, 10)

// endregion

// region Previews

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    StudyBuddyTheme {
        SettingsContent(
            state = SettingsState(
                profileName = "Lea",
                avatarConfig = AvatarConfig(
                    bodyId = "unicorn",
                    hatId = "hat_crown",
                    faceId = "none",
                    outfitId = "default",
                    petId = "pet_chick",
                ),
                locale = "fr",
                isSoundEnabled = true,
                isHapticEnabled = true,
                dailyGoal = 5,
                isAccentStrict = false,
                selectedTheme = "sunset",
                showParentZone = false,
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenParentZonePreview() {
    StudyBuddyTheme {
        SettingsContent(
            state = SettingsState(
                profileName = "Max",
                avatarConfig = AvatarConfig.default(),
                locale = "en",
                isSoundEnabled = false,
                isHapticEnabled = true,
                dailyGoal = 10,
                isAccentStrict = true,
                selectedTheme = "ocean",
                showParentZone = true,
                parentPinSet = true,
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenLoadingPreview() {
    StudyBuddyTheme {
        SettingsContent(
            state = SettingsState(isLoading = true),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PinDialogNewPreview() {
    StudyBuddyTheme {
        PinEntryDialog(
            isNewPin = true,
            error = null,
            onSubmit = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PinDialogErrorPreview() {
    StudyBuddyTheme {
        PinEntryDialog(
            isNewPin = false,
            error = "Incorrect PIN. Try again.",
            onSubmit = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResetDialogPreview() {
    StudyBuddyTheme {
        ResetConfirmationDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}

// endregion
