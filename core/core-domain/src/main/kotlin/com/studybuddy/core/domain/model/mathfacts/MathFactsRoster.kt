package com.studybuddy.core.domain.model.mathfacts

/**
 * The Jardin des Tables roster: tables 2–9, facts ×1–×10 — the CE2 canon,
 * recited table by table at school. 80 cards total.
 */
object MathFactsRoster {

    const val FIRST_TABLE = 2
    const val LAST_TABLE = 9
    const val FIRST_MULTIPLICAND = 1
    const val LAST_MULTIPLICAND = 10

    /** All tables covered by the garden, in teaching order. */
    val tables: List<Int> = (FIRST_TABLE..LAST_TABLE).toList()

    /**
     * Every fact, table-major (all of table 2 before table 3, …) — the order
     * new cards are introduced in Révision.
     */
    val all: List<MathFact> = tables.flatMap { table ->
        (FIRST_MULTIPLICAND..LAST_MULTIPLICAND).map { multiplicand ->
            MathFact(table, multiplicand)
        }
    }

    /** The ten facts of one [table], or empty when the table is not in the garden. */
    fun factsOf(table: Int): List<MathFact> = all.filter { it.table == table }

    fun isValid(fact: MathFact): Boolean = fact.table in FIRST_TABLE..LAST_TABLE &&
        fact.multiplicand in FIRST_MULTIPLICAND..LAST_MULTIPLICAND
}
