package com.studybuddy.core.domain.usecase.plan

import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** The local day, and the half-open instant range `[start, end)` that spans it. */
data class LocalDay(
    val date: LocalDate,
    val start: Instant,
    val end: Instant,
)

/**
 * Emits the current local day, then again at each local midnight.
 *
 * A kid's tablet is rarely restarted, so anything that pins "today" at subscription
 * time is stale by the next morning. The time zone is re-read on every tick, so
 * travelling or a DST change is picked up on the following day at the latest.
 *
 * The one-minute floor stops a clock jumping backwards from spinning the loop.
 */
fun dayTicker(clock: Clock): Flow<LocalDay> = flow {
    while (true) {
        val timeZone = TimeZone.currentSystemDefault()
        val now = clock.now()
        val date = now.toLocalDateTime(timeZone).date
        val start = date.atStartOfDayIn(timeZone)
        val end = date.plus(DatePeriod(days = 1)).atStartOfDayIn(timeZone)

        emit(LocalDay(date = date, start = start, end = end))

        delay(maxOf(end - now, MIN_TICK))
    }
}

private val MIN_TICK = 1.minutes
