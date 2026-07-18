package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.common.extensions.stripAccents
import com.studybuddy.core.domain.model.conjugation.AtelierCard
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import javax.inject.Inject

/**
 * How a drill submission compares to the expected "pronoun + form" answer.
 * Ordered from best to worst; everything above [WRONG] is either accepted or
 * a free retry — only [WRONG] climbs the hint ladder.
 */
enum class DrillVerdict {
    /** Exact match (case/spacing-insensitive, accents strict). */
    CORRECT,

    /** The il/ils (elle/elles) homophone twin — accepted, then taught. */
    CORRECT_TWIN,

    /** Right letters, wrong or missing accents. Free retry with a glow. */
    ACCENT_MISS,

    /** Right letters, elision or spacing off ("je aime", "jaime"). Free retry. */
    ELISION_MISS,

    /** Correctly conjugated — for a different person. A hearing slip: replay, free retry. */
    WRONG_PRONOUN,

    /** Wrong stem or ending: the real learning target. Climbs the hint ladder. */
    WRONG,
}

/**
 * @property verdict Classification of the submission.
 * @property expected The expected display answer, e.g. "nous allons".
 * @property twin The accepted twin's display form when [DrillVerdict.CORRECT_TWIN].
 * @property matchedPrefixLength How many characters of [expected] the input got
 * right before diverging — drives the "locate, don't tell" amber zone.
 */
data class DrillAnswerCheck(
    val verdict: DrillVerdict,
    val expected: String,
    val twin: String? = null,
    val matchedPrefixLength: Int = 0,
)

/**
 * Grades one drill submission against a card.
 *
 * Accent-strict by design — the accent bar removes the keyboard gymnastics,
 * and futur vs imparfait live in the accents (chanterai / chantais). But an
 * accent slip is never a failure: it is a near-miss with a free retry.
 */
class CheckDrillAnswerUseCase @Inject constructor() {

    operator fun invoke(
        input: String,
        card: AtelierCard,
    ): DrillAnswerCheck {
        val expected = card.prompt
        val given = input.canonical()
        val wanted = expected.canonical()

        if (given == wanted) return DrillAnswerCheck(DrillVerdict.CORRECT, expected)

        homophoneTwin(card)?.let { twin ->
            if (given == twin.canonical()) {
                return DrillAnswerCheck(DrillVerdict.CORRECT_TWIN, expected, twin = twin)
            }
        }

        // Letters-only comparison (apostrophes and spaces removed) against the
        // expected answer and its un-elided variant — "j'aime" must also match
        // "je aime" and "jaime", where elision has dropped or fused letters.
        val wantedVariants = unelidedVariants(wanted)
        if (wantedVariants.any { given.lettersOnly() == it.lettersOnly() }) {
            return DrillAnswerCheck(DrillVerdict.ELISION_MISS, expected)
        }
        if (wantedVariants.any {
                given.lettersOnly().stripAccents() == it.lettersOnly().stripAccents()
            }
        ) {
            return DrillAnswerCheck(DrillVerdict.ACCENT_MISS, expected)
        }

        // Correct conjugation of a different person: a hearing slip, not a
        // memory failure (accent-lenient so one slip is not double-counted).
        val otherPersonMatch = ConjugationPerson.entries
            .filter { it != card.person }
            .any { person ->
                unelidedVariants(card.verb.display(card.tense, person).canonical()).any { other ->
                    given.lettersOnly().stripAccents() == other.lettersOnly().stripAccents()
                }
            }
        if (otherPersonMatch) return DrillAnswerCheck(DrillVerdict.WRONG_PRONOUN, expected)

        return DrillAnswerCheck(
            verdict = DrillVerdict.WRONG,
            expected = expected,
            matchedPrefixLength = given.commonPrefixWith(wanted).length,
        )
    }

    /**
     * The display form of the acoustically identical il/ils twin, or null.
     *
     * French makes 3rd-person singular and plural sound the same whenever the
     * plural is spelled singular-minus-final-letter + "ent" (chante/chantent,
     * chantait/chantaient, voit/voient — but not dit/disent or va/vont).
     * Derived from the data, never hand-maintained.
     */
    private fun homophoneTwin(card: AtelierCard): String? {
        val twinPerson = when (card.person) {
            ConjugationPerson.IL_ELLE -> ConjugationPerson.ILS_ELLES
            ConjugationPerson.ILS_ELLES -> ConjugationPerson.IL_ELLE
            else -> return null
        }
        val singular = card.verb.form(card.tense, ConjugationPerson.IL_ELLE)
        val plural = card.verb.form(card.tense, ConjugationPerson.ILS_ELLES)
        val homophones = plural == singular.dropLast(1) + "ent"
        return if (homophones) card.verb.display(card.tense, twinPerson) else null
    }

    /** The canonical answer plus its un-elided spelling ("j'aime" → "je aime"). */
    private fun unelidedVariants(wanted: String): List<String> = if (wanted.startsWith("j'")) {
        listOf(wanted, "je " + wanted.removePrefix("j'"))
    } else {
        listOf(wanted)
    }

    /** Lowercase, trimmed, single spaces, straight apostrophes. */
    private fun String.canonical(): String = trim()
        .lowercase()
        .replace('’', '\'')
        .replace(Regex("\\s+"), " ")

    private fun String.lettersOnly(): String = replace("'", "").replace(" ", "")
}
