package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationStages
import kotlin.random.Random
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.RepetitionInfo
import org.junit.jupiter.api.Test

class BuildBattleRoundsUseCaseTest {

    private val useCase = BuildBattleRoundsUseCase()

    @RepeatedTest(100)
    fun `rounds satisfy all constraints for every stage`(repetitionInfo: RepetitionInfo) {
        val random = Random(repetitionInfo.currentRepetition.toLong())

        ConjugationStages.all.forEach { stage ->
            val rounds = useCase(stage, random)
            val mainRounds = rounds.filterNot { it.isReview }
            val reviewRounds = rounds.filter { it.isReview }

            // Exactly one main round per person, all for the stage verb.
            assertEquals(ConjugationPerson.entries.toSet(), mainRounds.map { it.person }.toSet())
            assertTrue(mainRounds.all { it.verb.id == stage.verb.id })

            // Review rounds drill earlier verbs only; stage 1 has none.
            val expectedReviews = if (stage.order == 1) 0 else BuildBattleRoundsUseCase.REVIEW_ROUND_COUNT
            assertEquals(expectedReviews, reviewRounds.size)
            val earlierVerbIds = ConjugationStages.all.filter { it.order < stage.order }.map { it.id }
            assertTrue(reviewRounds.all { it.verb.id in earlierVerbIds })

            rounds.forEach { round ->
                assertEquals(BuildBattleRoundsUseCase.OPTION_COUNT, round.options.size)
                assertEquals(round.options.size, round.options.toSet().size, "options must be unique")
                assertTrue(round.correctForm in round.options, "correct form must be an option")
            }
        }
    }

    @Test
    fun `same seed produces the same rounds`() {
        val stage = ConjugationStages.all.last()
        assertEquals(useCase(stage, Random(42)), useCase(stage, Random(42)))
    }
}
