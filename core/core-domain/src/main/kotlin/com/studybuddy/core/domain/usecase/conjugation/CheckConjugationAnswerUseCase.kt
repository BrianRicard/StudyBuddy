package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.common.extensions.matchesWord
import com.studybuddy.core.domain.model.Feedback
import javax.inject.Inject

/**
 * Grades a typed conjugation form. Accent-lenient by default ("etes" passes
 * for "êtes") because the write step is about memorizing the forms, not
 * keyboard gymnastics — a hint shows the accented spelling either way.
 *
 * Kept separate from dictée's CheckSpellingUseCase (same logic today) so
 * conjugation grading can diverge — e.g. accepting "j'aime" for "aime" —
 * without touching dictée.
 */
class CheckConjugationAnswerUseCase @Inject constructor() {
    operator fun invoke(
        userInput: String,
        correctForm: String,
        accentStrict: Boolean = false,
    ): Feedback = if (userInput.matchesWord(correctForm, strict = accentStrict)) {
        Feedback.Correct
    } else {
        Feedback.Incorrect(correctAnswer = correctForm)
    }
}
