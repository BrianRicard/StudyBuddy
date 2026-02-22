package com.studybuddy.core.domain.usecase.math

import com.studybuddy.core.domain.model.Difficulty
import com.studybuddy.core.domain.model.Operator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

class GenerateProblemUseCaseTest {

    private val useCase = GenerateProblemUseCase()

    @RepeatedTest(100)
    fun `division problems have no remainder`() {
        val problem = useCase(setOf(Operator.DIVIDE), 1..12, Difficulty.MEDIUM, 0)
        assertEquals(0, problem.operandA % problem.operandB)
        assertEquals(problem.operandA / problem.operandB, problem.correctAnswer)
    }

    @RepeatedTest(100)
    fun `subtraction results are non-negative`() {
        val problem = useCase(setOf(Operator.MINUS), 1..17, Difficulty.MEDIUM, 0)
        assertTrue(problem.correctAnswer >= 0)
        assertEquals(problem.operandA - problem.operandB, problem.correctAnswer)
    }

    @RepeatedTest(100)
    fun `power problems stay within sensible range`() {
        val problem = useCase(setOf(Operator.POWER), 2..5, Difficulty.MEDIUM, 0)
        assertTrue(problem.operandA in 2..5, "Base should be 2..5, got ${problem.operandA}")
        assertTrue(problem.operandB in 1..3, "Exponent should be 1..3, got ${problem.operandB}")
        assertTrue(problem.correctAnswer <= 125, "Result should be <= 125, got ${problem.correctAnswer}")
    }

    @RepeatedTest(100)
    fun `addition produces correct answer`() {
        val problem = useCase(setOf(Operator.PLUS), 1..12, Difficulty.EASY, 0)
        assertEquals(problem.operandA + problem.operandB, problem.correctAnswer)
    }

    @RepeatedTest(100)
    fun `multiplication produces correct answer`() {
        val problem = useCase(setOf(Operator.MULTIPLY), 1..12, Difficulty.EASY, 0)
        assertEquals(problem.operandA * problem.operandB, problem.correctAnswer)
    }

    @RepeatedTest(100)
    fun `no trivial problems at medium difficulty`() {
        val problem = useCase(setOf(Operator.MULTIPLY), 1..12, Difficulty.MEDIUM, 0)
        assertTrue(problem.operandA != 0 && problem.operandB != 0, "Should not have zero operand")
        assertTrue(problem.operandA != 1 && problem.operandB != 1, "Should not have one operand")
    }

    @Test
    fun `adaptive difficulty expands range with streak`() {
        val problems = (1..50).map {
            useCase(setOf(Operator.PLUS), 1..10, Difficulty.ADAPTIVE, 20)
        }
        val maxOperand = problems.maxOf { maxOf(it.operandA, it.operandB) }
        assertTrue(maxOperand > 10, "Adaptive should expand range beyond 10, max was $maxOperand")
    }
}
