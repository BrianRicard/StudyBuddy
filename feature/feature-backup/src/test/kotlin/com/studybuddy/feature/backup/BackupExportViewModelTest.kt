package com.studybuddy.feature.backup

import androidx.work.WorkManager
import app.cash.turbine.test
import com.studybuddy.core.domain.usecase.backup.CreateBackupUseCase
import com.studybuddy.core.domain.usecase.backup.ExportProgressReportUseCase
import com.studybuddy.core.domain.usecase.backup.RestoreBackupUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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

    private val createBackupUseCase: CreateBackupUseCase = mockk()
    private val restoreBackupUseCase: RestoreBackupUseCase = mockk()
    private val exportProgressReportUseCase: ExportProgressReportUseCase = mockk()
    private val workManager: WorkManager = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = BackupExportViewModel(
        createBackupUseCase = createBackupUseCase,
        restoreBackupUseCase = restoreBackupUseCase,
        exportProgressReportUseCase = exportProgressReportUseCase,
        workManager = workManager,
    )

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
        assertNull(state.statusMessage)
        assertNull(state.error)
    }

    @Test
    fun `create backup sets success state and emits effect`() = runTest {
        coEvery { createBackupUseCase() } returns "{\"version\":1}"
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(BackupExportIntent.CreateBackup)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isBackingUp)
            assertNotNull(state.lastBackupDate)
            assertEquals("Backup created successfully", state.statusMessage)

            val effect = awaitItem()
            assertTrue(effect is BackupExportEffect.FileCreated)
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
        assertEquals("Backup failed: disk full", state.error)
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
            assertEquals("Data restored successfully", state.statusMessage)

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
        assertEquals("Restore failed: corrupt data", state.error)
    }

    @Test
    fun `export pdf calls use case and emits share effect`() = runTest {
        coEvery { exportProgressReportUseCase.exportPdf(any()) } returns byteArrayOf()
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(BackupExportIntent.ExportPdf)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isExporting)
            assertEquals("PDF report generated", state.statusMessage)

            val effect = awaitItem()
            assertTrue(effect is BackupExportEffect.ShareFile)
            assertEquals("application/pdf", (effect as BackupExportEffect.ShareFile).mimeType)
        }
    }

    @Test
    fun `export json calls create backup and emits share effect`() = runTest {
        coEvery { createBackupUseCase() } returns "{\"version\":1}"
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(BackupExportIntent.ExportJson)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isExporting)
            assertEquals("JSON data exported", state.statusMessage)

            val effect = awaitItem()
            assertTrue(effect is BackupExportEffect.ShareFile)
            assertEquals("application/json", (effect as BackupExportEffect.ShareFile).mimeType)
        }
    }

    @Test
    fun `export csv calls use case and emits share effect`() = runTest {
        coEvery { exportProgressReportUseCase.exportCsv(any()) } returns "word,mastered\nhello,true"
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(BackupExportIntent.ExportCsv)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isExporting)
            assertEquals("CSV word lists exported", state.statusMessage)

            val effect = awaitItem()
            assertTrue(effect is BackupExportEffect.ShareFile)
            assertEquals("text/csv", (effect as BackupExportEffect.ShareFile).mimeType)
        }
    }

    @Test
    fun `dismiss status clears messages`() = runTest {
        coEvery { createBackupUseCase() } returns "{}"
        val viewModel = createViewModel()

        viewModel.onIntent(BackupExportIntent.CreateBackup)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.statusMessage)

        viewModel.onIntent(BackupExportIntent.DismissStatus)
        advanceUntilIdle()

        assertNull(viewModel.state.value.statusMessage)
        assertNull(viewModel.state.value.error)
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
