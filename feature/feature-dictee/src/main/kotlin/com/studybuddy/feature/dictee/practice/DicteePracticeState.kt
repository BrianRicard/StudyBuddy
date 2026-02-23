package com.studybuddy.feature.dictee.practice

import com.google.mlkit.vision.digitalink.Ink
import com.studybuddy.core.domain.model.DicteeWord
import com.studybuddy.core.domain.model.Feedback
import com.studybuddy.core.domain.model.InputMode

data class DicteePracticeState(
    val listTitle: String = "",
    val isChallengeMode: Boolean = false,
    val words: List<DicteeWord> = emptyList(),
    val currentIndex: Int = 0,
    val userInput: String = "",
    val inputMode: InputMode = InputMode.KEYBOARD,
    val feedback: Feedback? = null,
    val sessionScore: Int = 0,
    val streak: Int = 0,
    val isPlaying: Boolean = false,
    val hintVisible: Boolean = false,
    val recognizedText: String? = null,
) {
    val currentWord: DicteeWord? get() = words.getOrNull(currentIndex)
    val progress: Float get() = if (words.isEmpty()) 0f else currentIndex.toFloat() / words.size
    val isComplete: Boolean get() = words.isNotEmpty() && currentIndex >= words.size
    val totalWords: Int get() = words.size
}

sealed interface DicteePracticeIntent {
    data object PlayWord : DicteePracticeIntent
    data object PlayWordSlow : DicteePracticeIntent
    data class UpdateInput(val text: String) : DicteePracticeIntent
    data object ToggleInputMode : DicteePracticeIntent
    data object ShowHint : DicteePracticeIntent
    data object CheckAnswer : DicteePracticeIntent
    data object NextWord : DicteePracticeIntent
    data object RetryWord : DicteePracticeIntent
    data class HandwritingRecognized(val text: String) : DicteePracticeIntent
    data class RecognizeInk(val ink: Ink) : DicteePracticeIntent
}

sealed interface DicteePracticeEffect {
    data class ShowPoints(val points: Int) : DicteePracticeEffect
    data object NavigateToResults : DicteePracticeEffect
}
