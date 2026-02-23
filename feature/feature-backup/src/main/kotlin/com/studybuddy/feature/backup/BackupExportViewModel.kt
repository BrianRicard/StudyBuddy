package com.studybuddy.feature.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.usecase.backup.CreateBackupUseCase
import com.studybuddy.core.domain.usecase.backup.ExportProgressReportUseCase
import com.studybuddy.core.domain.usecase.backup.RestoreBackupUseCase
import com.studybuddy.core.domain.usecase.dictee.ImportWordListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Available export formats for user data.
 */
enum class ExportFormat {
    PDF,
    JSON,
    CSV,
}

/**
 * Frequency options for automatic backups.
 */
enum class AutoBackupFrequency {
    DAILY,
    WEEKLY,
}

/**
 * UI state for the Backup & Export screen.
 */
data class BackupExportState(
    val lastBackupDate: String? = null,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val showRestoreConfirmDialog: Boolean = false,
    val pendingRestoreJson: String? = null,
    val exportFormat: ExportFormat = ExportFormat.PDF,
    val statusMessage: String? = null,
    val error: String? = null,
    val autoBackupEnabled: Boolean = false,
    val autoBackupFrequency: AutoBackupFrequency = AutoBackupFrequency.WEEKLY,
)

/**
 * User actions dispatched to the Backup & Export ViewModel.
 */
sealed interface BackupExportIntent {
    data object CreateBackup : BackupExportIntent
    data class StartRestore(val json: String) : BackupExportIntent
    data object ConfirmRestore : BackupExportIntent
    data object DismissRestoreDialog : BackupExportIntent
    data object ExportPdf : BackupExportIntent
    data object ExportJson : BackupExportIntent
    data object ExportCsv : BackupExportIntent
    data class ImportCsv(val csvContent: String) : BackupExportIntent
    data object DismissStatus : BackupExportIntent
    data class SetAutoBackupEnabled(val enabled: Boolean) : BackupExportIntent
    data class SetAutoBackupFrequency(val frequency: AutoBackupFrequency) : BackupExportIntent
}

/**
 * One-shot side effects emitted by the Backup & Export ViewModel.
 */
sealed interface BackupExportEffect {
    data class ShareFile(val uri: Uri, val mimeType: String) : BackupExportEffect
    data class ShowToast(val message: String) : BackupExportEffect
    data class FileCreated(val path: String) : BackupExportEffect
}

