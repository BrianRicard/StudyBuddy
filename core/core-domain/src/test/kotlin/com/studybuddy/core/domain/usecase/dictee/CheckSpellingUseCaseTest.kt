package com.studybuddy.core.domain.usecase.dictee

import com.studybuddy.core.domain.model.Feedback
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CheckSpellingUseCaseTest {

    private val useCase = CheckSpellingUseCase()

    @Test
    fun `exact match returns Correct`() {
        val result = useCase("maison", "maison", accentStrict = true)
        assertTrue(result is Feedback.Correct)
    }

    @Test
    fun `case insensitive match returns Correct`() {
        val result = useCase("Maison", "maison", accentStrict = true)
        assertTrue(result is Feedback.Correct)
    }

    @Test
    fun `accent lenient match returns Correct`() {
        val result = useCase("chateau", "château", accentStrict = false)
        assertTrue(result is Feedback.Correct)
    }

    @Test
    fun `accent strict mismatch returns Incorrect`() {
        val result = useCase("chateau", "château", accentStrict = true)
        assertTrue(result is Feedback.Incorrect)
        assertTrue((result as Feedback.Incorrect).correctAnswer == "château")
    }

    @Test
    fun `wrong word returns Incorrect`() {
        val result = useCase("chateu", "château", accentStrict = false)
        assertTrue(result is Feedback.Incorrect)
    }

    @Test
    fun `trimmed input matches`() {
        val result = useCase("  maison  ", "maison", accentStrict = true)
        assertTrue(result is Feedback.Correct)
    }
}
