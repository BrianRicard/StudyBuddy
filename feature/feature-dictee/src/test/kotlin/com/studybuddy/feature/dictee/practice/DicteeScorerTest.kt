package com.studybuddy.feature.dictee.practice

import com.studybuddy.core.domain.model.InputMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class DicteeScorerTest {

    @Test
    fun `perfect match returns 5 stars and isCorrect true`() {
        val result = DicteeScorer.scoreWord("maison", "maison", "fr")
        assertEquals(5, result.starRating)
        assertTrue(result.isCorrect)
        assertEquals(1f, result.similarity)
    }

    @Test
    fun `case insensitive match is still correct`() {
        val result = DicteeScorer.scoreWord("Chat", "chat", "fr")
        assertEquals(5, result.starRating)
        assertTrue(result.isCorrect)
    }

    @Test
    fun `one letter off returns 4 stars`() {
        val result = DicteeScorer.scoreWord("maison", "maisen", "fr")
        assertEquals(4, result.starRating)
        assertFalse(result.isCorrect)
    }

    @Test
    fun `accent difference marked as ACCENT_WRONG`() {
        val letters = DicteeScorer.alignCharacters("école", "ecole")
        val accentLetters = letters.filter { it.status == LetterStatus.ACCENT_WRONG }
        assertTrue(accentLetters.isNotEmpty())
    }

    @Test
    fun `missing letter marked as MISSING`() {
        val letters = DicteeScorer.alignCharacters("chat", "cha")
        val missingLetters = letters.filter { it.status == LetterStatus.MISSING }
        assertEquals(1, missingLetters.size)
        assertEquals('t', missingLetters[0].referenceCharacter)
    }

    @Test
    fun `extra letter marked as EXTRA`() {
        val letters = DicteeScorer.alignCharacters("chat", "chats")
        val extraLetters = letters.filter { it.status == LetterStatus.EXTRA }
        assertEquals(1, extraLetters.size)
        assertEquals('s', extraLetters[0].character)
    }

    @Test
    fun `completely wrong word returns 1 star`() {
        val result = DicteeScorer.scoreWord("papillon", "xyz", "fr")
        assertEquals(1, result.starRating)
        assertFalse(result.isCorrect)
    }

    @Test
    fun `empty answer returns 1 star`() {
        val result = DicteeScorer.scoreWord("maison", "", "fr")
        assertEquals(1, result.starRating)
        assertFalse(result.isCorrect)
    }

    @ParameterizedTest
    @CsvSource(
        "1.0, 5",
        "0.95, 5",
        "0.80, 4",
        "0.60, 3",
        "0.35, 2",
        "0.10, 1",
        "0.0, 1",
    )
    fun `calculateStars returns correct star count`(
        similarity: Float,
        expectedStars: Int,
    ) {
        assertEquals(expectedStars, DicteeScorer.calculateStars(similarity))
    }

    @Test
    fun `normalizedLevenshtein returns 1 for identical strings`() {
        assertEquals(1f, DicteeScorer.normalizedLevenshtein("abc", "abc"))
    }

    @Test
    fun `normalizedLevenshtein returns 0 for completely different strings`() {
        assertEquals(0f, DicteeScorer.normalizedLevenshtein("abc", "xyz"))
    }

    @Test
    fun `normalizedLevenshtein handles empty strings`() {
        assertEquals(1f, DicteeScorer.normalizedLevenshtein("", ""))
        assertEquals(0f, DicteeScorer.normalizedLevenshtein("abc", ""))
    }

    @Test
    fun `inputMode is preserved in score result`() {
        val result = DicteeScorer.scoreWord("chat", "chat", "fr", InputMode.HANDWRITING)
        assertEquals(InputMode.HANDWRITING, result.inputMode)
    }

    @Test
    fun `alignment handles papillon with missing l`() {
        val letters = DicteeScorer.alignCharacters("papillon", "papiyon")
        // Should have some WRONG and/or MISSING letters
        val nonCorrect = letters.filter { it.status != LetterStatus.CORRECT }
        assertTrue(nonCorrect.isNotEmpty())
    }

    @Test
    fun `whitespace is trimmed before scoring`() {
        val result = DicteeScorer.scoreWord("chat", "  chat  ", "fr")
        assertTrue(result.isCorrect)
    }
}
