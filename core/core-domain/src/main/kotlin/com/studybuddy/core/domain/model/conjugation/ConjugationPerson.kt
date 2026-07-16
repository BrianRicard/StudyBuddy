package com.studybuddy.core.domain.model.conjugation

/**
 * The six grammatical persons used in French conjugation practice.
 *
 * @property pronoun The pronoun as displayed to the child (e.g. "il / elle").
 * @property spokenPronoun The pronoun used when building a spoken/written full
 * form (e.g. "il"), where combined pronouns collapse to their first variant.
 */
enum class ConjugationPerson(
    val pronoun: String,
    val spokenPronoun: String,
) {
    JE("je", "je"),
    TU("tu", "tu"),
    IL_ELLE("il / elle", "il"),
    NOUS("nous", "nous"),
    VOUS("vous", "vous"),
    ILS_ELLES("ils / elles", "ils"),
}
