package com.studybuddy.core.domain.usecase.math

import com.studybuddy.core.domain.model.Feedback
import com.studybuddy.core.domain.model.MathProblem
import javax.inject.Inject

class CheckAnswerUseCase @Inject constructor() {
    operator fun invoke(problem: MathProblem, userAnswer: Int): Feedback {
        return if (userAnswer == problem.correctAnswer) {
            Feedback.Correct
        } else {
            Feedback.Incorrect(correctAnswer = problem.correctAnswer.toString())
        }
    }
}
