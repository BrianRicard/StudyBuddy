package com.studybuddy.core.domain.model.conjugation

import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.ILS_ELLES
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.IL_ELLE
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.JE
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.NOUS
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.TU
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.VOUS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class FrenchVerbsTest {

    @ParameterizedTest
    @CsvSource(
        "etre, JE, suis",
        "etre, TU, es",
        "etre, IL_ELLE, est",
        "etre, NOUS, sommes",
        "etre, VOUS, êtes",
        "etre, ILS_ELLES, sont",
        "avoir, JE, ai",
        "avoir, TU, as",
        "avoir, IL_ELLE, a",
        "avoir, NOUS, avons",
        "avoir, VOUS, avez",
        "avoir, ILS_ELLES, ont",
        "aimer, JE, aime",
        "aimer, TU, aimes",
        "aimer, IL_ELLE, aime",
        "aimer, NOUS, aimons",
        "aimer, VOUS, aimez",
        "aimer, ILS_ELLES, aiment",
        "aller, JE, vais",
        "aller, TU, vas",
        "aller, IL_ELLE, va",
        "aller, NOUS, allons",
        "aller, VOUS, allez",
        "aller, ILS_ELLES, vont",
        "faire, JE, fais",
        "faire, TU, fais",
        "faire, IL_ELLE, fait",
        "faire, NOUS, faisons",
        "faire, VOUS, faites",
        "faire, ILS_ELLES, font",
        "dire, JE, dis",
        "dire, TU, dis",
        "dire, IL_ELLE, dit",
        "dire, NOUS, disons",
        "dire, VOUS, dites",
        "dire, ILS_ELLES, disent",
    )
    fun `every present-tense form is correct`(
        verbId: String,
        person: ConjugationPerson,
        expected: String,
    ) {
        assertEquals(expected, FrenchVerbs.byId(verbId)?.form(person))
    }

    @ParameterizedTest
    @CsvSource(
        "etre, JE, je suis",
        "avoir, JE, j'ai",
        "aimer, JE, j'aime",
        "aller, JE, je vais",
        "etre, IL_ELLE, il est",
        "etre, ILS_ELLES, ils sont",
        "avoir, NOUS, nous avons",
        "aimer, TU, tu aimes",
    )
    fun `display applies elision only before vowels`(
        verbId: String,
        person: ConjugationPerson,
        expected: String,
    ) {
        assertEquals(expected, FrenchVerbs.byId(verbId)?.display(person))
    }

    @Test
    fun `every verb conjugates all six persons`() {
        FrenchVerbs.all.forEach { verb ->
            assertEquals(
                ConjugationPerson.entries.toSet(),
                verb.forms.keys,
                "${verb.infinitive} is missing persons",
            )
        }
    }

    @Test
    fun `every boss sentence contains a form of its verb`() {
        FrenchVerbs.all.forEach { verb ->
            verb.bossSentences.forEach { sentence ->
                val words = sentence.lowercase().split(" ", "'")
                assertTrue(
                    verb.forms.values.any { it in words },
                    "\"$sentence\" has no form of ${verb.infinitive}",
                )
            }
        }
    }

    @Test
    fun `stages cover the six quest verbs in order`() {
        assertEquals(listOf(1, 2, 3, 4, 5, 6), ConjugationStages.all.map { it.order })
        assertEquals(
            listOf("etre", "avoir", "aimer", "aller", "faire", "dire"),
            ConjugationStages.all.map { it.id },
        )
        assertEquals(ConjugationStages.all.size, ConjugationStages.all.map { it.id }.toSet().size)
    }
}
