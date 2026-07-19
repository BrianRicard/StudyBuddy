package com.studybuddy.core.domain.model.mathfacts

import kotlinx.datetime.Instant

/**
 * Stored review state of one Jardin des Tables card: a (profile, table,
 * multiplicand) fact the child has answered at least once.
 *
 * @property box Leitner box, 0..[com.studybuddy.core.domain.model.srs.LeitnerSchedule.MAX_BOX].
 * @property dueAt When the card should next be reviewed.
 * @property lapses Total number of wrong answers ever given for this card.
 */
data class MathFactReview(
    val id: String,
    val profileId: String,
    val table: Int,
    val multiplicand: Int,
    val box: Int,
    val dueAt: Instant,
    val lapses: Int,
    val updatedAt: Instant,
) {
    val fact: MathFact get() = MathFact(table, multiplicand)
}
