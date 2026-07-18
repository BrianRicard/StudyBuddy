package com.studybuddy.core.domain.model.conjugation

import kotlin.time.Duration.Companion.days
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class AtelierScheduleTest {

    @ParameterizedTest
    @CsvSource(
        "0, 1",
        "1, 2",
        "2, 3",
        "3, 4",
        "4, 4",
    )
    fun `a correct answer promotes one box capped at MAX_BOX`(
        from: Int,
        expected: Int,
    ) {
        assertEquals(expected, AtelierSchedule.answered(box = from, correct = true).box)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0",
        "1, 0",
        "2, 1",
        "3, 2",
        "4, 3",
    )
    fun `a wrong answer demotes a single box floored at zero — never all the way down`(
        from: Int,
        expected: Int,
    ) {
        assertEquals(expected, AtelierSchedule.answered(box = from, correct = false).box)
    }

    @ParameterizedTest
    @CsvSource(
        "0, 1",
        "1, 3",
        "2, 7",
        "3, 15",
        "4, 15",
    )
    fun `a correct answer schedules by the new box interval`(
        from: Int,
        expectedDays: Int,
    ) {
        assertEquals(expectedDays.days, AtelierSchedule.answered(box = from, correct = true).nextDelay)
    }

    @Test
    fun `a wrong answer always comes back tomorrow`() {
        (0..AtelierSchedule.MAX_BOX).forEach { box ->
            assertEquals(1.days, AtelierSchedule.answered(box = box, correct = false).nextDelay)
        }
    }

    @Test
    fun `intervals grow strictly from box 1 up`() {
        AtelierSchedule.BOX_INTERVALS.drop(1).zipWithNext { shorter, longer ->
            assertTrue(shorter < longer, "intervals must grow: $shorter !< $longer")
        }
        assertEquals(AtelierSchedule.MAX_BOX + 1, AtelierSchedule.BOX_INTERVALS.size)
    }

    @Test
    fun `out-of-range boxes are clamped`() {
        assertEquals(AtelierSchedule.MAX_BOX, AtelierSchedule.answered(box = 99, correct = true).box)
        assertEquals(0, AtelierSchedule.answered(box = -1, correct = false).box)
    }
}
