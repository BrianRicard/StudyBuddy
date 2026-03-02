package com.studybuddy.core.common

import com.studybuddy.core.common.extensions.stripAccents
import kotlin.math.max

/**
 * Status classification for a single recognized word compared to its expected counterpart.
 */
enum class WordStatus {
    /** The recognized word is a close or exact match (score >= 0.85). */
    CORRECT,

    /** The recognized word is partially correct (0.60 <= score < 0.85). */
    CLOSE,

    /** The recognized word is too far from the expected word (score < 0.60). */
    WRONG,
}

/**
 * Result of comparing a single expected word against what was recognized.
 *
 * @property expected The word that was expected (original, un-normalized form).
 * @property recognized The word that was recognized (original, un-normalized form).
 * @property score Similarity score in the range [0.0, 1.0], where 1.0 is an exact match.
 * @property status Classification of how close the recognized word is to the expected word.
 */
data class WordResult(
    val expected: String,
    val recognized: String,
    val score: Float,
    val status: WordStatus,
)

/**
 * Result of comparing an entire line (sentence) of expected text against recognized text.
 *
 * @property words Per-word comparison results, one for each expected word.
 * @property overallScore Average of all per-word scores, in the range [0.0, 1.0].
 * @property passed Whether the overall score meets or exceeds [PronunciationChecker.LINE_PASS_THRESHOLD].
 */
data class LineResult(
    val words: List<WordResult>,
    val overallScore: Float,
    val passed: Boolean,
)

/**
 * Utility for comparing recognized speech or handwriting against expected text.
 *
 * Uses Levenshtein (edit) distance to compute per-word similarity scores, then
 * classifies each word and determines whether the full line passes a configurable
 * threshold. Both strings are normalized before comparison: accents are stripped,
 * punctuation is removed, and whitespace is trimmed.
 *
 * Usage:
 * ```kotlin
 * val result = PronunciationChecker.checkLine(
 *     expected = "Le chat mange",
 *     recognized = "le sha manj",
 * )
 * result.words.forEach { println("${it.expected} -> ${it.status}") }
 * println("Passed: ${result.passed}")
 * ```
 */
object PronunciationChecker {

    /** Minimum overall score for a line to be considered passing. */
    const val LINE_PASS_THRESHOLD = 0.80f

    /** Minimum per-word score to classify as [WordStatus.CORRECT]. */
    private const val WORD_CORRECT_THRESHOLD = 0.85f

    /** Minimum per-word score to classify as [WordStatus.CLOSE]. */
    private const val WORD_CLOSE_THRESHOLD = 0.60f

    /**
     * Compares a full line of expected text against recognized text.
     *
     * Both strings are normalized, then split into words. Words are aligned
     * positionally. If the recognized text has fewer words than expected, the
     * missing words receive a score of 0.0. If the recognized text has more
     * words, a best-match strategy is used to find the closest recognized word
     * for each expected word.
     *
     * @param expected The correct text (e.g., from a dictee word list).
     * @param recognized The text produced by speech recognition or handwriting.
     * @return A [LineResult] containing per-word scores and an overall pass/fail.
     */
    fun checkLine(
        expected: String,
        recognized: String,
    ): LineResult {
        val expectedWords = normalizeForComparison(expected).split(WHITESPACE_REGEX).filter { it.isNotEmpty() }
        val recognizedWords = normalizeForComparison(recognized).split(WHITESPACE_REGEX).filter { it.isNotEmpty() }

        // Preserve original words (split the raw inputs) for display purposes.
        val originalExpected = expected.trim().split(WHITESPACE_REGEX).filter { it.isNotEmpty() }
        val originalRecognized = recognized.trim().split(WHITESPACE_REGEX).filter { it.isNotEmpty() }

        if (expectedWords.isEmpty()) {
            return LineResult(words = emptyList(), overallScore = 1.0f, passed = true)
        }

        val wordResults = if (recognizedWords.size <= expectedWords.size) {
            // Fewer or equal recognized words: align positionally, pad missing with empty.
            expectedWords.mapIndexed { index, expWord ->
                val recWord = recognizedWords.getOrElse(index) { "" }
                val origExp = originalExpected.getOrElse(index) { expWord }
                val origRec = originalRecognized.getOrElse(index) { "" }
                val score = scoreWord(expWord, recWord)
                WordResult(
                    expected = origExp,
                    recognized = origRec,
                    score = score,
                    status = classifyWord(score),
                )
            }
        } else {
            // More recognized words than expected: find the best match for each expected word.
            val used = BooleanArray(recognizedWords.size)
            expectedWords.mapIndexed { index, expWord ->
                val origExp = originalExpected.getOrElse(index) { expWord }
                var bestScore = -1f
                var bestIdx = -1

                for (i in recognizedWords.indices) {
                    if (used[i]) continue
                    val s = scoreWord(expWord, recognizedWords[i])
                    if (s > bestScore) {
                        bestScore = s
                        bestIdx = i
                    }
                }

                if (bestIdx >= 0) used[bestIdx] = true
                val origRec = if (bestIdx >= 0) {
                    originalRecognized.getOrElse(bestIdx) { recognizedWords.getOrElse(bestIdx) { "" } }
                } else {
                    ""
                }
                val score = if (bestIdx >= 0) bestScore else 0f

                WordResult(
                    expected = origExp,
                    recognized = origRec,
                    score = score,
                    status = classifyWord(score),
                )
            }
        }

        val overallScore = if (wordResults.isNotEmpty()) {
            wordResults.map { it.score }.average().toFloat()
        } else {
            1.0f
        }

        return LineResult(
            words = wordResults,
            overallScore = overallScore,
            passed = overallScore >= LINE_PASS_THRESHOLD,
        )
    }

