package com.studybuddy.core.common.locale

import java.util.Locale

/**
 * Supported languages for the StudyBuddy app.
 * Each dictée list stores its own language independently of the app UI locale.
 */
enum class SupportedLocale(
    val code: String,
    val displayName: String,
    val javaLocale: Locale,
) {
    FRENCH("fr", "Français", Locale.FRENCH),
    ENGLISH("en", "English", Locale.ENGLISH),
    GERMAN("de", "Deutsch", Locale.GERMAN);

    companion object {
        fun fromCode(code: String): SupportedLocale =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}
