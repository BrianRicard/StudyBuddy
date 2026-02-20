package com.studybuddy.shared.points

import com.studybuddy.core.common.constants.PointValues
import com.studybuddy.core.domain.model.InputMode

object PointsCalculator {

    fun calculateDicteePoints(
        totalWords: Int,
        correctWords: Int,
        inputMode: InputMode,
    ): Int {
        val perWord = when (inputMode) {
            InputMode.KEYBOARD -> PointValues.DICTEE_CORRECT_TYPED
            InputMode.HANDWRITING -> PointValues.DICTEE_CORRECT_HANDWRITTEN
        }
        val wordPoints = correctWords * perWord
        val perfectBonus = if (correctWords == totalWords && totalWords > 0) {
            PointValues.DICTEE_PERFECT_LIST
        } else {
            0
        }
        return wordPoints + perfectBonus
    }

    fun calculateMathPoints(
        correctCount: Int,
        streak: Int,
    ): Int {
        val basePoints = correctCount * PointValues.MATH_CORRECT
        val streakBonus = calculateStreakBonus(streak)
        return basePoints + streakBonus
    }

    fun calculateStreakBonus(streak: Int): Int {
        var bonus = 0
        if (streak >= 5) bonus += PointValues.MATH_STREAK_5
        if (streak >= 10) bonus += PointValues.MATH_STREAK_10
        if (streak >= 20) bonus += PointValues.MATH_STREAK_20
        return bonus
    }

    fun applyMultiplier(basePoints: Int, streak: Int): Int {
        val multiplier = PointValues.streakMultiplier(streak)
        return (basePoints * multiplier).toInt()
    }
}