@HiltViewModel
class BackupExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val createBackupUseCase: CreateBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val exportProgressReportUseCase: ExportProgressReportUseCase,
    private val importWordListUseCase: ImportWordListUseCase,
    private val workManager: WorkManager,
) : ViewModel() {

    private val _state = MutableStateFlow(BackupExportState())
    val state: StateFlow<BackupExportState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<BackupExportEffect>()
    val effects: SharedFlow<BackupExportEffect> = _effects.asSharedFlow()

    private val profileId = AppConstants.DEFAULT_PROFILE_ID

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")

    fun onIntent(intent: BackupExportIntent) {
        when (intent) {
            is BackupExportIntent.CreateBackup -> handleCreateBackup()
            is BackupExportIntent.StartRestore -> handleStartRestore(intent.json)
            is BackupExportIntent.ConfirmRestore -> handleConfirmRestore()
            is BackupExportIntent.DismissRestoreDialog -> handleDismissRestoreDialog()
            is BackupExportIntent.ExportPdf -> handleExportPdf()
            is BackupExportIntent.ExportJson -> handleExportJson()
            is BackupExportIntent.ExportCsv -> handleExportCsv()
            is BackupExportIntent.ImportCsv -> handleImportCsv(intent.csvContent)
            is BackupExportIntent.DismissStatus -> handleDismissStatus()
            is BackupExportIntent.SetAutoBackupEnabled -> {
                handleSetAutoBackupEnabled(intent.enabled)
            }
            is BackupExportIntent.SetAutoBackupFrequency -> {
                handleSetAutoBackupFrequency(intent.frequency)
            }
        }
    }

    private fun handleCreateBackup() {
        if (_state.value.isBackingUp) return

        viewModelScope.launch {
            _state.update { it.copy(isBackingUp = true, error = null) }

            try {
                val backupJson = createBackupUseCase()
                val file = writeExportFile(
                    "studybuddy_backup.json",
                    backupJson.toByteArray(Charsets.UTF_8),
                )
                val uri = getFileProviderUri(file)
                val now = LocalDateTime.now().format(dateTimeFormatter)
                _state.update {
                    it.copy(
                        isBackingUp = false,
                        lastBackupDate = now,
                        statusMessage = "Backup created successfully",
                    )
                }
                _effects.emit(
                    BackupExportEffect.ShareFile(
                        uri = uri,
                        mimeType = "application/json",
                    ),
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isBackingUp = false,
                        error = "Backup failed: ${e.message ?: "Unknown error"}",
                    )
                }
            }
        }
    }

    private fun handleStartRestore(json: String) {
        _state.update {
            it.copy(
                showRestoreConfirmDialog = true,
                pendingRestoreJson = json,
                error = null,
            )
        }
    }

    private fun handleConfirmRestore() {
        val json = _state.value.pendingRestoreJson ?: return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    showRestoreConfirmDialog = false,
                    isRestoring = true,
                    error = null,
                )
            }

            try {
                restoreBackupUseCase(json)
                _state.update {
                    it.copy(
                        isRestoring = false,
                        pendingRestoreJson = null,
                        statusMessage = "Data restored successfully",
                    )
                }
                _effects.emit(
                    BackupExportEffect.ShowToast("All data has been restored"),
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isRestoring = false,
                        pendingRestoreJson = null,
                        error = "Restore failed: ${e.message ?: "Unknown error"}",
                    )
                }
            }
        }
    }

    private fun handleDismissRestoreDialog() {
        _state.update {
            it.copy(
                showRestoreConfirmDialog = false,
                pendingRestoreJson = null,
            )
        }
    }

    private fun handleExportPdf() {
        if (_state.value.isExporting) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isExporting = true,
                    exportFormat = ExportFormat.PDF,
                    error = null,
                )
            }

            try {
                val pdfBytes = exportProgressReportUseCase.exportPdf(profileId)
                val file = writeExportFile("studybuddy_progress_report.pdf", pdfBytes)
                val uri = getFileProviderUri(file)
                _state.update {
                    it.copy(
                        isExporting = false,
                        statusMessage = "PDF report generated",
                    )
                }
                _effects.emit(
                    BackupExportEffect.ShareFile(
                        uri = uri,
                        mimeType = "application/pdf",
                    ),
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isExporting = false,
                        error = "PDF export failed: ${e.message ?: "Unknown error"}",
                    )
                }
            }
        }
    }

    private fun handleExportJson() {
        if (_state.value.isExporting) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isExporting = true,
                    exportFormat = ExportFormat.JSON,
                    error = null,
                )
            }

            try {
                val json = createBackupUseCase()
                val file = writeExportFile(
                    "studybuddy_backup.json",
                    json.toByteArray(Charsets.UTF_8),
                )
                val uri = getFileProviderUri(file)
                _state.update {
                    it.copy(
                        isExporting = false,
                        statusMessage = "JSON data exported",
                    )
                }
                _effects.emit(
                    BackupExportEffect.ShareFile(
                        uri = uri,
                        mimeType = "application/json",
                    ),
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isExporting = false,
                        error = "JSON export failed: ${e.message ?: "Unknown error"}",
                    )
                }
            }
        }
    }

    private fun handleExportCsv() {
        if (_state.value.isExporting) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isExporting = true,
                    exportFormat = ExportFormat.CSV,
                    error = null,
                )
            }

            try {
                val csv = exportProgressReportUseCase.exportCsv(profileId)
                val file = writeExportFile(
                    "studybuddy_word_lists.csv",
                    csv.toByteArray(Charsets.UTF_8),
                )
                val uri = getFileProviderUri(file)
                _state.update {
                    it.copy(
                        isExporting = false,
                        statusMessage = "CSV word lists exported",
                    )
                }
                _effects.emit(
                    BackupExportEffect.ShareFile(
                        uri = uri,
                        mimeType = "text/csv",
                    ),
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isExporting = false,
                        error = "CSV export failed: ${e.message ?: "Unknown error"}",
                    )
                }
            }
        }
    }

    private fun handleImportCsv(csvContent: String) {
        if (_state.value.isImporting) return

        viewModelScope.launch {
            _state.update { it.copy(isImporting = true, error = null) }

            try {
                val count = importWordListUseCase(csvContent, profileId)
                _state.update {
                    it.copy(
                        isImporting = false,
                        statusMessage = "Imported $count words",
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isImporting = false,
                        error = "Import failed: ${e.message ?: "Unknown error"}",
                    )
                }
            }
        }
    }

    private fun handleDismissStatus() {
        _state.update { it.copy(statusMessage = null, error = null) }
    }

    private fun handleSetAutoBackupEnabled(enabled: Boolean) {
        _state.update { it.copy(autoBackupEnabled = enabled) }
        if (enabled) {
            scheduleAutoBackup(_state.value.autoBackupFrequency)
        } else {
            workManager.cancelUniqueWork(AUTO_BACKUP_WORK_NAME)
        }
    }

    private fun handleSetAutoBackupFrequency(frequency: AutoBackupFrequency) {
        _state.update { it.copy(autoBackupFrequency = frequency) }
        if (_state.value.autoBackupEnabled) {
            scheduleAutoBackup(frequency)
        }
    }

    private fun scheduleAutoBackup(frequency: AutoBackupFrequency) {
        val intervalHours = when (frequency) {
            AutoBackupFrequency.DAILY -> DAILY_BACKUP_INTERVAL_HOURS
            AutoBackupFrequency.WEEKLY -> WEEKLY_BACKUP_INTERVAL_HOURS
        }

        val workRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
            intervalHours,
            TimeUnit.HOURS,
        ).build()

        workManager.enqueueUniquePeriodicWork(
            AUTO_BACKUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest,
        )
    }

    private fun writeExportFile(
        filename: String,
        data: ByteArray,
    ): File {
        val exportsDir = File(context.cacheDir, "exports")
        exportsDir.mkdirs()
        val file = File(exportsDir, filename)
        file.writeBytes(data)
        return file
    }

    private fun getFileProviderUri(file: File): Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )

    companion object {
        private const val AUTO_BACKUP_WORK_NAME = "studybuddy_auto_backup"
        private const val DAILY_BACKUP_INTERVAL_HOURS = 24L
        private const val WEEKLY_BACKUP_INTERVAL_HOURS = 168L
    }
}
