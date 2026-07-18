package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.conjugation.ConjugationPerson
import com.studybuddy.core.domain.model.conjugation.ConjugationTense
import kotlin.random.Random
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuildDrillSessionUseCaseTest {

    private val repository = FakeAtelierReviewRepository()
    private val useCase = BuildDrillSessionUseCase(GetAtelierSessionUseCase(repository))

    @Test
    fun `revision mode delegates to the Leitner session`() = runTest {
        val session = useCase(DrillMode.REVISION, ATELIER_TEST_PROFILE, ATELIER_TEST_NOW)

        assertEquals(GetAtelierSessionUseCase.SESSION_SIZE, session.size)
        // Fresh profile: introduction order starts with être in the présent.
        assertEquals("etre", session.first().verb.id)
        assertEquals(ConjugationTense.PRESENT, session.first().tense)
    }

    @Test
    fun `surprise mode is random but full-size and duplicate-free`() = runTest {
        val session = useCase(
            DrillMode.SURPRISE,
            ATELIER_TEST_PROFILE,
            ATELIER_TEST_NOW,
            random = Random(seed = 42),
        )

        assertEquals(GetAtelierSessionUseCase.SESSION_SIZE, session.size)
        val keys = session.map { Triple(it.verb.id, it.tense, it.person) }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `surprise mode differs between seeds`() = runTest {
        val first = useCase(DrillMode.SURPRISE, ATELIER_TEST_PROFILE, ATELIER_TEST_NOW, random = Random(1))
        val second = useCase(DrillMode.SURPRISE, ATELIER_TEST_PROFILE, ATELIER_TEST_NOW, random = Random(2))

        assertTrue(first != second)
    }

    @Test
    fun `cell mode drills the six persons of one verb and tense`() = runTest {
        val session = useCase(
            DrillMode.CELL,
            ATELIER_TEST_PROFILE,
            ATELIER_TEST_NOW,
            verbId = "chanter",
            tense = ConjugationTense.IMPARFAIT,
            random = Random(7),
        )

        assertEquals(ConjugationPerson.entries.size, session.size)
        assertTrue(session.all { it.verb.id == "chanter" && it.tense == ConjugationTense.IMPARFAIT })
        assertEquals(ConjugationPerson.entries.toSet(), session.map { it.person }.toSet())
    }

    @Test
    fun `cell mode requires a known verb`() {
        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                useCase(
                    DrillMode.CELL,
                    ATELIER_TEST_PROFILE,
                    ATELIER_TEST_NOW,
                    verbId = "licorne",
                    tense = ConjugationTense.PRESENT,
                )
            }
        }
    }
}
