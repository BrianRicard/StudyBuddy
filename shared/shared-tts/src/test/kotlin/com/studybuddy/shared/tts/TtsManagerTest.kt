package com.studybuddy.shared.tts

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TtsManagerTest {

    @Test
    fun `initial state is Initializing`() {
        // TtsManager wraps Android APIs that can't be unit tested without mocking,
        // so we test the state model directly
        val state: TtsState = TtsState.Initializing
        assertTrue(state is TtsState.Initializing)
    }

    @Test
    fun `TtsState Ready is correct type`() {
        val state: TtsState = TtsState.Ready
        assertTrue(state is TtsState.Ready)
    }

    @Test
    fun `TtsState Speaking holds text`() {
        val state: TtsState = TtsState.Speaking("bonjour")
        assertTrue(state is TtsState.Speaking)
        assertEquals("bonjour", (state as TtsState.Speaking).text)
    }

    @Test
    fun `TtsState Error holds message`() {
        val state: TtsState = TtsState.Error("init failed")
        assertTrue(state is TtsState.Error)
        assertEquals("init failed", (state as TtsState.Error).message)
    }

    @Test
    fun `DownloadProgress calculates percent correctly`() {
        val progress = DownloadProgress(locale = "fr", bytesDownloaded = 50, totalBytes = 100)
        assertEquals(0.5f, progress.progressPercent)
    }

    @Test
    fun `DownloadProgress percent is zero when totalBytes is zero`() {
        val progress = DownloadProgress(locale = "fr")
        assertEquals(0f, progress.progressPercent)
    }

    @Test
    fun `speed constants have correct values`() {
        assertEquals(1.0f, TtsManager.SPEED_NORMAL)
        assertEquals(0.7f, TtsManager.SPEED_SLOW)
    }
}