    /**
     * Computes the Levenshtein (edit) distance between two strings.
     *
     * The Levenshtein distance is the minimum number of single-character edits
     * (insertions, deletions, or substitutions) required to transform one string
     * into the other.
     *
     * Uses an optimized single-row dynamic programming approach with O(min(m, n))
     * space complexity and O(m * n) time complexity.
     *
     * @param a The first string.
     * @param b The second string.
     * @return The edit distance (0 if the strings are identical).
     */
    fun levenshteinDistance(
        a: String,
        b: String,
    ): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        // Ensure we iterate over the longer string in the outer loop
        // and keep the shorter string for the DP row, saving memory.
        val (shorter, longer) = if (a.length <= b.length) a to b else b to a
        val shortLen = shorter.length
        val longLen = longer.length

        // Previous row of distances (only need one row at a time).
        var previousRow = IntArray(shortLen + 1) { it }

        for (i in 1..longLen) {
            val currentRow = IntArray(shortLen + 1)
            currentRow[0] = i

            for (j in 1..shortLen) {
                val cost = if (longer[i - 1] == shorter[j - 1]) 0 else 1
                currentRow[j] = minOf(
                    // insertion
                    currentRow[j - 1] + 1,
                    // deletion
                    previousRow[j] + 1,
                    // substitution
                    previousRow[j - 1] + cost,
                )
            }

            previousRow = currentRow
        }

        return previousRow[shortLen]
    }

    /**
     * Normalizes text for comparison by stripping accents, removing punctuation,
     * and collapsing whitespace.
     *
     * @param text The raw input text.
     * @return A lowercase string with only letters, digits, and single spaces.
     */
    private fun normalizeForComparison(text: String): String {
        return text
            .stripAccents()
            .replace(PUNCTUATION_REGEX, "")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
    }

    /**
     * Scores how similar the recognized word is to the expected word.
     *
     * The score is computed as `1.0 - (editDistance / maxLength)`, clamped to [0.0, 1.0].
     * Two empty strings yield a perfect score of 1.0.
     *
     * @param expected The normalized expected word.
     * @param recognized The normalized recognized word.
     * @return A similarity score in [0.0, 1.0].
     */
    private fun scoreWord(
        expected: String,
        recognized: String,
    ): Float {
        if (expected.isEmpty() && recognized.isEmpty()) return 1.0f
        val maxLen = max(expected.length, recognized.length)
        if (maxLen == 0) return 1.0f
        val distance = levenshteinDistance(expected, recognized)
        return (1.0f - (distance.toFloat() / maxLen.toFloat())).coerceIn(0.0f, 1.0f)
    }

    /**
     * Classifies a word similarity score into a [WordStatus].
     *
     * @param score A similarity score in [0.0, 1.0].
     * @return [WordStatus.CORRECT] if score >= 0.85,
     *         [WordStatus.CLOSE] if score >= 0.60,
     *         [WordStatus.WRONG] otherwise.
     */
    private fun classifyWord(score: Float): WordStatus {
        return when {
            score >= WORD_CORRECT_THRESHOLD -> WordStatus.CORRECT
            score >= WORD_CLOSE_THRESHOLD -> WordStatus.CLOSE
            else -> WordStatus.WRONG
        }
    }

    private val WHITESPACE_REGEX = Regex("\\s+")
    private val PUNCTUATION_REGEX = Regex("[^\\p{L}\\p{N}\\s]")
    private val MULTI_SPACE_REGEX = Regex("\\s{2,}")
}
