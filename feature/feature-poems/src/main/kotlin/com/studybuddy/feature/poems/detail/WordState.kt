package com.studybuddy.feature.poems.detail

/**
 * State of a single word after speech recognition scoring.
 */
enum class WordState {
    /** Not yet scored (before recording). */
    UNREAD,

    /** Child spoke the word correctly (similarity >= 0.70). */
    CORRECT,

    /** Child attempted but mispronounced (similarity >= 0.40). */
    INCORRECT,

    /** Could not understand what was said (similarity < 0.40). */
    UNCLEAR,

    /** Child did not reach this word. */
    SKIPPED,
}

/**
 * A single word in the poem with its position and scoring state.
 *
 * @property text The original word text from the poem.
 * @property lineIndex Which line of the poem this word belongs to.
 * @property wordIndex Position within the line.
 * @property globalIndex Position across the entire poem.
 * @property state Current scoring state.
 */
data class WordInfo(
    val text: String,
    val lineIndex: Int,
    val wordIndex: Int,
    val globalIndex: Int,
    val state: WordState = WordState.UNREAD,
)
