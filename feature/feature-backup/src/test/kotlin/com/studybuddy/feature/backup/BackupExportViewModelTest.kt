package com.studybuddy.feature.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.work.WorkManager
import app.cash.turbine.test
import com.studybuddy.core.domain.usecase.backup.CreateBackupUseCase
import com.studybuddy.core.domain.usecase.backup.ExportProgressReportUseCase
import com.studybuddy.core.domain.usecase.backup.RestoreBackupUseCase
import com.studybuddy.core.domain.usecase.dictee.ImportWordListUseCase
import com.studybuddy.core.ui.R as CoreUiR
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackupExportViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val context: Context = mockk(relaxed = true)
    private val contentResolver: ContentResolver = mockk(relaxed = true)
    private val createBackupUseCase: CreateBackupUseCase = mockk()
    private val restoreBackupUseCase: RestoreBackupUseCase = mockk()
    private val exportProgressReportUseCase: ExportProgressReportUseCase = mockk()
    private val importWordListUseCase: ImportWordListUseCase = mockk()
    private val workManager: WorkManager = mockk(relaxed = true)
    private val mockUri: Uri = mockk()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.contentResolver } returns contentResolver
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = BackupExportViewModel(
        context = context,
        createBackupUseCase = createBackupUseCase,
        restoreBackupUseCase = restoreBackupUseCase,
        exportProgressReportUseCase = exportProgressReportUseCase,
        importWordListUseCase = importWordListUseCase,
        workManager = workManager,
    ).also {
        it.ioDispatcher = testDispatcher
    }

    @Test
    fun `initial state has expected defaults`() {
        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertNull(state.lastBackupDate)
        assertFalse(state.isBackingUp)
        assertFalse(state.isRestoring)
        assertFalse(state.isExporting)
        assertFalse(state.showRestoreConfirmDialog)
        assertEquals(ExportFormat.PDF, state.exportFormat)
        assertNull(state.statusMessageResId)
        assertNull(state.errorResId)
    }

    @Test
    fun `create backup emits export picker effect`() = runTest {
        coEvery { createBackupUseCase() } returns "{\"version\":1}"
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(BackupExportIntent.CreateBackup)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isBackingUp)

            val effect = awaitItem()
            assertTrue(effect is BackupExportEffect.LaunchExportPicker)
            val picker = effect as BackupExportEffect.LaunchExportPicker
            assertEquals("application/json", picker.mimeType)
            assertTrue(picker.suggestedFileName.startsWith("studybuddy-backup-"))
            assertTrue(picker.suggestedFileName.endsWith(".json"))
        }
    }

    @Test
    fun `export location chosen writes data and sets success`() = runTest {
        coEvery { createBackupUseCase() } returns "{\"version\":1}"
        val outputStream = ByteArrayOutputStream()
        every { contentResolver.openOutputStream(mockUri) } returns outputStream
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(BackupExportIntent.CreateBackup)
            advanceUntilIdle()
            awaitItem() // consume LaunchExportPicker

            viewModel.onIntent(BackupExportIntent.ExportLocationChosen(mockUri))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertNotNull(state.lastBackupDate)
            assertEquals(CoreUiR.string.backup_created_success, state.statusMessageResId)
            assertEquals("{\"version\":1}", outputStream.toString(Charsets.UTF_8.name()))
        }
    }

    @Test
    fun `export location chosen with null uri clears pending state`() = runTest {
        coEvery { createBackupUseCase() } returns "{\"version\":1}"
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(BackupExportIntent.CreateBackup)
            advanceUntilIdle()
            awaitItem() // consume LaunchExportPicker

            viewModel.onIntent(BackupExportIntent.ExportLocationChosen(null))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertNull(state.statusMessageResId)
            assertNull(state.errorResId)
        }
    }

    @Test
    fun `create backup sets error on failure`() = runTest {
        coEvery { createBackupUseCase() } throws RuntimeException("disk full")
        val viewModel = createViewModel()

        viewModel.onIntent(BackupExportIntent.CreateBackup)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isBackingUp)
        assertEquals(CoreUiR.string.backup_failed_generic, state.errorResId)
    }

    @Test
    fun `start restore shows confirmation dialog`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(BackupExportIntent.StartRestore("{\"version\":1}"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.showRestoreConfirmDialog)
        assertEquals("{\"version\":1}", state.pendingRestoreJson)
    }

    @Test
    fun `dismiss restore dialog clears state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(BackupExportIntent.StartRestore("{\"version\":1}"))
        advanceUntilIdle()

        viewModel.onIntent(BackupExportIntent.DismissRestoreDialog)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.showRestoreConfirmDialog)
        assertNull(state.pendingRestoreJson)
    }

    @Test
    fun `confirm restore calls use case and sets success`() = runTest {
        coEvery { restoreBackupUseCase(any()) } returns Unit
        val viewModel = createViewModel()

        viewModel.onIntent(BackupExportIntent.StartRestore("{\"version\":1}"))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(BackupExportIntent.ConfirmRestore)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isRestoring)
            assertFalse(state.showRestoreConfirmDialog)
            assertEquals(CoreUiR.string.backup_restored_success, state.statusMessageResId)

            val effect = awaitItem()
            assertTrue(effect is BackupExportEffect.ShowToast)
        }

        coVerify { restoreBackupUseCase("{\"version\":1}") }
    }

    @Test
    fun `confirm restore sets error on failure`() = runTest {
        coEvery { restoreBackupUseCase(any()) } throws RuntimeException("corrupt data")
        val viewModel = createViewModel()

        viewModel.onIntent(BackupExportIntent.StartRestore("{\"data\":\"bad\"}"))
        advanceUntilIdle()

        viewModel.onIntent(BackupExportIntent.ConfirmRestore)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isRestoring)
        assertEquals(CoreUiR.string.backup_restore_failed, state.errorResId)
    }

    @Test
    fun `export pdf emits export picker effect`() = runTest {
        coEvery { exportProgressReportUseCase.exportPdf(any()) } returns byteArrayOf()
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(BackupExportIntent.ExportPdf)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isExporting)

            val effect = awaitItem()
            assertTrue(effect is BackupExportEffect.LaunchExportPicker)
            val picker = effect as BackupExportEffect.LaunchExportPicker
            assertEquals("application/pdf", picker.mimeType)
            assertTrue(picker.suggestedFileName.endsWith(".pdf"))
        }
    }

    @Test
    fun `export json emits export picker effect`() = runTest {
        coEvery { createBackupUseCase() } returns "{\"version\":1}"
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(BackupExportIntent.ExportJson)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isExporting)

            val effect = awaitItem()
            assertTrue(effect is BackupExportEffect.LaunchExportPicker)
            val picker = effect as BackupExportEffect.LaunchExportPicker
            assertEquals("application/json", picker.mimeType)
            assertTrue(picker.suggestedFileName.endsWith(".json"))
        }
    }

    @Test
    fun `export csv emits export picker effect`() = runTest {
        coEvery { exportProgressReportUseCase.exportCsv(any()) } returns "word,mastered\nhello,true"
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(BackupExportIntent.ExportCsv)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isExporting)

            val effect = awaitItem()
            assertTrue(effect is BackupExportEffect.LaunchExportPicker)
            val picker = effect as BackupExportEffect.LaunchExportPicker
            assertEquals("text/csv", picker.mimeType)
            assertTrue(picker.suggestedFileName.endsWith(".csv"))
        }
    }

    @Test
    fun `dismiss status clears messages`() = runTest {
        coEvery { createBackupUseCase() } returns "{}"
        val viewModel = createViewModel()

        viewModel.onIntent(BackupExportIntent.CreateBackup)
        advanceUntilIdle()

        viewModel.onIntent(BackupExportIntent.DismissStatus)
        advanceUntilIdle()

        assertNull(viewModel.state.value.statusMessageResId)
        assertNull(viewModel.state.value.errorResId)
    }

    @Test
    fun `set auto backup enabled updates state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(BackupExportIntent.SetAutoBackupEnabled(true))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.autoBackupEnabled)
    }

    @Test
    fun `set auto backup frequency updates state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(BackupExportIntent.SetAutoBackupFrequency(AutoBackupFrequency.DAILY))
        advanceUntilIdle()

        assertEquals(AutoBackupFrequency.DAILY, viewModel.state.value.autoBackupFrequency)
    }
}
