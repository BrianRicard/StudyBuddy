package com.studybuddy.core.domain.model.conjugation

// NOTE: 'h' is deliberately excluded — elision before 'h' depends on h muet vs
// h aspiré and must be modelled per-verb if an h-verb is ever added.
private val VOWELS = setOf('a', 'e', 'i', 'o', 'u', 'é', 'è', 'ê')

/**
 * A verb with its conjugated forms for every supported tense and person.
 *
 * @property id Stable identifier used for persistence and navigation (e.g. "etre").
 * Progress and review rows reference this id — treat it like a database column
 * name and never rename it.
 * @property infinitive Display form of the verb (e.g. "être").
 * @property group School classification used to organise the Atelier grid.
 * @property tenses The conjugated forms (without pronoun) per tense and person.
 * @property bossSentences Short, kid-friendly sentences the stage boss raps.
 * Only the six Verb Quest verbs have these; Atelier-only verbs leave it empty.
 */
data class ConjugationVerb(
    val id: String,
    val infinitive: String,
    val group: VerbGroup,
    val tenses: Map<ConjugationTense, Map<ConjugationPerson, String>>,
    val bossSentences: List<String> = emptyList(),
) {

    /** Present-tense forms — the Verb Quest shorthand. */
    val forms: Map<ConjugationPerson, String>
        get() = tenses.getValue(ConjugationTense.PRESENT)

    /** The present-tense form for [person], e.g. "suis". */
    fun form(person: ConjugationPerson): String = form(ConjugationTense.PRESENT, person)

    /** The conjugated form for [tense] and [person], e.g. "serai". */
    fun form(
        tense: ConjugationTense,
        person: ConjugationPerson,
    ): String = tenses.getValue(tense).getValue(person)

    /**
     * The full present-tense display form with pronoun, applying elision:
     * "je" + "aime" → "j'aime", but "je" + "suis" → "je suis".
     */
    fun display(person: ConjugationPerson): String = display(ConjugationTense.PRESENT, person)

    /** The full display form with pronoun for [tense], applying elision. */
    fun display(
        tense: ConjugationTense,
        person: ConjugationPerson,
    ): String {
        val form = form(tense, person)
        val pronoun = person.spokenPronoun
        return if (pronoun == "je" && form.first() in VOWELS) "j'$form" else "$pronoun $form"
    }
}
