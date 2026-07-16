package com.studybuddy.core.domain.usecase.conjugation

import com.studybuddy.core.domain.model.Feedback
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CheckConjugationAnswerUseCaseTest {

    private val useCase = CheckConjugationAnswerUseCase()

    @ParameterizedTest
    @CsvSource(
        // input, correct form, strict, expected correct
        "suis, suis, false, true",
        "suis, suis, true, true",
        "SUIS, suis, false, true",
        "' sommes ', sommes, false, true",
        "etes, êtes, false, true",
        "etes, êtes, true, false",
        "êtes, êtes, true, true",
        "sui, suis, false, false",
        "est, es, false, false",
        "aiment, aiment, true, true",
        "vais, va, false, false",
    )
    fun `grades typed forms with accent leniency`(
        input: String,
        correctForm: String,
        strict: Boolean,
        expectedCorrect: Boolean,
    ) {
        val result = useCase(input, correctForm, strict)
        if (expectedCorrect) {
            assertTrue(result is Feedback.Correct, "Expected Correct for '$input' vs '$correctForm'")
        } else {
            assertTrue(result is Feedback.Incorrect, "Expected Incorrect for '$input' vs '$correctForm'")
        }
    }
}
