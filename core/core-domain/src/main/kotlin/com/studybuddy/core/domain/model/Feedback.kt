package com.studybuddy.core.domain.model

sealed interface Feedback {
    data object Correct : Feedback
    data class Incorrect(val correctAnswer: String) : Feedback
    data object TimeUp : Feedback
}
