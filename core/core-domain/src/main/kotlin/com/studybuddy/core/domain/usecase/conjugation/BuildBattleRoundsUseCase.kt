package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.BattleRound
import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationStage
import com.studybuddy.core.domain.model.conjugation.ConjugationStages
import com.studybuddy.core.domain.model.conjugation.ConjugationVerb
import javax.inject.Inject
import kotlin.random.Random

/**
 * Builds the question list for a battle: one round per person for the stage's
 * verb (shuffled), plus review rounds from earlier stages so old verbs stay
 * fresh — light spaced repetition, Duolingo-style.
 */
class BuildBattleRoundsUseCase @Inject constructor() {

    operator fun invoke(
        stage: ConjugationStage,
        random: Random = Random.Default,
    ): List<BattleRound> {
        val mainRounds = ConjugationPerson.entries
            .shuffled(random)
            .map { person -> round(verb = stage.verb, person = person, isReview = false, random = random) }

        val reviewRounds = ConjugationStages.all
            .filter { it.order < stage.order }
            .flatMap { earlier -> ConjugationPerson.entries.map { earlier.verb to it } }
            .shuffled(random)
            .take(REVIEW_ROUND_COUNT)
            .map { (verb, person) -> round(verb = verb, person = person, isReview = true, random = random) }

        return (mainRounds + reviewRounds).shuffled(random)
    }

    private fun round(
        verb: ConjugationVerb,
        person: ConjugationPerson,
        isReview: Boolean,
        random: Random,
    ): BattleRound {
        val correct = verb.form(person)
        val distractors = verb.forms.values
            .distinct()
            .filter { it != correct }
            .shuffled(random)
            .take(OPTION_COUNT - 1)
        val options = (distractors + correct).shuffled(random)
        require(options.size == OPTION_COUNT) {
            "${verb.infinitive} has too few distinct forms for $OPTION_COUNT options"
        }
        return BattleRound(
            verb = verb,
            person = person,
            options = options,
            isReview = isReview,
        )
    }

    companion object {
        const val OPTION_COUNT = 4
        const val REVIEW_ROUND_COUNT = 2
    }
}
