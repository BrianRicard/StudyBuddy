package com.studybuddy.feature.poems.detail

import androidx.annotation.StringRes
import com.studybuddy.core.common.PronunciationChecker
import com.studybuddy.core.ui.R as CoreUiR

/**
 * Result of scoring a child's poem recitation.
 *
 * @property scoredWords All words with their updated states.
 * @property overallAccuracy Fraction of words that are CORRECT, in [0.0, 1.0].
 * @property completeness Fraction of words that were attempted (not SKIPPED), in [0.0, 1.0].
 * @property starRating 1–5 star rating (never 0).
 * @property encouragementResId String resource for the encouragement message.
 */
data class PoemScore(
    val scoredWords: List<WordInfo>,
    val overallAccuracy: Float,
    val completeness: Float,
    val starRating: Int,
    @StringRes val encouragementResId: Int,
)

/**
 * Scores a child's recitation by comparing whisper transcription against the poem text.
 *
 * Uses word-level DP alignment to handle insertions/deletions gracefully.
 * Thresholds are generous because:
 * - Children mumble, pause, and mispronounce
 * - Whisper has higher error rates on children's speech
 * - Feedback must be encouraging, never punitive
 */
object PoemScorer {

    private const val CORRECT_THRESHOLD = 0.70f
    private const val INCORRECT_THRESHOLD = 0.40f

    private val FILLER_WORDS = setOf(
        "um", "uh", "euh", "eh", "ah", "oh", "hm", "hmm",
        "like", "well", "so", "then", "and", "uhm",
        "ben", "bah", "alors", "donc", "euh",
        "also", "na", "ja", "naja", "ähm",
    )

    private val SHORT_ARTICLES = setOf(
        "le", "la", "l", "les", "un", "une", "de", "du", "des", "au", "aux",
        "the", "a", "an",
        "der", "die", "das", "ein", "eine", "dem", "den", "des",
    )

    /**
     * Score the child's recitation against the poem words.
     *
     * @param words The poem words (with UNREAD state).
     * @param transcribedText The raw text from whisper transcription.
     * @return A [PoemScore] with word-level feedback and star rating.
     */
    fun score(
        words: List<WordInfo>,
        transcribedText: String,
    ): PoemScore {
        val spokenWords = transcribedText
            .split(WHITESPACE_REGEX)
            .filter { it.isNotBlank() }
            .map { it.trim() }
            .filterNot { it.lowercase() in FILLER_WORDS }

        if (spokenWords.isEmpty()) {
            val skippedWords = words.map { it.copy(state = WordState.SKIPPED) }
            return PoemScore(
                scoredWords = skippedWords,
                overallAccuracy = 0f,
                completeness = 0f,
                starRating = 1,
                encouragementResId = CoreUiR.string.poems_score_practice_more,
            )
        }

        val expectedTexts = words.map { it.text }
        val alignment = alignWords(expectedTexts, spokenWords)
        val scoredWords = scoreAlignment(words, spokenWords, alignment)

        val attempted = scoredWords.count { it.state != WordState.SKIPPED }
        val correct = scoredWords.count { it.state == WordState.CORRECT }
        val completeness = if (words.isNotEmpty()) attempted.toFloat() / words.size else 0f
        val accuracy = if (attempted > 0) correct.toFloat() / words.size else 0f

        val starRating = computeStars(accuracy)
        val encouragement = encouragementForStars(starRating)

        return PoemScore(
            scoredWords = scoredWords,
            overallAccuracy = accuracy,
            completeness = completeness,
            starRating = starRating,
            encouragementResId = encouragement,
        )
    }

