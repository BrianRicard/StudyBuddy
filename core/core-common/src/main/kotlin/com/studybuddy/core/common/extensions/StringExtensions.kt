package com.studybuddy.core.common.extensions

import java.text.Normalizer

/**
 * Accent-aware string comparison for dictée grading.
 *
 * @param target The correct word
 * @param strict If true, accents must match exactly. If false, base characters are compared.
 * @return true if the strings match under the given strictness
 */
fun String.matchesWord(target: String, strict: Boolean = false): Boolean {
    if (strict) return this.trim().equals(target.trim(), ignoreCase = true)
    return normalize(this) == normalize(target)
}

/**
 * Strips diacritical marks from a string using Unicode NFD normalization.
 */
fun String.stripAccents(): String = normalize(this)

private fun normalize(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFD)
        .replace(Regex("[\\p{InCombiningDiacriticalMarks}]"), "")
        .lowercase()
        .trim()
