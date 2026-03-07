package com.studybuddy.core.domain.model

data class ReadingPassage(
    val id: String,
    val language: String,
    val tier: Int,
    val theme: ReadingTheme,
    val title: String,
    val passage: String,
    val wordCount: Int,
    val source: String,
    val sourceAttribution: String?,
    val questions: List<ReadingQuestion>,
    val bestScore: Int? = null,
    val bestTotal: Int? = null,
    val isLocked: Boolean = false,
)

data class ReadingQuestion(
    val id: String,
    val passageId: String,
    val questionIndex: Int,
    val type: ReadingQuestionType,
    val questionText: String,
    val options: List<String>?,
    val correctAnswer: String,
    val explanation: String,
    val evidenceSentenceIndex: Int,
)

enum class ReadingQuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    FIND_IN_TEXT,
}

enum class ReadingTheme {
    ANIMALS,
    ADVENTURE,
    FAMILY,
    SCHOOL,
    NATURE,
    SCIENCE,
}

data class ReadingResult(
    val id: Long = 0,
    val passageId: String,
    val score: Int,
    val totalQuestions: Int,
    val pointsEarned: Int,
    val readingTimeMs: Long,
    val questionsTimeMs: Long,
    val completedAt: Long,
    val allCorrectFirstTry: Boolean,
)

data class ReadingDictionaryEntry(
    val word: String,
    val language: String,
    val definition: String,
    val translations: Map<String, String>,
    val passageIds: List<String>,
)
