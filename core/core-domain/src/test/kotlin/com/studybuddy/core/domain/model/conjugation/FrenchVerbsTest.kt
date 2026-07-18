package com.studybuddy.core.domain.model.conjugation

import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.ILS_ELLES
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.IL_ELLE
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.JE
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.NOUS
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.TU
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.VOUS
import com.studybuddy.core.domain.model.conjugation.ConjugationTense.FUTUR
import com.studybuddy.core.domain.model.conjugation.ConjugationTense.IMPARFAIT
import com.studybuddy.core.domain.model.conjugation.ConjugationTense.PRESENT
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
        "chanter, JE, chante",
        "chanter, TU, chantes",
        "chanter, IL_ELLE, chante",
        "chanter, NOUS, chantons",
        "chanter, VOUS, chantez",
        "chanter, ILS_ELLES, chantent",
        "jouer, JE, joue",
        "jouer, TU, joues",
        "jouer, IL_ELLE, joue",
        "jouer, NOUS, jouons",
        "jouer, VOUS, jouez",
        "jouer, ILS_ELLES, jouent",
        "manger, JE, mange",
        "manger, TU, manges",
        "manger, IL_ELLE, mange",
        "manger, NOUS, mangeons",
        "manger, VOUS, mangez",
        "manger, ILS_ELLES, mangent",
        "donner, JE, donne",
        "donner, TU, donnes",
        "donner, IL_ELLE, donne",
        "donner, NOUS, donnons",
        "donner, VOUS, donnez",
        "donner, ILS_ELLES, donnent",
        "regarder, JE, regarde",
        "regarder, TU, regardes",
        "regarder, IL_ELLE, regarde",
        "regarder, NOUS, regardons",
        "regarder, VOUS, regardez",
        "regarder, ILS_ELLES, regardent",
        "ecouter, JE, écoute",
        "ecouter, TU, écoutes",
        "ecouter, IL_ELLE, écoute",
        "ecouter, NOUS, écoutons",
        "ecouter, VOUS, écoutez",
        "ecouter, ILS_ELLES, écoutent",
        "finir, JE, finis",
        "finir, TU, finis",
        "finir, IL_ELLE, finit",
        "finir, NOUS, finissons",
        "finir, VOUS, finissez",
        "finir, ILS_ELLES, finissent",
        "choisir, JE, choisis",
        "choisir, TU, choisis",
        "choisir, IL_ELLE, choisit",
        "choisir, NOUS, choisissons",
        "choisir, VOUS, choisissez",
        "choisir, ILS_ELLES, choisissent",
        "grandir, JE, grandis",
        "grandir, TU, grandis",
        "grandir, IL_ELLE, grandit",
        "grandir, NOUS, grandissons",
        "grandir, VOUS, grandissez",
        "grandir, ILS_ELLES, grandissent",
        "venir, JE, viens",
        "venir, TU, viens",
        "venir, IL_ELLE, vient",
        "venir, NOUS, venons",
        "venir, VOUS, venez",
        "venir, ILS_ELLES, viennent",
        "prendre, JE, prends",
        "prendre, TU, prends",
        "prendre, IL_ELLE, prend",
        "prendre, NOUS, prenons",
        "prendre, VOUS, prenez",
        "prendre, ILS_ELLES, prennent",
        "voir, JE, vois",
        "voir, TU, vois",
        "voir, IL_ELLE, voit",
        "voir, NOUS, voyons",
        "voir, VOUS, voyez",
        "voir, ILS_ELLES, voient",
        "vouloir, JE, veux",
        "vouloir, TU, veux",
        "vouloir, IL_ELLE, veut",
        "vouloir, NOUS, voulons",
        "vouloir, VOUS, voulez",
        "vouloir, ILS_ELLES, veulent",
        "pouvoir, JE, peux",
        "pouvoir, TU, peux",
        "pouvoir, IL_ELLE, peut",
        "pouvoir, NOUS, pouvons",
        "pouvoir, VOUS, pouvez",
        "pouvoir, ILS_ELLES, peuvent",
        "partir, JE, pars",
        "partir, TU, pars",
        "partir, IL_ELLE, part",
        "partir, NOUS, partons",
        "partir, VOUS, partez",
        "partir, ILS_ELLES, partent",
        "mettre, JE, mets",
        "mettre, TU, mets",
        "mettre, IL_ELLE, met",
        "mettre, NOUS, mettons",
        "mettre, VOUS, mettez",
        "mettre, ILS_ELLES, mettent",
        "dormir, JE, dors",
        "dormir, TU, dors",
        "dormir, IL_ELLE, dort",
        "dormir, NOUS, dormons",
        "dormir, VOUS, dormez",
        "dormir, ILS_ELLES, dorment",
        "lire, JE, lis",
        "lire, TU, lis",
        "lire, IL_ELLE, lit",
        "lire, NOUS, lisons",
        "lire, VOUS, lisez",
        "lire, ILS_ELLES, lisent",
    )
    fun `every present-tense form is correct`(
        verbId: String,
        person: ConjugationPerson,
        expected: String,
    ) {
        assertEquals(expected, FrenchVerbs.byId(verbId)?.form(person))
    }

    /**
     * The futur stem is the only irregular part of the futur simple — pin it
     * for every verb via the je-form; the endings test below propagates
     * correctness to the other five persons.
     */
    @ParameterizedTest
    @CsvSource(
        "etre, serai",
        "avoir, aurai",
        "aimer, aimerai",
        "aller, irai",
        "faire, ferai",
        "dire, dirai",
        "chanter, chanterai",
        "jouer, jouerai",
        "manger, mangerai",
        "donner, donnerai",
        "regarder, regarderai",
        "ecouter, écouterai",
        "finir, finirai",
        "choisir, choisirai",
        "grandir, grandirai",
        "venir, viendrai",
        "prendre, prendrai",
        "voir, verrai",
        "vouloir, voudrai",
        "pouvoir, pourrai",
        "partir, partirai",
        "mettre, mettrai",
        "dormir, dormirai",
        "lire, lirai",
    )
    fun `futur stem is correct for every verb`(
        verbId: String,
        expectedJeForm: String,
    ) {
        assertEquals(expectedJeForm, FrenchVerbs.byId(verbId)?.form(FUTUR, JE))
    }

    @Test
    fun `futur endings are universal across all verbs`() {
        val endings = mapOf(
            JE to "ai",
            TU to "as",
            IL_ELLE to "a",
            NOUS to "ons",
            VOUS to "ez",
            ILS_ELLES to "ont",
        )
        FrenchVerbs.all.forEach { verb ->
            val jeForm = verb.form(FUTUR, JE)
            assertTrue(jeForm.endsWith("ai"), "${verb.infinitive}: futur je-form must end in -ai")
            val stem = jeForm.removeSuffix("ai")
            endings.forEach { (person, ending) ->
                assertEquals(
                    stem + ending,
                    verb.form(FUTUR, person),
                    "${verb.infinitive} futur $person",
                )
            }
        }
    }

    /**
     * For every verb but être, the imparfait stem is the present nous-form
     * minus -ons. Together with the present-tense gold table this verifies the
     * whole imparfait, including the -ge- softening of manger (mangeais but
     * mangions).
     */
    @Test
    fun `imparfait derives from the present nous stem`() {
        val endings = mapOf(
            JE to "ais",
            TU to "ais",
            IL_ELLE to "ait",
            NOUS to "ions",
            VOUS to "iez",
            ILS_ELLES to "aient",
        )
        FrenchVerbs.all.forEach { verb ->
            val stem = if (verb.id == "etre") "ét" else verb.form(PRESENT, NOUS).removeSuffix("ons")
            endings.forEach { (person, ending) ->
                val softened = if (stem.endsWith("ge") && ending.startsWith("i")) stem.dropLast(1) else stem
                assertEquals(
                    softened + ending,
                    verb.form(IMPARFAIT, person),
                    "${verb.infinitive} imparfait $person",
                )
            }
        }
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

    @ParameterizedTest
    @CsvSource(
        "etre, IMPARFAIT, JE, j'étais",
        "etre, FUTUR, JE, je serai",
        "avoir, FUTUR, JE, j'aurai",
        "aller, FUTUR, JE, j'irai",
        "aimer, IMPARFAIT, JE, j'aimais",
        "ecouter, PRESENT, JE, j'écoute",
        "ecouter, PRESENT, TU, tu écoutes",
        "chanter, PRESENT, JE, je chante",
        "vouloir, FUTUR, JE, je voudrai",
        "manger, IMPARFAIT, NOUS, nous mangions",
    )
    fun `display applies elision across tenses`(
        verbId: String,
        tense: ConjugationTense,
        person: ConjugationPerson,
        expected: String,
    ) {
        assertEquals(expected, FrenchVerbs.byId(verbId)?.display(tense, person))
    }

    @Test
    fun `every verb conjugates all six persons in all three tenses`() {
        FrenchVerbs.all.forEach { verb ->
            assertEquals(
                ConjugationTense.entries.toSet(),
                verb.tenses.keys,
                "${verb.infinitive} is missing tenses",
            )
            verb.tenses.forEach { (tense, forms) ->
                assertEquals(
                    ConjugationPerson.entries.toSet(),
                    forms.keys,
                    "${verb.infinitive} $tense is missing persons",
                )
            }
        }
    }

    @Test
    fun `the forms shorthand returns the present tense`() {
        FrenchVerbs.all.forEach { verb ->
            assertEquals(verb.tenses.getValue(PRESENT), verb.forms, verb.infinitive)
        }
    }

    @Test
    fun `verb ids are unique`() {
        assertEquals(FrenchVerbs.all.size, FrenchVerbs.all.map { it.id }.toSet().size)
    }

    @Test
    fun `premier groupe verbs are regular -er verbs`() {
        val premierGroupe = FrenchVerbs.all.filter { it.group == VerbGroup.PREMIER_GROUPE }
        assertTrue(premierGroupe.isNotEmpty())
        premierGroupe.forEach { verb ->
            assertTrue(verb.infinitive.endsWith("er"), "${verb.infinitive} is not an -er verb")
            assertEquals(
                verb.infinitive + "ai",
                verb.form(FUTUR, JE),
                "${verb.infinitive}: 1st-group futur stem is the infinitive",
            )
        }
    }

    @Test
    fun `deuxieme groupe verbs take -issons in present nous`() {
        val deuxiemeGroupe = FrenchVerbs.all.filter { it.group == VerbGroup.DEUXIEME_GROUPE }
        assertTrue(deuxiemeGroupe.isNotEmpty())
        deuxiemeGroupe.forEach { verb ->
            assertTrue(verb.infinitive.endsWith("ir"), "${verb.infinitive} is not an -ir verb")
            assertEquals(verb.infinitive.dropLast(2) + "issons", verb.form(PRESENT, NOUS))
            assertEquals(verb.infinitive + "ai", verb.form(FUTUR, JE))
        }
    }

    @Test
    fun `auxiliaires are exactly etre and avoir`() {
        assertEquals(
            listOf("etre", "avoir"),
            FrenchVerbs.all.filter { it.group == VerbGroup.AUXILIAIRE }.map { it.id },
        )
    }

    @Test
    fun `quest verbs keep their teaching order`() {
        assertEquals(
            listOf("etre", "avoir", "aimer", "aller", "faire", "dire"),
            FrenchVerbs.questVerbs.map { it.id },
        )
        FrenchVerbs.questVerbs.forEach { verb ->
            assertTrue(verb.bossSentences.isNotEmpty(), "${verb.infinitive} has no boss sentences")
        }
    }

    @Test
    fun `every boss sentence starts with a correct pronoun and form of its verb`() {
        FrenchVerbs.all.forEach { verb ->
            verb.bossSentences.forEach { sentence ->
                assertTrue(
                    ConjugationPerson.entries.any {
                        sentence.lowercase().startsWith(verb.display(it).lowercase())
                    },
                    "\"$sentence\" does not start with a pronoun+form of ${verb.infinitive}",
                )
            }
        }
    }

    @Test
    fun `no form starts with h — the elision rule assumes none does`() {
        FrenchVerbs.all.forEach { verb ->
            verb.tenses.values.forEach { forms ->
                assertTrue(
                    forms.values.none { it.first() == 'h' },
                    "${verb.infinitive} has an h-form; model h muet/aspiré before adding it",
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
