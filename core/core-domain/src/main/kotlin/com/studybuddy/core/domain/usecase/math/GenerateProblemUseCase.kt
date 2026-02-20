package com.studybuddy.core.domain.usecase.math

import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.domain.model.Difficulty
import com.studybuddy.core.domain.model.MathProblem
import com.studybuddy.core.domain.model.Operator
import javax.inject.Inject
import kotlin.math.pow

class GenerateProblemUseCase @Inject constructor() {

    operator fun invoke(
        operators: Set<Operator>,
        range: IntRange,
        difficulty: Difficulty,
        currentStreak: Int,
    ): MathProblem {
        require(operators.isNotEmpty()) { "At least one operator must be selected" }

        val adaptedRange = if (difficulty == Difficulty.ADAPTIVE) {
            val expansion = currentStreak / 5
            range.first..(range.last + expansion).coerceAtMost(AppConstants.MAX_NUMBER_RANGE)
        } else {
            range
        }

        val operator = operators.random()
        return generateForOperator(operator, adaptedRange, difficulty)
    }

    private fun generateForOperator(
        operator: Operator,
        range: IntRange,
        difficulty: Difficulty,
    ): MathProblem {
        return when (operator) {
            Operator.PLUS -> generateAddition(range, difficulty)
            Operator.MINUS -> generateSubtraction(range, difficulty)
            Operator.MULTIPLY -> generateMultiplication(range, difficulty)
            Operator.DIVIDE -> generateDivision(range, difficulty)
            Operator.POWER -> generatePower()
        }
    }

    private fun generateAddition(range: IntRange, difficulty: Difficulty): MathProblem {
        var a: Int
        var b: Int
        do {
            a = range.random()
            b = range.random()
        } while (isTrivial(a, b, Operator.PLUS, difficulty))
        return MathProblem(a, b, Operator.PLUS, a + b)
    }

    private fun generateSubtraction(range: IntRange, difficulty: Difficulty): MathProblem {
        var a: Int
        var b: Int
        do {
            a = range.random()
            b = range.random()
            if (a < b) {
                val temp = a; a = b; b = temp
            }
        } while (isTrivial(a, b, Operator.MINUS, difficulty))
        return MathProblem(a, b, Operator.MINUS, a - b)
    }

    private fun generateMultiplication(range: IntRange, difficulty: Difficulty): MathProblem {
        var a: Int
        var b: Int
        do {
            a = range.random()
            b = range.random()
        } while (isTrivial(a, b, Operator.MULTIPLY, difficulty))
        return MathProblem(a, b, Operator.MULTIPLY, a * b)
    }

    private fun generateDivision(range: IntRange, difficulty: Difficulty): MathProblem {
        var a: Int
        var b: Int
        do {
            b = (range.first.coerceAtLeast(1)..range.last).random()
            val multiplier = range.random().coerceAtLeast(1)
            a = b * multiplier
        } while (isTrivial(a, b, Operator.DIVIDE, difficulty))
        return MathProblem(a, b, Operator.DIVIDE, a / b)
    }

    private fun generatePower(): MathProblem {
        val base = (AppConstants.POWER_BASE_MIN..AppConstants.POWER_BASE_MAX).random()
        val exponent = (AppConstants.POWER_EXPONENT_MIN..AppConstants.POWER_EXPONENT_MAX).random()
        val result = base.toDouble().pow(exponent).toInt()
        return MathProblem(base, exponent, Operator.POWER, result)
    }

    private fun isTrivial(a: Int, b: Int, operator: Operator, difficulty: Difficulty): Boolean {
        if (difficulty == Difficulty.EASY) return false
        return when (operator) {
            Operator.PLUS -> a == 0 || b == 0
            Operator.MINUS -> b == 0
            Operator.MULTIPLY -> a == 0 || b == 0 || a == 1 || b == 1
            Operator.DIVIDE -> b == 1 || a == 0
            Operator.POWER -> false
        }
    }
}