    /**
     * DP word-level alignment: find best mapping from expected words to spoken words.
     * Returns an array where alignment[i] is the index into spokenWords that best matches
     * expectedWords[i], or -1 if no match found.
     */
    private fun alignWords(
        expected: List<String>,
        spoken: List<String>,
    ): IntArray {
        val n = expected.size
        val m = spoken.size

        // Cost matrix: dp[i][j] = minimum cost to align expected[0..i-1] with spoken[0..j-1]
        val dp = Array(n + 1) { FloatArray(m + 1) { Float.MAX_VALUE } }
        val from = Array(n + 1) { IntArray(m + 1) { -1 } } // track which spoken word matched

        dp[0][0] = 0f
        for (j in 1..m) dp[0][j] = 0f // Spoken words before first expected are free (child said extra)

        for (i in 1..n) {
            dp[i][0] = i.toFloat() // Skip cost = 1 per expected word

            for (j in 1..m) {
                // Option 1: skip expected word i (no match)
                val skipCost = dp[i - 1][j] + 1.0f
                if (skipCost < dp[i][j]) {
                    dp[i][j] = skipCost
                    from[i][j] = -1
                }

                // Option 2: skip spoken word j (extra word spoken)
                val extraCost = dp[i][j - 1]
                if (extraCost < dp[i][j]) {
                    dp[i][j] = extraCost
                    from[i][j] = from[i][j - 1]
                }

                // Option 3: match expected[i-1] with spoken[j-1]
                val sim = PronunciationChecker.similarity(expected[i - 1], spoken[j - 1])
                val matchCost = dp[i - 1][j - 1] + (1.0f - sim)
                if (matchCost < dp[i][j]) {
                    dp[i][j] = matchCost
                    from[i][j] = j - 1
                }
            }
        }

        // Backtrack to find best alignment
        val alignment = IntArray(n) { -1 }
        var j = m
        // Find best ending column
        var bestJ = 0
        var bestCost = dp[n][0]
        for (jj in 1..m) {
            if (dp[n][jj] <= bestCost) {
                bestCost = dp[n][jj]
                bestJ = jj
            }
        }
        j = bestJ

        // Trace back
        var i = n
        while (i > 0 && j >= 0) {
            val matchedJ = from[i][j]
            if (matchedJ >= 0 && (j == 0 || dp[i][j] != dp[i][j - 1])) {
                alignment[i - 1] = matchedJ
                i--
                j = matchedJ
            } else if (j > 0 && dp[i][j] == dp[i][j - 1]) {
                j-- // extra spoken word, skip
            } else {
                i-- // skipped expected word
                // j stays the same
            }
        }

        return alignment
    }

    private fun scoreAlignment(
        words: List<WordInfo>,
        spoken: List<String>,
        alignment: IntArray,
    ): List<WordInfo> {
        return words.mapIndexed { idx, word ->
            val matchedIdx = alignment[idx]
            if (matchedIdx < 0) {
                word.copy(state = WordState.SKIPPED)
            } else {
                val sim = PronunciationChecker.similarity(word.text, spoken[matchedIdx])

                // Short articles are auto-CORRECT if child spoke something in position
                val isArticle = word.text.lowercase() in SHORT_ARTICLES

                val state = when {
                    sim >= CORRECT_THRESHOLD -> WordState.CORRECT
                    isArticle && sim >= INCORRECT_THRESHOLD -> WordState.CORRECT
                    sim >= INCORRECT_THRESHOLD -> WordState.INCORRECT
                    else -> WordState.UNCLEAR
                }

                word.copy(state = state)
            }
        }
    }

    private fun computeStars(accuracy: Float): Int {
        return when {
            accuracy >= 0.90f -> 5
            accuracy >= 0.75f -> 4
            accuracy >= 0.60f -> 3
            accuracy >= 0.40f -> 2
            else -> 1
        }
    }

    @StringRes
    private fun encouragementForStars(stars: Int): Int {
        return when (stars) {
            5 -> CoreUiR.string.poems_score_excellent
            4 -> CoreUiR.string.poems_score_good
            3 -> CoreUiR.string.poems_score_keep_trying
            else -> CoreUiR.string.poems_score_practice_more
        }
    }

    private val WHITESPACE_REGEX = Regex("\\s+")
}
