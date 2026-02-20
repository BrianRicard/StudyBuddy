package com.studybuddy.core.common.extensions

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Returns today's date in the system default timezone.
 */
fun Clock.today(): LocalDate =
    now().toLocalDateTime(TimeZone.currentSystemDefault()).date

/**
 * Returns true if this Instant is from the same day as today.
 */
fun Instant.isToday(): Boolean {
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    val thisDate = toLocalDateTime(tz).date
    return thisDate == today
}
