package com.studybuddy.core.domain.usecase.dictee

import com.studybuddy.core.common.extensions.matchesWord
import com.studybuddy.core.domain.model.Feedback
import javax.inject.Inject

class CheckSpellingUseCase @Inject constructor() {
    operator fun invoke(
        userInput: String,
        correctWord: String,
        accentStrict: Boolean,
    ): Feedback = if (userInput.matchesWord(correctWord, strict = accentStrict)) {
        Feedback.Correct
    } else {
        Feedback.Incorrect(correctAnswer = correctWord)
    }
}
