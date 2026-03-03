package com.studybuddy.feature.dictee.practice

import androidx.annotation.StringRes
import com.google.mlkit.vision.digitalink.Ink
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.model.InputMode

/**
 * Session state machine — mirrors the Poems section flow rhythm.
 */
enum class DicteeSessionState {
    /** Word loaded, waiting for child to tap Listen. */
    IDLE,

    /** Audio is playing — speaker button pulses. */
    LISTENING,

    /** Child is typing or selecting tiles. */
    TYPING,

    /** Child is writing on the canvas. */
    HANDWRITING,

    /** Brief processing animation after Check is tapped. */
    CHECKING,

    /** Results displayed with letter-level feedback + stars. */
    SCORED,
}

data class DicteePracticeState(
    val listTitle: String = "",
    val isChallengeMode: Boolean = false,
    val words: List<DicteeWord> = emptyList(),
    val currentIndex: Int = 0,
    val sessionState: DicteeSessionState = DicteeSessionState.IDLE,
    val userInput: String = "",
    val inputMode: InputMode = InputMode.KEYBOARD,
    val feedback: DicteeWordScore? = null,
    val sessionResults: List<DicteeWordScore> = emptyList(),
    val sessionScore: Int = 0,
    val streak: Int = 0,
    val isPlaying: Boolean = false,
    val hasListenedAtLeastOnce: Boolean = false,
    val hintVisible: Boolean = false,
    val replayCount: Int = 0,

    // Handwriting-specific
    val recognizedText: String? = null,
    val recognitionPending: Boolean = false,
    @StringRes val recognitionErrorResId: Int? = null,

    // Letter tile-specific
    val letterTiles: List<LetterTile> = emptyList(),
    val answerSlots: List<Char?> = emptyList(),
) {
    val currentWord: DicteeWord? get() = words.getOrNull(currentIndex)
    val progress: Float get() = if (words.isEmpty()) 0f else currentIndex.toFloat() / words.size
    val isComplete: Boolean get() = words.isNotEmpty() && currentIndex >= words.size
    val totalWords: Int get() = words.size
    val isInputEnabled: Boolean get() = hasListenedAtLeastOnce &&
        sessionState != DicteeSessionState.CHECKING &&
        sessionState != DicteeSessionState.SCORED
    val averageStars: Int get() {
        if (sessionResults.isEmpty()) return 0
        val avg = sessionResults.map { it.starRating }.average()
        return kotlin.math.ceil(avg).toInt().coerceIn(1, 5)
    }
}

data class LetterTile(
    val letter: Char,
    val originalIndex: Int,
    val isUsed: Boolean = false,
)

sealed interface DicteePracticeIntent {
    data object PlayWord : DicteePracticeIntent
    data object PlayWordSlow : DicteePracticeIntent
    data class UpdateInput(val text: String) : DicteePracticeIntent
    data class SwitchInputMode(val mode: InputMode) : DicteePracticeIntent
    data object ShowHint : DicteePracticeIntent
    data object CheckAnswer : DicteePracticeIntent
    data object NextWord : DicteePracticeIntent
    data object RetryWord : DicteePracticeIntent
    data object SkipWord : DicteePracticeIntent
    data class HandwritingRecognized(val text: String) : DicteePracticeIntent
    data class RecognizeInk(val ink: Ink) : DicteePracticeIntent

    // Letter tile intents
    data class TapTile(val tileIndex: Int) : DicteePracticeIntent
    data class RemoveFromSlot(val slotIndex: Int) : DicteePracticeIntent
}

sealed interface DicteePracticeEffect {
    data class ShowPoints(val points: Int) : DicteePracticeEffect
    data object NavigateToResults : DicteePracticeEffect
}
