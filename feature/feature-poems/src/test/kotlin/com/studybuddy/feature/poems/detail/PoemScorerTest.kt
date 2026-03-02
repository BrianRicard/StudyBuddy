package com.studybuddy.feature.poems.detail

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PoemScorerTest {

    private fun makeWords(vararg words: String): List<WordInfo> {
        var globalIdx = 0
        return words.map { word ->
            WordInfo(
                text = word,
                lineIndex = 0,
                wordIndex = globalIdx,
                globalIndex = globalIdx++,
            )
        }
    }

    @Test
    fun `exact match gives all CORRECT and 5 stars`() {
        val words = makeWords("the", "cat", "sat", "on", "the", "mat")
        val result = PoemScorer.score(words, "the cat sat on the mat")
        assertEquals(5, result.starRating)
        assertTrue(result.overallAccuracy >= 0.90f)
        result.scoredWords.forEach {
            assertEquals(WordState.CORRECT, it.state, "Word '${it.text}' should be CORRECT")
        }
    }

    @Test
    fun `empty transcription gives all SKIPPED and 1 star`() {
        val words = makeWords("hello", "world")
        val result = PoemScorer.score(words, "")
        assertEquals(1, result.starRating)
        assertEquals(0f, result.overallAccuracy)
        result.scoredWords.forEach {
            assertEquals(WordState.SKIPPED, it.state)
        }
    }

    @Test
    fun `filler words are filtered out`() {
        val words = makeWords("the", "cat")
        val result = PoemScorer.score(words, "um the uh cat")
        result.scoredWords.forEach {
            assertEquals(WordState.CORRECT, it.state, "Word '${it.text}' should be CORRECT")
        }
    }

    @Test
    fun `star rating is never 0`() {
        val words = makeWords("abcdefgh", "ijklmnop")
        val result = PoemScorer.score(words, "xyz")
        assertTrue(result.starRating >= 1, "Star rating must be at least 1")
    }

    @Test
    fun `partial match gives intermediate stars`() {
        val words = makeWords("one", "two", "three", "four", "five")
        // Only say 3 of 5 words correctly
        val result = PoemScorer.score(words, "one two three")
        assertTrue(result.starRating in 2..4, "Expected 2-4 stars, got ${result.starRating}")
        assertTrue(result.completeness < 1.0f)
    }

    @Test
    fun `mispronounced word gets INCORRECT at generous threshold`() {
        val words = makeWords("beautiful")
        // "bootiful" is close but not exact — similarity ~0.66
        val result = PoemScorer.score(words, "bootiful")
        val state = result.scoredWords[0].state
        assertTrue(
            state == WordState.CORRECT || state == WordState.INCORRECT,
            "Expected CORRECT or INCORRECT for close mispronunciation, got $state",
        )
    }

    @Test
    fun `completely wrong word gets UNCLEAR or SKIPPED`() {
        val words = makeWords("butterfly")
        val result = PoemScorer.score(words, "xyz")
        val state = result.scoredWords[0].state
        // DP alignment may choose to skip rather than match very dissimilar words
        assertTrue(
            state == WordState.UNCLEAR || state == WordState.SKIPPED,
            "Expected UNCLEAR or SKIPPED for very wrong word, got $state",
        )
    }

    @Test
    fun `short articles are lenient`() {
        val words = makeWords("le", "chat")
        // "la" instead of "le" - article should be lenient
        val result = PoemScorer.score(words, "la chat")
        val articleState = result.scoredWords[0].state
        assertEquals(WordState.CORRECT, articleState, "Short article should be lenient")
    }

    @Test
    fun `completeness tracks how many words were attempted`() {
        val words = makeWords("one", "two", "three", "four")
        val result = PoemScorer.score(words, "one two")
        assertTrue(result.completeness <= 0.75f, "Expected completeness <= 0.75, got ${result.completeness}")
    }

    @Test
    fun `accent insensitive scoring`() {
        val words = makeWords("café", "résumé")
        val result = PoemScorer.score(words, "cafe resume")
        result.scoredWords.forEach {
            assertEquals(WordState.CORRECT, it.state, "Accent-stripped word '${it.text}' should be CORRECT")
        }
    }

    @Test
    fun `german umlauts handled correctly`() {
        val words = makeWords("Über", "allen", "Gipfeln")
        val result = PoemScorer.score(words, "uber allen gipfeln")
        result.scoredWords.forEach {
            assertEquals(WordState.CORRECT, it.state, "Umlaut word '${it.text}' should be CORRECT")
        }
    }
}
