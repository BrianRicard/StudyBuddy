package com.studybuddy.core.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PronunciationCheckerTest {

    @Test
    fun `exact match returns perfect score`() {
        val result = PronunciationChecker.checkLine("Le chat mange", "Le chat mange")
        assertEquals(1.0f, result.overallScore, 0.01f)
        assertTrue(result.passed)
        result.words.forEach {
            assertEquals(WordStatus.CORRECT, it.status)
        }
    }

    @Test
    fun `case-insensitive match returns perfect score`() {
        val result = PronunciationChecker.checkLine("Le Chat Mange", "le chat mange")
        assertEquals(1.0f, result.overallScore, 0.01f)
        assertTrue(result.passed)
    }

    @Test
    fun `accent-insensitive match returns perfect score`() {
        val result = PronunciationChecker.checkLine("café résumé", "cafe resume")
        assertEquals(1.0f, result.overallScore, 0.01f)
        assertTrue(result.passed)
    }

    @Test
    fun `empty expected returns passing`() {
        val result = PronunciationChecker.checkLine("", "anything")
        assertTrue(result.passed)
        assertEquals(1.0f, result.overallScore)
    }

    @Test
    fun `empty recognized scores zero for each word`() {
        val result = PronunciationChecker.checkLine("hello world", "")
        assertEquals(0.0f, result.overallScore, 0.01f)
        assertFalse(result.passed)
    }

    @Test
    fun `fewer recognized words pads missing with zero`() {
        val result = PronunciationChecker.checkLine("one two three", "one two")
        assertEquals(2, result.words.count { it.score > 0 })
        assertEquals(3, result.words.size)
        assertEquals(0.0f, result.words[2].score)
    }

    @Test
    fun `more recognized words uses best match`() {
        val result = PronunciationChecker.checkLine("chat", "le chat mange")
        // "chat" should match "chat" exactly from the recognized words
        assertEquals(1.0f, result.words[0].score, 0.01f)
    }

    @Test
    fun `close word gets CLOSE status`() {
        // "chatt" vs "chat" = 1 edit out of 5 = 0.80 -> CLOSE
        val result = PronunciationChecker.checkLine("chatt", "chat")
        assertEquals(WordStatus.CLOSE, result.words[0].status)
    }

    @Test
    fun `wrong word gets WRONG status`() {
        val result = PronunciationChecker.checkLine("abcdef", "xyz")
        assertEquals(WordStatus.WRONG, result.words[0].status)
    }

    @Test
    fun `punctuation is stripped`() {
        val result = PronunciationChecker.checkLine("Hello, world!", "hello world")
        assertEquals(1.0f, result.overallScore, 0.01f)
    }

    @ParameterizedTest
    @CsvSource(
        "chat, chat, 0",
        "chat, chats, 1",
        "'', abc, 3",
        "abc, '', 3",
        "kitten, sitting, 3",
    )
    fun `levenshtein distance computed correctly`(
        a: String,
        b: String,
        expected: Int,
    ) {
        assertEquals(expected, PronunciationChecker.levenshteinDistance(a, b))
    }

    @Test
    fun `line below threshold fails`() {
        // Completely wrong words should not pass
        val result = PronunciationChecker.checkLine("bonjour monde", "xyz abc")
        assertFalse(result.passed)
    }

    @Test
    fun `french poem line with minor errors passes`() {
        val result = PronunciationChecker.checkLine(
            "Maître Corbeau sur un arbre perché",
            "metre corbo sur un arbre perche",
        )
        // Most words close enough → should pass
        assertTrue(result.overallScore > 0.5f)
    }

    @Test
    fun `german umlaut handling`() {
        val result = PronunciationChecker.checkLine("Über allen Gipfeln", "uber allen gipfeln")
        assertEquals(1.0f, result.overallScore, 0.01f)
        assertTrue(result.passed)
    }
}
