package com.studybuddy.feature.backup

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
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
import com.studybuddy.core.ui.R as CoreUiR
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    @StringRes val statusMessageResId: Int? = null,
    val statusMessageArgs: Array<Any> = emptyArray(),
    @StringRes val errorResId: Int? = null,
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
    data class ExportLocationChosen(val uri: Uri?) : BackupExportIntent
    data class ImportCsv(val csvContent: String) : BackupExportIntent
    data object DismissStatus : BackupExportIntent
    data class SetAutoBackupEnabled(val enabled: Boolean) : BackupExportIntent
    data class SetAutoBackupFrequency(val frequency: AutoBackupFrequency) : BackupExportIntent
}

/**
 * One-shot side effects emitted by the Backup & Export ViewModel.
 */
sealed interface BackupExportEffect {
    data class LaunchExportPicker(
        val suggestedFileName: String,
        val mimeType: String,
    ) : BackupExportEffect
    data class ShowToast(@StringRes val messageResId: Int) : BackupExportEffect
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
    private val fileTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")

    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private var pendingExportData: ByteArray? = null

    fun onIntent(intent: BackupExportIntent) {
        when (intent) {
            is BackupExportIntent.CreateBackup -> handleCreateBackup()
            is BackupExportIntent.StartRestore -> handleStartRestore(intent.json)
            is BackupExportIntent.ConfirmRestore -> handleConfirmRestore()
            is BackupExportIntent.DismissRestoreDialog -> handleDismissRestoreDialog()
            is BackupExportIntent.ExportPdf -> handleExportPdf()
            is BackupExportIntent.ExportJson -> handleExportJson()
            is BackupExportIntent.ExportCsv -> handleExportCsv()
            is BackupExportIntent.ExportLocationChosen -> {
                handleExportLocationChosen(intent.uri)
            }
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
            _state.update { it.copy(isBackingUp = true, errorResId = null) }

            try {
                val backupJson = createBackupUseCase()
                pendingExportData = backupJson.toByteArray(Charsets.UTF_8)
                val timestamp = LocalDateTime.now().format(fileTimestampFormatter)
                _state.update {
                    it.copy(
                        isBackingUp = false,
                        exportFormat = ExportFormat.JSON,
                    )
                }
                _effects.emit(
                    BackupExportEffect.LaunchExportPicker(
                        suggestedFileName = "studybuddy-backup-$timestamp.json",
                        mimeType = "application/json",
                    ),
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isBackingUp = false,
                        errorResId = CoreUiR.string.backup_failed_generic,
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
                errorResId = null,
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
                    errorResId = null,
                )
            }

            try {
                restoreBackupUseCase(json)
                _state.update {
                    it.copy(
                        isRestoring = false,
                        pendingRestoreJson = null,
                        statusMessageResId = CoreUiR.string.backup_restored_success,
                        statusMessageArgs = emptyArray(),
                    )
                }
                _effects.emit(
                    BackupExportEffect.ShowToast(CoreUiR.string.backup_all_restored),
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isRestoring = false,
                        pendingRestoreJson = null,
                        errorResId = CoreUiR.string.backup_restore_failed,
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
                    errorResId = null,
                )
            }

            try {
                val pdfBytes = exportProgressReportUseCase.exportPdf(profileId)
                pendingExportData = pdfBytes
                val timestamp = LocalDateTime.now().format(fileTimestampFormatter)
                _state.update {
                    it.copy(isExporting = false)
                }
                _effects.emit(
                    BackupExportEffect.LaunchExportPicker(
                        suggestedFileName = "studybuddy-progress-$timestamp.pdf",
                        mimeType = "application/pdf",
                    ),
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isExporting = false,
                        errorResId = CoreUiR.string.backup_pdf_failed,
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
                    errorResId = null,
                )
            }

            try {
                val json = createBackupUseCase()
                pendingExportData = json.toByteArray(Charsets.UTF_8)
                val timestamp = LocalDateTime.now().format(fileTimestampFormatter)
                _state.update {
                    it.copy(isExporting = false)
                }
                _effects.emit(
                    BackupExportEffect.LaunchExportPicker(
                        suggestedFileName = "studybuddy-backup-$timestamp.json",
                        mimeType = "application/json",
                    ),
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isExporting = false,
                        errorResId = CoreUiR.string.backup_json_failed,
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
                    errorResId = null,
                )
            }

            try {
                val csv = exportProgressReportUseCase.exportCsv(profileId)
                pendingExportData = csv.toByteArray(Charsets.UTF_8)
                val timestamp = LocalDateTime.now().format(fileTimestampFormatter)
                _state.update {
                    it.copy(isExporting = false)
                }
                _effects.emit(
                    BackupExportEffect.LaunchExportPicker(
                        suggestedFileName = "studybuddy-wordlists-$timestamp.csv",
                        mimeType = "text/csv",
                    ),
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isExporting = false,
                        errorResId = CoreUiR.string.backup_csv_failed,
                    )
                }
            }
        }
    }

    private fun handleExportLocationChosen(uri: Uri?) {
        if (uri == null) {
            pendingExportData = null
            return
        }

        val data = pendingExportData ?: return
        val isBackup = _state.value.exportFormat == ExportFormat.JSON

        viewModelScope.launch {
            try {
                writeToUri(uri, data)
                pendingExportData = null

                if (isBackup) {
                    val now = LocalDateTime.now().format(dateTimeFormatter)
                    _state.update {
                        it.copy(
                            lastBackupDate = now,
                            statusMessageResId = CoreUiR.string.backup_created_success,
                            statusMessageArgs = emptyArray(),
                        )
                    }
                } else {
                    val messageResId = when (_state.value.exportFormat) {
                        ExportFormat.PDF -> CoreUiR.string.backup_pdf_generated
                        ExportFormat.JSON -> CoreUiR.string.backup_json_exported
                        ExportFormat.CSV -> CoreUiR.string.backup_csv_exported
                    }
                    _state.update {
                        it.copy(
                            statusMessageResId = messageResId,
                            statusMessageArgs = emptyArray(),
                        )
                    }
                }
            } catch (e: Exception) {
                pendingExportData = null
                _state.update {
                    it.copy(errorResId = CoreUiR.string.backup_failed_generic)
                }
            }
        }
    }

    private fun handleImportCsv(csvContent: String) {
        if (_state.value.isImporting) return

        viewModelScope.launch {
            _state.update { it.copy(isImporting = true, errorResId = null) }

            try {
                val count = importWordListUseCase(csvContent, profileId)
                _state.update {
                    it.copy(
                        isImporting = false,
                        statusMessageResId = CoreUiR.string.backup_imported_words,
                        statusMessageArgs = arrayOf(count),
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isImporting = false,
                        errorResId = CoreUiR.string.backup_import_failed,
                    )
                }
            }
        }
    }

    private fun handleDismissStatus() {
        _state.update {
            it.copy(statusMessageResId = null, statusMessageArgs = emptyArray(), errorResId = null)
        }
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

    private suspend fun writeToUri(
        uri: Uri,
        data: ByteArray,
    ) {
        withContext(ioDispatcher) {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(data)
                outputStream.flush()
            } ?: throw IOException("Could not open output stream for $uri")
        }
    }

    companion object {
        private const val AUTO_BACKUP_WORK_NAME = "studybuddy_auto_backup"
        private const val DAILY_BACKUP_INTERVAL_HOURS = 24L
        private const val WEEKLY_BACKUP_INTERVAL_HOURS = 168L
    }
}
