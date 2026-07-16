package com.studybuddy.core.domain.model.conjugation

// NOTE: 'h' is deliberately excluded — elision before 'h' depends on h muet vs
// h aspiré and must be modelled per-verb if an h-verb is ever added.
private val VOWELS = setOf('a', 'e', 'i', 'o', 'u', 'é', 'è', 'ê')

/**
 * A verb with its present-tense forms for all six persons.
 *
 * @property id Stable identifier used for persistence and navigation (e.g. "etre").
 * Progress rows reference this id — treat it like a database column name and
 * never rename it.
 * @property infinitive Display form of the verb (e.g. "être").
 * @property forms The conjugated form (without pronoun) for each person.
 * @property bossSentences Short, kid-friendly sentences the stage boss raps.
 */
data class ConjugationVerb(
    val id: String,
    val infinitive: String,
    val forms: Map<ConjugationPerson, String>,
    val bossSentences: List<String>,
) {
    /** The conjugated form for [person], e.g. "suis". */
    fun form(person: ConjugationPerson): String = forms.getValue(person)

    /**
     * The full display form with pronoun, applying elision:
     * "je" + "aime" → "j'aime", but "je" + "suis" → "je suis".
     */
    fun display(person: ConjugationPerson): String {
        val form = form(person)
        val pronoun = person.spokenPronoun
        return if (pronoun == "je" && form.first() in VOWELS) "j'$form" else "$pronoun $form"
    }
}
