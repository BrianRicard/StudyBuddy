package com.studybuddy.core.domain.model.mathfacts

/**
 * One multiplication fact, e.g. 7 × 8. Facts are kept per table — 7 × 3
 * belongs to the table de 7 and 3 × 7 to the table de 3 — matching how
 * French school teaches and recites each table.
 *
 * @property table The table this fact belongs to (the left factor).
 * @property multiplicand The right factor.
 */
data class MathFact(
    val table: Int,
    val multiplicand: Int,
) {
    val product: Int get() = table * multiplicand

    /** The written prompt, e.g. "7 × 8". */
    val prompt: String get() = "$table × $multiplicand"

    /**
     * The spoken prompt, e.g. "7 fois 8" — French TTS reads the digits as
     * words (« sept fois huit »), so no number-word table is needed.
     */
    val spokenPrompt: String get() = "$table fois $multiplicand"

    /**
     * The neighboring fact used for the attempt-2 strategy hint, the way CE2
     * teaches it: "7 × 7 = 49… alors 7 × 8 ?" (add-a-row). Null for ×1 facts,
     * which have no smaller neighbor.
     */
    val hintNeighbor: MathFact?
        get() = if (multiplicand > 1) MathFact(table, multiplicand - 1) else null
}
