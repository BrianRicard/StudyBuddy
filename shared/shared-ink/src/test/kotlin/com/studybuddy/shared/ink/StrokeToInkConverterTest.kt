package com.studybuddy.shared.ink

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StrokeToInkConverterTest {

    @Test
    fun `StrokePoint stores coordinates and timestamp`() {
        val point = StrokePoint(10f, 20f, 1000L)
        assertEquals(10f, point.x)
        assertEquals(20f, point.y)
        assertEquals(1000L, point.timestamp)
        assertEquals(1f, point.pressure) // default pressure
    }

    @Test
    fun `StrokePoint stores pressure for stylus input`() {
        val point = StrokePoint(10f, 20f, 1000L, pressure = 0.7f)
        assertEquals(0.7f, point.pressure)
    }

    @Test
    fun `DownloadProgress reports completion`() {
        val progress = DownloadProgress(languageTag = "fr", isComplete = true)
        assertTrue(progress.isComplete)
        assertEquals("fr", progress.languageTag)
    }

    @Test
    fun `DownloadProgress reports error`() {
        val progress = DownloadProgress(languageTag = "en", error = "Network error")
        assertEquals("Network error", progress.error)
    }

    @Test
    fun `DownloadProgress defaults are correct`() {
        val progress = DownloadProgress(languageTag = "de")
        assertEquals(false, progress.isComplete)
        assertEquals(null, progress.error)
    }
}
