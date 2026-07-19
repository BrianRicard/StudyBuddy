package com.studybuddy.core.domain.usecase.mathfacts

import com.studybuddy.core.domain.model.mathfacts.MathFact
import javax.inject.Inject

/** How a tables submission compares to the expected product. */
enum class TablesVerdict {
    CORRECT,
    WRONG,
}

/**
 * @property verdict Classification of the submission.
 * @property expected The expected product, e.g. 56.
 * @property hintNeighbor The neighboring fact for the attempt-2 strategy hint
 * ("7 × 7 = 49… alors 7 × 8 ?"), null for ×1 facts.
 */
data class TablesAnswerCheck(
    val verdict: TablesVerdict,
    val expected: Int,
    val hintNeighbor: MathFact?,
)

/**
 * Grades one drill submission against a fact. Whitespace-tolerant; anything
 * that is not the right number — including non-numeric input — is simply
 * WRONG and climbs the gentle hint ladder.
 */
class CheckTablesAnswerUseCase @Inject constructor() {

    operator fun invoke(
        input: String,
        fact: MathFact,
    ): TablesAnswerCheck {
        val answered = input.trim().toIntOrNull()
        val verdict = if (answered == fact.product) TablesVerdict.CORRECT else TablesVerdict.WRONG
        return TablesAnswerCheck(
            verdict = verdict,
            expected = fact.product,
            hintNeighbor = fact.hintNeighbor,
        )
    }
}
