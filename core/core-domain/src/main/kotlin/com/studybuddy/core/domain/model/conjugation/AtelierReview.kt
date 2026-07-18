package com.studybuddy.core.domain.model.conjugation

import kotlinx.datetime.Instant

/**
 * Stored review state of one Atelier card: a (profile, verb, tense, person)
 * combination the child has answered at least once.
 *
 * @property box Leitner box, 0..[AtelierSchedule.MAX_BOX].
 * @property dueAt When the card should next be reviewed.
 * @property lapses Total number of wrong answers ever given for this card.
 */
data class AtelierReview(
    val id: String,
    val profileId: String,
    val verbId: String,
    val tense: ConjugationTense,
    val person: ConjugationPerson,
    val box: Int,
    val dueAt: Instant,
    val lapses: Int,
    val updatedAt: Instant,
)
