package com.studybuddy.app.crash

import java.security.MessageDigest

object CrashFingerprint {

    private const val OUR_PACKAGE = "com.studybuddy"
    private const val MAX_FRAMES = 5

    fun generate(stackTrace: String): String {
        val lines = stackTrace.lines()

        val exceptionClass = lines.firstOrNull()?.trim()?.substringBefore(":")?.trim() ?: "unknown"

        val relevantFrames = lines
            .filter { it.trimStart().startsWith("at $OUR_PACKAGE") }
            .take(MAX_FRAMES)
            .map { it.trim() }

        val fingerprint = buildString {
            appendLine(exceptionClass)
            relevantFrames.forEach { appendLine(it) }
        }

        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(fingerprint.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }.take(12)
    }
}
