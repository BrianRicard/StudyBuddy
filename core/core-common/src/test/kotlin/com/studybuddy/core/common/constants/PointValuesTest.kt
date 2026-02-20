package com.studybuddy.core.common.constants

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PointValuesTest {

    @Test
    fun `streak multiplier for 0 streak is 1_0`() {
        assertEquals(1.0, PointValues.streakMultiplier(0))
    }

    @Test
    fun `streak multiplier for streak 1-4 is 1_0`() {
        assertEquals(1.0, PointValues.streakMultiplier(1))
        assertEquals(1.0, PointValues.streakMultiplier(4))
    }

    @Test
    fun `streak multiplier for streak 5-9 is 1_5`() {
        assertEquals(1.5, PointValues.streakMultiplier(5))
        assertEquals(1.5, PointValues.streakMultiplier(9))
    }

    @Test
    fun `streak multiplier for streak 10-19 is 2_0`() {
        assertEquals(2.0, PointValues.streakMultiplier(10))
        assertEquals(2.0, PointValues.streakMultiplier(19))
    }

    @Test
    fun `streak multiplier for streak 20+ is 3_0`() {
        assertEquals(3.0, PointValues.streakMultiplier(20))
        assertEquals(3.0, PointValues.streakMultiplier(100))
    }

    @Test
    fun `point values are correct`() {
        assertEquals(10, PointValues.DICTEE_CORRECT_TYPED)
        assertEquals(15, PointValues.DICTEE_CORRECT_HANDWRITTEN)
        assertEquals(50, PointValues.DICTEE_PERFECT_LIST)
        assertEquals(5, PointValues.MATH_CORRECT)
        assertEquals(25, PointValues.MATH_STREAK_5)
        assertEquals(75, PointValues.MATH_STREAK_10)
        assertEquals(150, PointValues.MATH_STREAK_20)
        assertEquals(10, PointValues.DAILY_LOGIN)
        assertEquals(20, PointValues.FIRST_SESSION_OF_DAY)
        assertEquals(100, PointValues.DAILY_CHALLENGE_COMPLETE)
        assertEquals(200, PointValues.WEEKLY_CHALLENGE_COMPLETE)
    }
}
