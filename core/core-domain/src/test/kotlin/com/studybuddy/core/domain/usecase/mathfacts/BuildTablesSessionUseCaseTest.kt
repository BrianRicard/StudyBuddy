package com.studybuddy.core.domain.usecase.mathfacts

import com.studybuddy.core.domain.model.mathfacts.MathFact
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuildTablesSessionUseCaseTest {

    private val repository = FakeMathFactsReviewRepository()
    private val useCase = BuildTablesSessionUseCase(repository)

    @Test
    fun `a brand-new profile starts with the table de 2 in recited order`() = runTest {
        val session = useCase(TablesMode.REVISION, TABLES_TEST_PROFILE, TABLES_TEST_NOW)

        assertEquals(BuildTablesSessionUseCase.SESSION_SIZE, session.size)
        assertTrue(session.all { it.isNew && it.fact.table == 2 })
        assertEquals((1..10).toList(), session.map { it.fact.multiplicand })
    }

    @Test
    fun `due cards come first, most overdue first`() = runTest {
        repository.reviews = listOf(
            factReview(3, 4, dueAt = TABLES_TEST_NOW - 1.days),
            factReview(5, 6, dueAt = TABLES_TEST_NOW - 3.days),
            factReview(2, 9, dueAt = TABLES_TEST_NOW - 2.days),
        )

        val session = useCase(TablesMode.REVISION, TABLES_TEST_PROFILE, TABLES_TEST_NOW)

        assertEquals(
            listOf(MathFact(5, 6), MathFact(2, 9), MathFact(3, 4)),
            session.take(3).map { it.fact },
        )
        assertTrue(session.take(3).none { it.isNew })
        // The rest of the session is filled with new cards.
        assertTrue(session.drop(3).all { it.isNew })
    }

    @Test
    fun `reviewed cards are not reintroduced as new`() = runTest {
        repository.reviews = listOf(factReview(2, 1, dueAt = TABLES_TEST_NOW + 5.days))

        val session = useCase(TablesMode.REVISION, TABLES_TEST_PROFILE, TABLES_TEST_NOW)

        assertTrue(session.filter { it.fact == MathFact(2, 1) }.none { it.isNew })
    }

    @Test
    fun `when everything is reviewed, upcoming cards fill the session soonest first`() = runTest {
        var offset = 0
        repository.reviews = com.studybuddy.core.domain.model.mathfacts.MathFactsRoster.all.map { fact ->
            offset += 1
            factReview(fact.table, fact.multiplicand, dueAt = TABLES_TEST_NOW + offset.hours)
        }

        val session = useCase(TablesMode.REVISION, TABLES_TEST_PROFILE, TABLES_TEST_NOW)

        assertEquals(BuildTablesSessionUseCase.SESSION_SIZE, session.size)
        assertTrue(session.none { it.isNew })
        assertEquals(List(10) { 2 }, session.map { it.fact.table })
    }

    @Test
    fun `rows outside the roster are ignored`() = runTest {
        repository.reviews = listOf(factReview(13, 5, dueAt = TABLES_TEST_NOW - 1.days))

        val session = useCase(TablesMode.REVISION, TABLES_TEST_PROFILE, TABLES_TEST_NOW)

        assertTrue(session.none { it.fact.table == 13 })
        assertEquals(BuildTablesSessionUseCase.SESSION_SIZE, session.size)
    }

    @Test
    fun `another profile's reviews are invisible`() = runTest {
        repository.reviews = listOf(
            factReview(5, 5, dueAt = TABLES_TEST_NOW - 1.days, profileId = "someone-else"),
        )

        val session = useCase(TablesMode.REVISION, TABLES_TEST_PROFILE, TABLES_TEST_NOW)

        assertTrue(session.all { it.isNew })
    }

    @Test
    fun `table mode drills the ten facts of one table`() = runTest {
        val session = useCase(
            TablesMode.TABLE,
            TABLES_TEST_PROFILE,
            TABLES_TEST_NOW,
            table = 7,
            random = Random(7),
        )

        assertEquals(10, session.size)
        assertTrue(session.all { it.fact.table == 7 })
        assertEquals((1..10).toSet(), session.map { it.fact.multiplicand }.toSet())
    }

    @Test
    fun `table mode requires a table in the garden`() {
        assertThrows(IllegalArgumentException::class.java) {
            runTest { useCase(TablesMode.TABLE, TABLES_TEST_PROFILE, TABLES_TEST_NOW, table = 12) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runTest { useCase(TablesMode.TABLE, TABLES_TEST_PROFILE, TABLES_TEST_NOW, table = null) }
        }
    }

    @Test
    fun `surprise mode is random but full-size and duplicate-free`() = runTest {
        val first = useCase(TablesMode.SURPRISE, TABLES_TEST_PROFILE, TABLES_TEST_NOW, random = Random(1))
        val second = useCase(TablesMode.SURPRISE, TABLES_TEST_PROFILE, TABLES_TEST_NOW, random = Random(2))

        assertEquals(BuildTablesSessionUseCase.SESSION_SIZE, first.size)
        assertEquals(first.map { it.fact }.toSet().size, first.size)
        assertTrue(first != second)
    }
}
