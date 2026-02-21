package com.studybuddy.feature.backup

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.ui.components.StudyBuddyButton
import com.studybuddy.core.ui.components.StudyBuddyCard
import com.studybuddy.core.ui.components.StudyBuddyOutlinedButton
import com.studybuddy.core.ui.theme.StudyBuddyTheme

@Composable
fun BackupExportScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: BackupExportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BackupExportEffect.ShareFile -> {
                    // TODO: Open Android share intent with uri and mimeType
                }
                is BackupExportEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is BackupExportEffect.FileCreated -> {
                    // TODO: Notify user about created file at path
                }
            }
        }
    }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onIntent(BackupExportIntent.DismissStatus)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onIntent(BackupExportIntent.DismissStatus)
        }
    }

    BackupExportContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupExportContent(
    state: BackupExportState,
    onIntent: (BackupExportIntent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Backup & Export") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isError = state.error != null
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.inverseSurface
                    },
                    contentColor = if (isError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.inverseOnSurface
                    },
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // region Backup Section
            item {
                SectionHeader(title = "Backup")
            }

            item {
                BackupStatusCard(
                    lastBackupDate = state.lastBackupDate,
                    isBackingUp = state.isBackingUp,
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StudyBuddyButton(
                        text = if (state.isBackingUp) "Backing up..." else "Backup Now",
                        onClick = { onIntent(BackupExportIntent.CreateBackup) },
                        enabled = !state.isBackingUp && !state.isRestoring,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    StudyBuddyOutlinedButton(
                        text = if (state.isRestoring) {
                            "Restoring..."
                        } else {
                            "Restore from Backup"
                        },
                        onClick = {
                            // TODO: Open file picker to select backup JSON
                            // For now, pass empty JSON to trigger the confirm dialog
                            onIntent(BackupExportIntent.StartRestore(""))
                        },
                        enabled = !state.isBackingUp && !state.isRestoring,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            // endregion

            // region Export Section
            item {
                SectionHeader(title = "Export")
            }

            item {
                ExportOptionCard(
                    title = "PDF Progress Report",
                    description = "A printable report with charts showing " +
                        "your learning progress across all activities.",
                    buttonText = "Export PDF",
                    isExporting = state.isExporting &&
                        state.exportFormat == ExportFormat.PDF,
                    enabled = !state.isExporting,
                    onClick = { onIntent(BackupExportIntent.ExportPdf) },
                )
            }

            item {
                ExportOptionCard(
                    title = "Raw Data (JSON)",
                    description = "Full data export for backup or transfer " +
                        "to another device. Includes all profiles and settings.",
                    buttonText = "Export JSON",
                    isExporting = state.isExporting &&
                        state.exportFormat == ExportFormat.JSON,
                    enabled = !state.isExporting,
                    onClick = { onIntent(BackupExportIntent.ExportJson) },
                )
            }

            item {
                ExportOptionCard(
                    title = "Word Lists (CSV)",
                    description = "Export your dictee word lists as a " +
                        "spreadsheet-compatible file with attempt statistics.",
                    buttonText = "Export CSV",
                    isExporting = state.isExporting &&
                        state.exportFormat == ExportFormat.CSV,
                    enabled = !state.isExporting,
                    onClick = { onIntent(BackupExportIntent.ExportCsv) },
                )
            }
            // endregion

            // region Auto-Backup Section
            item {
                SectionHeader(title = "Auto-Backup")
            }

            item {
                AutoBackupCard(
                    enabled = state.autoBackupEnabled,
                    frequency = state.autoBackupFrequency,
                    onEnabledChanged = { enabled ->
                        onIntent(BackupExportIntent.SetAutoBackupEnabled(enabled))
                    },
                    onFrequencyChanged = { frequency ->
                        onIntent(BackupExportIntent.SetAutoBackupFrequency(frequency))
                    },
                )
            }
            // endregion

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (state.showRestoreConfirmDialog) {
        RestoreConfirmDialog(
            onConfirm = { onIntent(BackupExportIntent.ConfirmRestore) },
            onDismiss = { onIntent(BackupExportIntent.DismissRestoreDialog) },
        )
    }
}

// region Section Components

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 8.dp),
    )
}

@Composable
private fun BackupStatusCard(
    lastBackupDate: String?,
    isBackingUp: Boolean,
    modifier: Modifier = Modifier,
) {
    StudyBuddyCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isBackingUp) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = if (lastBackupDate != null) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = if (lastBackupDate != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBackingUp) {
                        "Creating backup..."
                    } else {
                        "Last Backup"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        isBackingUp -> "Please wait"
                        lastBackupDate != null -> lastBackupDate
                        else -> "No backups yet"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ExportOptionCard(
    title: String,
    description: String,
    buttonText: String,
    isExporting: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StudyBuddyCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedVisibility(visible = isExporting) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                StudyBuddyOutlinedButton(
                    text = if (isExporting) "Exporting..." else buttonText,
                    onClick = onClick,
                    enabled = enabled && !isExporting,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoBackupCard(
    enabled: Boolean,
    frequency: AutoBackupFrequency,
    onEnabledChanged: (Boolean) -> Unit,
    onFrequencyChanged: (AutoBackupFrequency) -> Unit,
    modifier: Modifier = Modifier,
) {
    StudyBuddyCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Auto-Backup",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Automatically back up your data",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChanged,
                )
            }

            AnimatedVisibility(visible = enabled) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Frequency",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        AutoBackupFrequency.entries.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = frequency == option,
                                onClick = { onFrequencyChanged(option) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = AutoBackupFrequency.entries.size,
                                ),
                            ) {
                                Text(
                                    text = when (option) {
                                        AutoBackupFrequency.DAILY -> "Daily"
                                        AutoBackupFrequency.WEEKLY -> "Weekly"
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// endregion

// region Restore Confirm Dialog

@Composable
private fun RestoreConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp),
            )
        },
        title = {
            Text(text = "Restore from Backup?")
        },
        text = {
            Text(
                text = "This will replace all your current data with " +
                    "the backup data. This action cannot be undone. " +
                    "Make sure you have a recent backup of your " +
                    "current data before proceeding.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            StudyBuddyButton(
                text = "Restore",
                onClick = onConfirm,
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

// region Previews

@Preview(showBackground = true)
@Composable
private fun BackupExportScreenPreview() {
    StudyBuddyTheme {
        BackupExportContent(
            state = BackupExportState(
                lastBackupDate = "Feb 20, 2026 at 3:45 PM",
            ),
            onIntent = {},
            onNavigateBack = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BackupExportScreenNoBackupPreview() {
    StudyBuddyTheme {
        BackupExportContent(
            state = BackupExportState(),
            onIntent = {},
            onNavigateBack = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BackupExportScreenBackingUpPreview() {
    StudyBuddyTheme {
        BackupExportContent(
            state = BackupExportState(
                isBackingUp = true,
                lastBackupDate = "Feb 19, 2026 at 10:00 AM",
            ),
            onIntent = {},
            onNavigateBack = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BackupExportScreenExportingPreview() {
    StudyBuddyTheme {
        BackupExportContent(
            state = BackupExportState(
                isExporting = true,
                exportFormat = ExportFormat.PDF,
                lastBackupDate = "Feb 20, 2026 at 3:45 PM",
            ),
            onIntent = {},
            onNavigateBack = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BackupExportScreenAutoBackupEnabledPreview() {
    StudyBuddyTheme {
        BackupExportContent(
            state = BackupExportState(
                lastBackupDate = "Feb 20, 2026 at 3:45 PM",
                autoBackupEnabled = true,
                autoBackupFrequency = AutoBackupFrequency.DAILY,
            ),
            onIntent = {},
            onNavigateBack = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RestoreConfirmDialogPreview() {
    StudyBuddyTheme {
        RestoreConfirmDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}

// endregion
