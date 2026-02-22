package com.studybuddy.core.domain.usecase.dictee

import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.repository.DicteeRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetMixedPracticeWordsUseCaseTest {

    private val repository: DicteeRepository = mockk()
    private val useCase = GetMixedPracticeWordsUseCase(repository)

    private fun word(
        id: String,
        word: String,
        listId: String,
        attempts: Int = 0,
        correctCount: Int = 0,
    ) = DicteeWord(
        id = id,
        listId = listId,
        word = word,
        mastered = false,
        attempts = attempts,
        correctCount = correctCount,
        lastAttemptAt = Instant.fromEpochMilliseconds(0),
    )

    @Test
    fun `returns words from multiple lists combined`() = runTest {
        val words = listOf(
            word("w1", "chat", "list1"),
            word("w2", "chien", "list1"),
            word("w3", "rouge", "list2"),
            word("w4", "bleu", "list2"),
        )
        every { repository.getWordsForLists(listOf("list1", "list2")) } returns flowOf(words)

        val result = useCase(listOf("list1", "list2")).first()

        assertEquals(4, result.size)
        assertTrue(result.any { it.listId == "list1" })
        assertTrue(result.any { it.listId == "list2" })
    }

    @Test
    fun `sorts words by mastery ratio ascending — weakest first`() = runTest {
        val words = listOf(
            // 100%
            word("w1", "perfect", "list1", attempts = 10, correctCount = 10),
            // 0 attempts → 0.0
            word("w2", "never_tried", "list1"),
            // 50%
            word("w3", "half", "list1", attempts = 4, correctCount = 2),
            // 80%
            word("w4", "almost", "list1", attempts = 10, correctCount = 8),
        )
        every { repository.getWordsForLists(listOf("list1")) } returns flowOf(words)

        val result = useCase(listOf("list1")).first()

        // never_tried (0.0) → half (0.5) → almost (0.8) → perfect (1.0)
        assertEquals("never_tried", result[0].word)
        assertEquals("half", result[1].word)
        assertEquals("almost", result[2].word)
        assertEquals("perfect", result[3].word)
    }

    @Test
    fun `words with zero attempts sort first`() = runTest {
        val words = listOf(
            word("w1", "experienced", "list1", attempts = 5, correctCount = 4),
            word("w2", "untouched", "list2"),
        )
        every { repository.getWordsForLists(listOf("list1", "list2")) } returns flowOf(words)

        val result = useCase(listOf("list1", "list2")).first()

        assertEquals("untouched", result[0].word)
    }

    @Test
    fun `returns empty list when no words in lists`() = runTest {
        every { repository.getWordsForLists(listOf("list1", "list2")) } returns flowOf(emptyList())

        val result = useCase(listOf("list1", "list2")).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `works with single list id`() = runTest {
        val words = listOf(word("w1", "maison", "list1"))
        every { repository.getWordsForLists(listOf("list1")) } returns flowOf(words)

        val result = useCase(listOf("list1")).first()

        assertEquals(1, result.size)
        assertEquals("maison", result[0].word)
    }
}
