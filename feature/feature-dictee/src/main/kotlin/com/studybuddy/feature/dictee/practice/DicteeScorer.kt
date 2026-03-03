package com.studybuddy.feature.dictee.practice

import androidx.annotation.StringRes
import com.studybuddy.core.domain.model.InputMode
import com.studybuddy.core.ui.R as CoreUiR
import java.text.Normalizer

/**
 * Status of a single letter in the dictée scoring alignment.
 */
enum class LetterStatus {
    CORRECT,
    ACCENT_WRONG,
    WRONG,
    MISSING,
    EXTRA,
}

data class ScoredLetter(
    val character: Char?,
    val referenceCharacter: Char?,
    val status: LetterStatus,
)

data class DicteeWordScore(
    val referenceWord: String,
    val childAnswer: String,
    val scoredLetters: List<ScoredLetter>,
    val similarity: Float,
    val starRating: Int,
    val isCorrect: Boolean,
    @StringRes val encouragementResId: Int,
    val inputMode: InputMode,
)

/**
 * Scores a child's spelling attempt using character-level Levenshtein alignment.
 *
 * Handles French accent nuances: 'e' vs 'é' is ACCENT_WRONG (orange),
 * not fully WRONG. Case insensitive.
 */
object DicteeScorer {

    fun scoreWord(
        referenceWord: String,
        childAnswer: String,
        language: String,
        inputMode: InputMode = InputMode.KEYBOARD,
    ): DicteeWordScore {
        val normalizedRef = referenceWord.lowercase().trim()
        val normalizedAnswer = childAnswer.lowercase().trim()

        val scoredLetters = alignCharacters(normalizedRef, normalizedAnswer)
        val similarity = normalizedLevenshtein(normalizedRef, normalizedAnswer)
        val starRating = calculateStars(similarity)
        val encouragement = encouragementForStars(starRating)

        return DicteeWordScore(
            referenceWord = referenceWord,
            childAnswer = childAnswer,
            scoredLetters = scoredLetters,
            similarity = similarity,
            starRating = starRating,
            isCorrect = similarity >= 0.95f,
            encouragementResId = encouragement,
            inputMode = inputMode,
        )
    }

    /**
     * Character-level alignment using Levenshtein edit distance with backtracking.
     * Produces a list of ScoredLetter entries showing the optimal alignment.
     */
    internal fun alignCharacters(
        reference: String,
        answer: String,
    ): List<ScoredLetter> {
        val n = reference.length
        val m = answer.length

        // DP cost matrix
        val dp = Array(n + 1) { IntArray(m + 1) }
        for (i in 0..n) dp[i][0] = i
        for (j in 0..m) dp[0][j] = j

        for (i in 1..n) {
            for (j in 1..m) {
                val cost = if (reference[i - 1] == answer[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost,
                )
            }
        }

        // Backtrack to build alignment
        val result = mutableListOf<ScoredLetter>()
        var i = n
        var j = m

        while (i > 0 || j > 0) {
            when {
                i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] +
                    (if (reference[i - 1] == answer[j - 1]) 0 else 1) -> {
                    val refChar = reference[i - 1]
                    val ansChar = answer[j - 1]
                    val status = when {
                        refChar == ansChar -> LetterStatus.CORRECT
                        stripAccents(refChar.toString()) ==
                            stripAccents(ansChar.toString()) -> LetterStatus.ACCENT_WRONG
                        else -> LetterStatus.WRONG
                    }
                    result.add(
                        ScoredLetter(
                            character = ansChar,
                            referenceCharacter = refChar,
                            status = status,
                        ),
                    )
                    i--
                    j--
                }
                i > 0 && dp[i][j] == dp[i - 1][j] + 1 -> {
                    result.add(
                        ScoredLetter(
                            character = null,
                            referenceCharacter = reference[i - 1],
                            status = LetterStatus.MISSING,
                        ),
                    )
                    i--
                }
                else -> {
                    result.add(
                        ScoredLetter(
                            character = answer[j - 1],
                            referenceCharacter = null,
                            status = LetterStatus.EXTRA,
                        ),
                    )
                    j--
                }
            }
        }

        return result.reversed()
    }

    internal fun normalizedLevenshtein(
        a: String,
        b: String,
    ): Float {
        if (a.isEmpty() && b.isEmpty()) return 1f
        val maxLen = maxOf(a.length, b.length)
        if (maxLen == 0) return 1f
        val distance = levenshteinDistance(a, b)
        return (1f - distance.toFloat() / maxLen.toFloat()).coerceIn(0f, 1f)
    }

    private fun levenshteinDistance(
        a: String,
        b: String,
    ): Int {
        val n = a.length
        val m = b.length
        val dp = Array(n + 1) { IntArray(m + 1) }
        for (i in 0..n) dp[i][0] = i
        for (j in 0..m) dp[0][j] = j
        for (i in 1..n) {
            for (j in 1..m) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[n][m]
    }

    internal fun calculateStars(similarity: Float): Int {
        return when {
            similarity >= 0.95f -> 5
            similarity >= 0.80f -> 4
            similarity >= 0.60f -> 3
            similarity >= 0.35f -> 2
            else -> 1
        }
    }

    @StringRes
    internal fun encouragementForStars(stars: Int): Int {
        return when (stars) {
            5 -> CoreUiR.string.dictee_score_5_stars
            4 -> CoreUiR.string.dictee_score_4_stars
            3 -> CoreUiR.string.dictee_score_3_stars
            2 -> CoreUiR.string.dictee_score_2_stars
            else -> CoreUiR.string.dictee_score_1_star
        }
    }

    private fun stripAccents(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        return normalized.replace(Regex("[\\p{InCombiningDiacriticalMarks}]"), "")
    }
}
