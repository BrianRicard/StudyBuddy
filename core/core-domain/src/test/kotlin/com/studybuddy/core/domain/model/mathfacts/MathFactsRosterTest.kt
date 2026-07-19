package com.studybuddy.core.domain.model.mathfacts

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class MathFactsRosterTest {

    @Test
    fun `the roster is the CE2 canon — tables 2 to 9, facts 1 to 10`() {
        assertEquals(80, MathFactsRoster.all.size)
        assertEquals((2..9).toList(), MathFactsRoster.tables)
        assertEquals(MathFactsRoster.all.size, MathFactsRoster.all.toSet().size)
        assertTrue(MathFactsRoster.all.all { it.multiplicand in 1..10 })
    }

    @Test
    fun `facts are introduced table-major`() {
        // All of the table de 2 comes before anything from the table de 3.
        assertEquals(List(10) { 2 }, MathFactsRoster.all.take(10).map { it.table })
        assertEquals((1..10).toList(), MathFactsRoster.all.take(10).map { it.multiplicand })
        assertEquals(3, MathFactsRoster.all[10].table)
    }

    @Test
    fun `factsOf returns the full recited table in order`() {
        val sept = MathFactsRoster.factsOf(7)
        assertEquals(10, sept.size)
        assertEquals((1..10).toList(), sept.map { it.multiplicand })
        assertTrue(MathFactsRoster.factsOf(11).isEmpty())
    }

    @ParameterizedTest
    @CsvSource(
        "7, 8, 56",
        "9, 9, 81",
        "2, 1, 2",
        "6, 10, 60",
    )
    fun `products and prompts are consistent`(
        table: Int,
        multiplicand: Int,
        product: Int,
    ) {
        val fact = MathFact(table, multiplicand)
        assertEquals(product, fact.product)
        assertEquals("$table × $multiplicand", fact.prompt)
        assertEquals("$table fois $multiplicand", fact.spokenPrompt)
    }

    @Test
    fun `the strategy hint is the previous fact of the same table`() {
        assertEquals(MathFact(7, 7), MathFact(7, 8).hintNeighbor)
        assertEquals(49, MathFact(7, 8).hintNeighbor?.product)
        // ×1 has no smaller neighbor.
        assertNull(MathFact(7, 1).hintNeighbor)
    }

    @Test
    fun `validity covers exactly the roster`() {
        assertTrue(MathFactsRoster.all.all { MathFactsRoster.isValid(it) })
        assertFalse(MathFactsRoster.isValid(MathFact(1, 5)))
        assertFalse(MathFactsRoster.isValid(MathFact(10, 5)))
        assertFalse(MathFactsRoster.isValid(MathFact(7, 0)))
        assertFalse(MathFactsRoster.isValid(MathFact(7, 11)))
    }
}
