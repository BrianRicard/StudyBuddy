package com.studybuddy.core.domain.usecase.dictee

import com.studybuddy.core.domain.model.Feedback
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CheckSpellingUseCaseTest {

    private val useCase = CheckSpellingUseCase()

    @ParameterizedTest
    @CsvSource(
        "maison, maison, true, true",
        "maison, maison, false, true",
        "Maison, maison, true, true",
        "Maison, maison, false, true",
        "château, chateau, false, true",
        "château, chateau, true, false",
        "bibliothèque, bibliotheque, false, true",
        "bibliothèque, bibliotheque, true, false",
        "château, chateu, false, false",
        "château, chateu, true, false",
        "école, ecole, false, true",
        "école, ecole, true, false",
        "garçon, garcon, false, true",
        "garçon, garcon, true, false",
    )
    fun `spelling comparison`(
        input: String,
        target: String,
        strict: Boolean,
        expectedCorrect: Boolean,
    ) {
        val result = useCase(input, target, strict)
        if (expectedCorrect) {
            assertTrue(
                result is Feedback.Correct,
                "Expected Correct for input='$input', target='$target', strict=$strict",
            )
        } else {
            assertTrue(
                result is Feedback.Incorrect,
                "Expected Incorrect for input='$input', target='$target', strict=$strict",
            )
        }
    }

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
        assertEquals("château", (result as Feedback.Incorrect).correctAnswer)
    }

    @Test
    fun `wrong word returns Incorrect`() {
        val result = useCase("chateu", "château", accentStrict = false)
        assertTrue(result is Feedback.Incorrect)
    }

    @Test
    fun `wrong letter returns Incorrect with correct answer`() {
        val result = useCase("maisxn", "maison", accentStrict = false)
        assertTrue(result is Feedback.Incorrect)
        assertEquals("maison", (result as Feedback.Incorrect).correctAnswer)
    }

    @Test
    fun `empty input returns Incorrect`() {
        val result = useCase("", "maison", accentStrict = false)
        assertTrue(result is Feedback.Incorrect)
    }

    @Test
    fun `trimmed input matches`() {
        val result = useCase("  maison  ", "maison", accentStrict = true)
        assertTrue(result is Feedback.Correct)
    }
}
