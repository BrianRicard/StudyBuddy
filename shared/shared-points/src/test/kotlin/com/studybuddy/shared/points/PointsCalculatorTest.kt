package com.studybuddy.shared.points

import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.InputMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PointsCalculatorTest {

    @Test
    fun `dictee typed all correct with perfect bonus`() {
        val points = PointsCalculator.calculateDicteePoints(
            totalWords = 8,
            correctWords = 8,
            inputMode = InputMode.KEYBOARD,
        )
        assertEquals(8 * 10 + 50, points) // 80 + 50 = 130
    }

    @Test
    fun `dictee typed partial correct no perfect bonus`() {
        val points = PointsCalculator.calculateDicteePoints(
            totalWords = 8,
            correctWords = 6,
            inputMode = InputMode.KEYBOARD,
        )
        assertEquals(6 * 10, points) // 60, no bonus
    }

    @Test
    fun `dictee handwritten all correct with perfect bonus`() {
        val points = PointsCalculator.calculateDicteePoints(
            totalWords = 5,
            correctWords = 5,
            inputMode = InputMode.HANDWRITING,
        )
        assertEquals(5 * 15 + 50, points) // 75 + 50 = 125
    }

    @Test
    fun `dictee zero correct`() {
        val points = PointsCalculator.calculateDicteePoints(
            totalWords = 5,
            correctWords = 0,
            inputMode = InputMode.KEYBOARD,
        )
        assertEquals(0, points)
    }

    @Test
    fun `math points with no streak bonus`() {
        val points = PointsCalculator.calculateMathPoints(correctCount = 10, streak = 3)
        assertEquals(10 * 5, points) // 50
    }

    @Test
    fun `math points with streak 5 bonus`() {
        val points = PointsCalculator.calculateMathPoints(correctCount = 10, streak = 5)
        assertEquals(50 + 25, points) // 50 + streak_5 bonus
    }

    @Test
    fun `math points with streak 10 bonus`() {
        val points = PointsCalculator.calculateMathPoints(correctCount = 10, streak = 10)
        assertEquals(50 + 25 + 75, points) // 50 + streak_5 + streak_10
    }

    @Test
    fun `math points with streak 20 bonus`() {
        val points = PointsCalculator.calculateMathPoints(correctCount = 10, streak = 20)
        assertEquals(50 + 25 + 75 + 150, points) // 50 + all streak bonuses
    }

    @Test
    fun `apply multiplier at various streak levels`() {
        assertEquals(10, PointsCalculator.applyMultiplier(10, 0))
        assertEquals(10, PointsCalculator.applyMultiplier(10, 4))
        assertEquals(15, PointsCalculator.applyMultiplier(10, 5))
        assertEquals(20, PointsCalculator.applyMultiplier(10, 10))
        assertEquals(30, PointsCalculator.applyMultiplier(10, 20))
    }

    @Test
    fun `streak bonus accumulates correctly`() {
        assertEquals(0, PointsCalculator.calculateStreakBonus(0))
        assertEquals(0, PointsCalculator.calculateStreakBonus(4))
        assertEquals(25, PointsCalculator.calculateStreakBonus(5))
        assertEquals(25 + 75, PointsCalculator.calculateStreakBonus(10))
        assertEquals(25 + 75 + 150, PointsCalculator.calculateStreakBonus(20))
    }
}
