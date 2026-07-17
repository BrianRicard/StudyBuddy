package com.studybuddy.core.domain.model.conjugation

/**
 * One multiple-choice question in the battle step: "pronoun + ___ (infinitive)".
 *
 * @property options Candidate conjugated forms, already shuffled.
 * @property isReview True when the round drills a verb from an earlier stage.
 */
data class BattleRound(
    val verb: ConjugationVerb,
    val person: ConjugationPerson,
    val options: List<String>,
    val isReview: Boolean,
) {
    val correctForm: String get() = verb.form(person)

    /** Pronoun as shown in the prompt, elided for JE where needed ("j' ___"). */
    val promptPronoun: String
        get() = if (verb.display(person).startsWith("j'")) "j'" else person.pronoun
}
