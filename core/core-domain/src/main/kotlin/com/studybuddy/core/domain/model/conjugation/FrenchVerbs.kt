package com.studybuddy.core.domain.model.conjugation

import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.ILS_ELLES
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.IL_ELLE
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.JE
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.NOUS
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.TU
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson.VOUS

/**
 * Present-tense conjugations for the Verb Quest.
 *
 * The verb content itself is French only (the app UI around it is localized).
 * Boss sentences are story-flavoured: the child is on a quest to bring back
 * the Grumpy King's smile.
 */
object FrenchVerbs {

    val ETRE = ConjugationVerb(
        id = "etre",
        infinitive = "être",
        forms = mapOf(
            JE to "suis",
            TU to "es",
            IL_ELLE to "est",
            NOUS to "sommes",
            VOUS to "êtes",
            ILS_ELLES to "sont",
        ),
        bossSentences = listOf(
            "Je suis ton ami",
            "Nous sommes des héros",
            "Tu es vraiment super",
        ),
    )

    val AVOIR = ConjugationVerb(
        id = "avoir",
        infinitive = "avoir",
        forms = mapOf(
            JE to "ai",
            TU to "as",
            IL_ELLE to "a",
            NOUS to "avons",
            VOUS to "avez",
            ILS_ELLES to "ont",
        ),
        bossSentences = listOf(
            "J'ai un grand sourire",
            "Nous avons du courage",
            "Ils ont des étoiles",
        ),
    )

    val AIMER = ConjugationVerb(
        id = "aimer",
        infinitive = "aimer",
        forms = mapOf(
            JE to "aime",
            TU to "aimes",
            IL_ELLE to "aime",
            NOUS to "aimons",
            VOUS to "aimez",
            ILS_ELLES to "aiment",
        ),
        bossSentences = listOf(
            "J'aime chanter avec toi",
            "Nous aimons les fleurs",
            "Vous aimez la musique",
        ),
    )

    val ALLER = ConjugationVerb(
        id = "aller",
        infinitive = "aller",
        forms = mapOf(
            JE to "vais",
            TU to "vas",
            IL_ELLE to "va",
            NOUS to "allons",
            VOUS to "allez",
            ILS_ELLES to "vont",
        ),
        bossSentences = listOf(
            "Je vais au château",
            "Nous allons très vite",
            "Tu vas gagner",
        ),
    )

    val FAIRE = ConjugationVerb(
        id = "faire",
        infinitive = "faire",
        forms = mapOf(
            JE to "fais",
            TU to "fais",
            IL_ELLE to "fait",
            NOUS to "faisons",
            VOUS to "faites",
            ILS_ELLES to "font",
        ),
        bossSentences = listOf(
            "Je fais de la magie",
            "Nous faisons la fête",
            "Vous faites un gâteau",
        ),
    )

    val DIRE = ConjugationVerb(
        id = "dire",
        infinitive = "dire",
        forms = mapOf(
            JE to "dis",
            TU to "dis",
            IL_ELLE to "dit",
            NOUS to "disons",
            VOUS to "dites",
            ILS_ELLES to "disent",
        ),
        bossSentences = listOf(
            "Je dis que tu es super",
            "Nous disons bonjour au roi",
            "Vous dites des mots magiques",
        ),
    )

    /** All quest verbs in teaching order (easiest and most common first). */
    val all: List<ConjugationVerb> = listOf(ETRE, AVOIR, AIMER, ALLER, FAIRE, DIRE)

    private val byId = all.associateBy { it.id }

    fun byId(id: String): ConjugationVerb? = byId[id]
}
