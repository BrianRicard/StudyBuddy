package com.studybuddy.core.common.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class StringExtensionsTest {

    @ParameterizedTest
    @CsvSource(
        "maison, maison, true, true",
        "Maison, maison, true, true",
        "MAISON, maison, true, true",
        "maison, maison, false, true",
        "château, chateau, false, true",
        "château, chateau, true, false",
        "bibliothèque, bibliotheque, false, true",
        "bibliothèque, bibliotheque, true, false",
        "élève, eleve, false, true",
        "élève, eleve, true, false",
        "château, chateu, false, false",
        "château, chateu, true, false",
        "'  maison  ', maison, false, true",
        "'  maison  ', maison, true, true",
        "noël, noel, false, true",
        "noël, noel, true, false",
        "über, uber, false, true",
        "über, uber, true, false",
        "café, cafe, false, true",
        "café, cafe, true, false",
    )
    fun `spelling comparison`(input: String, target: String, strict: Boolean, expected: Boolean) {
        assertEquals(expected, input.matchesWord(target, strict))
    }
}
