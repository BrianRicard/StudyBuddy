package com.studybuddy.core.domain.usecase.mathfacts

import com.studybuddy.core.domain.model.mathfacts.MathFact
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CheckTablesAnswerUseCaseTest {

    private val check = CheckTablesAnswerUseCase()

    @ParameterizedTest
    @CsvSource(
        "7, 8, '56'",
        "7, 8, ' 56 '",
        "9, 9, '81'",
        "2, 1, '2'",
    )
    fun `the right product is correct, whitespace-tolerant`(
        table: Int,
        multiplicand: Int,
        input: String,
    ) {
        assertEquals(
            TablesVerdict.CORRECT,
            check(input, MathFact(table, multiplicand)).verdict,
        )
    }

    @ParameterizedTest
    @CsvSource(
        "7, 8, '54'",
        "7, 8, '65'",
        "7, 8, ''",
        "7, 8, 'abc'",
        "7, 8, '5 6'",
    )
    fun `anything else is wrong, without crashing`(
        table: Int,
        multiplicand: Int,
        input: String,
    ) {
        val result = check(input, MathFact(table, multiplicand))
        assertEquals(TablesVerdict.WRONG, result.verdict)
        assertEquals(56, result.expected)
    }

    @Test
    fun `the check carries the strategy hint`() {
        val result = check("54", MathFact(7, 8))
        assertEquals(MathFact(7, 7), result.hintNeighbor)

        assertNull(check("9", MathFact(9, 1)).hintNeighbor)
    }
}
