package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.AtelierCard
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.model.conjugation.FrenchVerbs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

private fun card(
    verbId: String,
    tense: ConjugationTense,
    person: ConjugationPerson,
) = AtelierCard(
    verb = FrenchVerbs.byId(verbId)!!,
    tense = tense,
    person = person,
    box = 0,
    isNew = false,
)

class CheckDrillAnswerUseCaseTest {

    private val check = CheckDrillAnswerUseCase()

    @ParameterizedTest
    @CsvSource(
        "etre, PRESENT, JE, je suis",
        "etre, PRESENT, JE, 'Je  Suis'",
        "aimer, PRESENT, JE, j'aime",
        "aimer, PRESENT, JE, j’aime",
        "etre, IMPARFAIT, JE, j'étais",
        "aller, FUTUR, NOUS, nous irons",
        "manger, IMPARFAIT, NOUS, nous mangions",
    )
    fun `exact answers are correct, whatever the case or spacing`(
        verbId: String,
        tense: ConjugationTense,
        person: ConjugationPerson,
        input: String,
    ) {
        assertEquals(DrillVerdict.CORRECT, check(input, card(verbId, tense, person)).verdict)
    }

    @ParameterizedTest
    @CsvSource(
        // Présent, 1st group: chante/chantent are acoustic twins.
        "chanter, PRESENT, IL_ELLE, ils chantent",
        "chanter, PRESENT, ILS_ELLES, il chante",
        "jouer, PRESENT, IL_ELLE, ils jouent",
        // voit/voient too.
        "voir, PRESENT, IL_ELLE, ils voient",
        // Imparfait: -ait/-aient are twins for every verb.
        "etre, IMPARFAIT, IL_ELLE, ils étaient",
        "finir, IMPARFAIT, ILS_ELLES, il finissait",
        "chanter, IMPARFAIT, IL_ELLE, ils chantaient",
    )
    fun `acoustic twins are accepted`(
        verbId: String,
        tense: ConjugationTense,
        person: ConjugationPerson,
        input: String,
    ) {
        val result = check(input, card(verbId, tense, person))
        assertEquals(DrillVerdict.CORRECT_TWIN, result.verdict)
        assertEquals(input, result.twin)
    }

    @ParameterizedTest
    @CsvSource(
        // dit/disent, va/vont, est/sont, part/partent sound different.
        "dire, PRESENT, IL_ELLE, ils disent",
        "aller, PRESENT, IL_ELLE, ils vont",
        "etre, PRESENT, IL_ELLE, ils sont",
        "partir, PRESENT, ILS_ELLES, il part",
        // Futur -a/-ont are never twins.
        "chanter, FUTUR, IL_ELLE, ils chanteront",
    )
    fun `audibly different persons are not twins`(
        verbId: String,
        tense: ConjugationTense,
        person: ConjugationPerson,
        input: String,
    ) {
        val result = check(input, card(verbId, tense, person))
        assertTrue(result.verdict != DrillVerdict.CORRECT_TWIN, "$input must not be a twin")
        assertNull(result.twin)
    }

    @ParameterizedTest
    @CsvSource(
        "etre, IMPARFAIT, JE, j'etais",
        "etre, PRESENT, VOUS, vous etes",
        "ecouter, PRESENT, JE, j'ecoute",
        "chanter, IMPARFAIT, NOUS, nous chantîons",
    )
    fun `right letters with wrong accents are an accent miss`(
        verbId: String,
        tense: ConjugationTense,
        person: ConjugationPerson,
        input: String,
    ) {
        assertEquals(DrillVerdict.ACCENT_MISS, check(input, card(verbId, tense, person)).verdict)
    }

    @ParameterizedTest
    @CsvSource(
        "aimer, PRESENT, JE, je aime",
        "aimer, PRESENT, JE, jaime",
        "etre, IMPARFAIT, JE, je étais",
        "etre, PRESENT, NOUS, noussommes",
    )
    fun `elision and spacing slips are an elision miss`(
        verbId: String,
        tense: ConjugationTense,
        person: ConjugationPerson,
        input: String,
    ) {
        assertEquals(DrillVerdict.ELISION_MISS, check(input, card(verbId, tense, person)).verdict)
    }

    @ParameterizedTest
    @CsvSource(
        // Heard "tu chantes", wrote another person perfectly conjugated.
        "chanter, PRESENT, TU, nous chantons",
        "etre, PRESENT, JE, tu es",
        "aller, FUTUR, NOUS, vous irez",
        // Accent-lenient: a hearing slip plus an accent slip is still a hearing slip.
        "etre, IMPARFAIT, TU, j'etais",
    )
    fun `a correct form for another person is a hearing slip`(
        verbId: String,
        tense: ConjugationTense,
        person: ConjugationPerson,
        input: String,
    ) {
        assertEquals(DrillVerdict.WRONG_PRONOUN, check(input, card(verbId, tense, person)).verdict)
    }

    @ParameterizedTest
    @CsvSource(
        "etre, PRESENT, JE, je sui, 6",
        // 'nous chantons' (présent) for 'nous chanterons' (futur): tense confusion
        // is a real learning target, so it climbs the ladder — diverges after 'nous chant'.
        "chanter, FUTUR, NOUS, nous chantons, 10",
        "etre, PRESENT, NOUS, nous somme, 10",
        "dire, PRESENT, VOUS, vous disez, 7",
    )
    fun `wrong answers report where they diverge`(
        verbId: String,
        tense: ConjugationTense,
        person: ConjugationPerson,
        input: String,
        expectedPrefix: Int,
    ) {
        val result = check(input, card(verbId, tense, person))
        assertEquals(DrillVerdict.WRONG, result.verdict)
        assertEquals(expectedPrefix, result.matchedPrefixLength)
    }

    @Test
    fun `expected display answer is always returned`() {
        val result = check("n'importe quoi", card("aller", ConjugationTense.FUTUR, ConjugationPerson.JE))
        assertEquals("j'irai", result.expected)
    }

    @Test
    fun `twins never fire for je tu nous vous`() {
        listOf(
            ConjugationPerson.JE,
            ConjugationPerson.TU,
            ConjugationPerson.NOUS,
            ConjugationPerson.VOUS,
        ).forEach { person ->
            val result = check("ils chantaient", card("chanter", ConjugationTense.IMPARFAIT, person))
            assertTrue(result.verdict != DrillVerdict.CORRECT_TWIN, "$person must have no twin")
        }
    }
}
