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
            .map { person -> round(stage.verb, person, isReview = false, random) }

        val earlierVerbs = ConjugationStages.all
            .filter { it.order < stage.order }
            .map { it.verb }

        val reviewRounds = if (earlierVerbs.isEmpty()) {
            emptyList()
        } else {
            List(REVIEW_ROUND_COUNT) {
                val verb = earlierVerbs.random(random)
                val person = ConjugationPerson.entries.random(random)
                round(verb, person, isReview = true, random)
            }
        }

        return mainRounds + reviewRounds
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
        return BattleRound(
            verb = verb,
            person = person,
            options = (distractors + correct).shuffled(random),
            isReview = isReview,
        )
    }

    companion object {
        const val OPTION_COUNT = 4
        const val REVIEW_ROUND_COUNT = 2
    }
}
