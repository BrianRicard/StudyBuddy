package com.studybuddy.core.domain.usecase.plan

import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * A kid's tablet is rarely restarted, so "today" cannot be pinned at subscription
 * time — these pin the rollover the Home quest depends on.
 */
class DayTickerTest {

    /** Advances with the test scheduler's virtual time, so a day passes instantly. */
    private class TestClock(private val start: Instant, private val elapsed: () -> Long) : Clock {
        override fun now(): Instant = start + elapsed().milliseconds
        private val Long.milliseconds get() = kotlin.time.Duration.parse("${this}ms")
    }

    @Test
    fun `emits today immediately`() = runTest {
        val clock = TestClock(NOON) { testScheduler.currentTime }

        val first = dayTicker(clock).take(1).toList().single()

        val timeZone = TimeZone.currentSystemDefault()
        assertEquals(NOON.toLocalDateTime(timeZone).date, first.date)
    }

    @Test
    fun `the range brackets the emitted date`() = runTest {
        val clock = TestClock(NOON) { testScheduler.currentTime }

        val day = dayTicker(clock).take(1).toList().single()

        assertTrue(day.start <= NOON, "start ${day.start} should be at or before now")
        assertTrue(day.end > NOON, "end ${day.end} should be after now")
        assertTrue(day.end > day.start)
    }

    @Test
    fun `rolls over to the next day rather than reporting yesterday forever`() = runTest {
        // The bug this guards: capturing the boundary once meant a tablet left on
        // overnight scored the morning's sessions against yesterday's window, where
        // they count as zero, until the process restarted.
        val clock = TestClock(NOON) { testScheduler.currentTime }

        val days = dayTicker(clock).take(2).toList()

        assertEquals(2, days.size)
        assertTrue(days[1].date > days[0].date, "expected a later date, got ${days.map { it.date }}")
        assertEquals(days[0].end, days[1].start, "the second day should start where the first ended")
    }

    @Test
    fun `waits until midnight, not on a busy loop`() = runTest {
        val clock = TestClock(NOON) { testScheduler.currentTime }

        dayTicker(clock).take(2).toList()

        // Midday to midnight; the assertion is that it slept rather than spun.
        assertTrue(
            testScheduler.currentTime >= 11.hours.inWholeMilliseconds,
            "only advanced ${testScheduler.currentTime}ms",
        )
    }

    private companion object {
        /** Midday UTC, far from any boundary the local zone might impose. */
        val NOON: Instant = Instant.parse("2026-08-03T12:00:00Z")
    }
}
