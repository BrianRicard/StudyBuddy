package com.studybuddy.shared.points

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

    // --- Regression tests: no double multiplier on math points (bug fix #21) ---

    @Test
    fun `math points with streak 20 and 20 correct returns exact value without double multiplier`() {
        // correctCount=20, streak=20:
        // basePoints = 20 * 5 = 100
        // streakBonus = 25 (streak_5) + 75 (streak_10) + 150 (streak_20) = 250
        // total = 100 + 250 = 350
        // A previous bug applied the streak multiplier ON TOP of the streak bonus,
        // inflating the result. This test ensures no double multiplier.
        val points = PointsCalculator.calculateMathPoints(correctCount = 20, streak = 20)
        assertEquals(
            350,
            points,
            "calculateMathPoints(20, 20) = 20*5 + 25 + 75 + 150 = 350, not an inflated value",
        )
    }

    @Test
    fun `applyMultiplier with streak 0 returns base points unchanged`() {
        // streak 0 means multiplier = 1.0, so base points should not change
        val basePoints = 42
        val result = PointsCalculator.applyMultiplier(basePoints = basePoints, streak = 0)
        assertEquals(
            basePoints,
            result,
            "applyMultiplier with streak=0 must return basePoints unchanged (multiplier 1.0x)",
        )
    }

    @Test
    fun `applyMultiplier with streak 4 returns base points unchanged`() {
        // streak 0-4 all have multiplier 1.0
        val basePoints = 100
        val result = PointsCalculator.applyMultiplier(basePoints = basePoints, streak = 4)
        assertEquals(
            basePoints,
            result,
            "applyMultiplier with streak=4 must return basePoints unchanged (multiplier 1.0x)",
        )
    }

    @Test
    fun `calculateMathPoints does not apply streak multiplier to result`() {
        // streak=10 -> multiplier would be 2.0x if applied
        // basePoints = 10 * 5 = 50
        // streakBonus = 25 + 75 = 100
        // correct total = 50 + 100 = 150
        // wrong total (if multiplied) would be 150 * 2.0 = 300
        val points = PointsCalculator.calculateMathPoints(correctCount = 10, streak = 10)
        assertEquals(150, points, "Math points must be base + bonus, not multiplied")
    }
}
