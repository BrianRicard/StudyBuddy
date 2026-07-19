package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import com.studybuddy.core.domain.model.conjugation.FrenchVerbs
import com.studybuddy.core.domain.model.srs.LeitnerGrowth
import com.studybuddy.core.domain.model.srs.LeitnerSchedule
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class GetAtelierGardenUseCaseTest {

    private val repository = FakeAtelierReviewRepository()
    private val useCase = GetAtelierGardenUseCase(repository)

    @ParameterizedTest
    @CsvSource(
        "'0,0,0,0,0,0', SEED",
        "'1,0,0,0,0,0', SPROUT",
        "'1,1,1,1,1,1', SPROUT",
        "'2,2,2,2,2,2', FLOWER",
        "'4,4,4,4,3,3', FLOWER",
        "'4,4,4,4,4,4', TREE",
    )
    fun `growth stages derive from the six person boxes`(
        boxesCsv: String,
        expected: LeitnerGrowth,
    ) {
        assertEquals(expected, LeitnerGrowth.fromBoxes(boxesCsv.split(',').map { it.toInt() }))
    }

    @Test
    fun `an untouched garden is all seeds with nothing due`() = runTest {
        val garden = useCase(ATELIER_TEST_PROFILE, ATELIER_TEST_NOW).first()

        assertEquals(0, garden.dueCardCount)
        assertEquals(0, garden.dueVerbCount)
        assertEquals(FrenchVerbs.all.size, garden.verbs.size)
        assertTrue(
            garden.verbs.all { row ->
                ConjugationTense.entries.all { row.growth[it] == LeitnerGrowth.SEED }
            },
        )
    }

    @Test
    fun `growth is per verb and tense, other cells stay seeds`() = runTest {
        repository.reviews = ConjugationPerson.entries.map { person ->
            atelierReview("etre", person = person, box = LeitnerSchedule.MAX_BOX, dueAt = ATELIER_TEST_NOW + 5.days)
        }

        val garden = useCase(ATELIER_TEST_PROFILE, ATELIER_TEST_NOW).first()
        val etre = garden.verbs.first { it.verb.id == "etre" }

        assertEquals(LeitnerGrowth.TREE, etre.growth[ConjugationTense.PRESENT])
        assertEquals(LeitnerGrowth.SEED, etre.growth[ConjugationTense.FUTUR])
        assertEquals(
            LeitnerGrowth.SEED,
            garden.verbs.first { it.verb.id == "avoir" }.growth[ConjugationTense.PRESENT],
        )
    }

    @Test
    fun `due counts count cards and distinct verbs`() = runTest {
        repository.reviews = listOf(
            atelierReview("etre", person = ConjugationPerson.JE),
            atelierReview("etre", person = ConjugationPerson.TU),
            atelierReview("avoir", person = ConjugationPerson.JE),
            // Not due yet — must not be counted.
            atelierReview("aimer", person = ConjugationPerson.JE, dueAt = ATELIER_TEST_NOW + 3.days),
        )

        val garden = useCase(ATELIER_TEST_PROFILE, ATELIER_TEST_NOW).first()

        assertEquals(3, garden.dueCardCount)
        assertEquals(2, garden.dueVerbCount)
    }

    @Test
    fun `unknown verbs are ignored everywhere`() = runTest {
        repository.reviews = listOf(atelierReview("licorne", person = ConjugationPerson.JE))

        val garden = useCase(ATELIER_TEST_PROFILE, ATELIER_TEST_NOW).first()

        assertEquals(0, garden.dueCardCount)
        assertEquals(0, garden.dueVerbCount)
        assertTrue(garden.verbs.none { it.verb.id == "licorne" })
    }

    @Test
    fun `verbs keep roster order`() = runTest {
        val garden = useCase(ATELIER_TEST_PROFILE, ATELIER_TEST_NOW).first()

        assertEquals(FrenchVerbs.all.map { it.id }, garden.verbs.map { it.verb.id })
    }
}
