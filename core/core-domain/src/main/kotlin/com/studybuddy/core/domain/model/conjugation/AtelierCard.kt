package com.studybuddy.core.domain.model.conjugation

/**
 * One drillable item: a conjugated form the child will hear and write.
 *
 * @property box Current Leitner box (0 for a card never seen).
 * @property isNew True when the card has never been answered.
 */
data class AtelierCard(
    val verb: ConjugationVerb,
    val tense: ConjugationTense,
    val person: ConjugationPerson,
    val box: Int,
    val isNew: Boolean,
) {
    /** The full prompt the child hears and must write, e.g. "j'étais". */
    val prompt: String get() = verb.display(tense, person)
}
