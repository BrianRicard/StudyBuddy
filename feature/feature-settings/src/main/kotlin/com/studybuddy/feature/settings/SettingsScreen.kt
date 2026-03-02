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
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.common.locale.SupportedLocale
import com.studybuddy.core.domain.model.AvatarConfig
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.core.ui.components.LoadingState
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.components.StudyBuddyOutlinedButton
import com.studybuddy.core.ui.modifier.animateItemAppearance
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
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.NavigateTo -> onNavigate(effect.route)
                is SettingsEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageResId))
                }
                is SettingsEffect.AppReset -> onAppReset()
            }
        }
    }

    SettingsContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text(stringResource(CoreUiR.string.settings_title)) })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                // -- General Section --
                item {
                    SectionHeader(
                        title = stringResource(CoreUiR.string.settings_general),
                        modifier = Modifier.animateItemAppearance(0),
                    )
                }
                item {
                    LanguageSettingRow(
                        currentLocale = state.locale,
                        onLocaleSelected = { onIntent(SettingsIntent.SetLocale(it)) },
                        modifier = Modifier.animateItemAppearance(1),
                    )
                }
                item {
                    SwitchSettingRow(
                        label = stringResource(CoreUiR.string.settings_sound),
                        isChecked = state.isSoundEnabled,
                        onToggle = { onIntent(SettingsIntent.ToggleSound) },
                        modifier = Modifier.animateItemAppearance(2),
                    )
                }
                // -- Learning Section --
                item {
                    Column(modifier = Modifier.animateItemAppearance(3)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader(title = stringResource(CoreUiR.string.settings_learning))
                    }
                }
                item {
                    DailyGoalSettingRow(
                        currentGoal = state.dailyGoal,
                        onGoalSelected = { onIntent(SettingsIntent.SetDailyGoal(it)) },
                        modifier = Modifier.animateItemAppearance(4),
                    )
                }
                item {
                    AccentModeSettingRow(
                        isStrict = state.isAccentStrict,
                        onToggle = { onIntent(SettingsIntent.ToggleAccentStrict) },
                        modifier = Modifier.animateItemAppearance(5),
                    )
                }

                // -- Parent Zone Section --
                item {
                    Column(modifier = Modifier.animateItemAppearance(6)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader(title = stringResource(CoreUiR.string.settings_parent_zone_section))
                    }
                }
                item {
                    ParentZoneLockRow(
                        isUnlocked = state.showParentZone,
                        onClick = { onIntent(SettingsIntent.OpenParentZone) },
                        modifier = Modifier.animateItemAppearance(7),
                    )
                }
                if (state.showParentZone) {
                    item {
                        NavigationSettingRow(
                            label = stringResource(CoreUiR.string.settings_progress_stats),
                            onClick = { onIntent(SettingsIntent.NavigateToStats) },
                            modifier = Modifier.animateItemAppearance(8),
                        )
                    }
                    item {
                        NavigationSettingRow(
                            label = stringResource(CoreUiR.string.settings_backup_export),
                            onClick = { onIntent(SettingsIntent.NavigateToBackup) },
                            modifier = Modifier.animateItemAppearance(9),
                        )
                    }
                    item {
                        DisabledSettingRow(
                            label = stringResource(CoreUiR.string.settings_cloud_sync),
                            subtitle = stringResource(CoreUiR.string.settings_coming_soon),
                            modifier = Modifier.animateItemAppearance(10),
                        )
                    }
                    item {
                        ResetSettingRow(
                            onClick = { onIntent(SettingsIntent.RequestReset) },
                            modifier = Modifier.animateItemAppearance(11),
                        )
                    }
                }

                // Bottom spacer for scroll padding
                item { Spacer(modifier = Modifier.animateItemAppearance(12).height(24.dp)) }
            }
        }
    }

    // -- Dialogs --
    if (state.showPinDialog) {
        PinEntryDialog(
            isNewPin = !state.parentPinSet,
            error = state.pinErrorResId?.let { stringResource(it) },
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
            text = stringResource(CoreUiR.string.settings_language),
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
            text = stringResource(CoreUiR.string.settings_daily_goal),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(CoreUiR.string.settings_activities, currentGoal),
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
                text = stringResource(CoreUiR.string.settings_accent_mode),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = if (isStrict) {
                    stringResource(CoreUiR.string.settings_strict)
                } else {
                    stringResource(CoreUiR.string.settings_lenient)
                },
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
            contentDescription = if (isUnlocked) {
                stringResource(CoreUiR.string.settings_parent_unlocked_desc)
            } else {
                stringResource(CoreUiR.string.settings_parent_locked_desc)
            },
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (isUnlocked) {
                stringResource(CoreUiR.string.settings_parent_unlocked)
            } else {
                stringResource(CoreUiR.string.settings_unlock_parent)
            },
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
            text = stringResource(CoreUiR.string.settings_reset_all),
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
        title = { Text(stringResource(CoreUiR.string.settings_select_language)) },
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
                Text(stringResource(CoreUiR.string.cancel))
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
        title = { Text(stringResource(CoreUiR.string.settings_set_daily_goal)) },
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
                            text = stringResource(CoreUiR.string.settings_activities_per_day, goal),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(CoreUiR.string.cancel))
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
                text = if (isNewPin) {
                    stringResource(CoreUiR.string.settings_create_pin)
                } else {
                    stringResource(CoreUiR.string.settings_enter_pin_title)
                },
            )
        },
        text = {
            Column {
                Text(
                    text = if (isNewPin) {
                        stringResource(CoreUiR.string.settings_set_pin_desc)
                    } else {
                        stringResource(CoreUiR.string.settings_enter_pin_desc)
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
                    label = { Text(stringResource(CoreUiR.string.settings_pin_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (pin.length == SettingsViewModel.PIN_LENGTH) {
                                onSubmit(pin)
                            }
                        },
                    ),
                    isError = error != null,
                    supportingText = error?.let { { Text(text = it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            StudyBuddyButton(
                text = if (isNewPin) {
                    stringResource(CoreUiR.string.settings_set_pin_btn)
                } else {
                    stringResource(CoreUiR.string.settings_unlock_btn)
                },
                onClick = { onSubmit(pin) },
                enabled = pin.length == SettingsViewModel.PIN_LENGTH,
            )
        },
        dismissButton = {
            StudyBuddyOutlinedButton(
                text = stringResource(CoreUiR.string.cancel),
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
                text = stringResource(CoreUiR.string.settings_reset_title),
                color = MaterialTheme.colorScheme.error,
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(CoreUiR.string.settings_reset_warning),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(CoreUiR.string.settings_type_reset),
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
                text = stringResource(CoreUiR.string.settings_reset_everything),
                onClick = { onConfirm(confirmText) },
                enabled = confirmText == SettingsViewModel.RESET_CONFIRMATION_TEXT,
            )
        },
        dismissButton = {
            StudyBuddyOutlinedButton(
                text = stringResource(CoreUiR.string.cancel),
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
