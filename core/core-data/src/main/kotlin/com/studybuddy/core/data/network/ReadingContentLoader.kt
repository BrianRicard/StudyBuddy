package com.studybuddy.core.data.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BundledPassage(
    val id: String,
    val language: String,
    val tier: Int,
    val theme: String,
    val title: String,
    val source: String,
    val sourceAttribution: String? = null,
    val passage: String,
    val wordCount: Int,
    val questions: List<BundledQuestion>,
)

@Serializable
data class BundledQuestion(
    val type: String,
    val questionText: String,
    val options: List<String>? = null,
    val correctAnswer: String,
    val explanation: String,
    val evidenceSentenceIndex: Int,
)

@Serializable
data class BundledReadingContent(
    val version: Int,
    val passages: List<BundledPassage>,
)

@Serializable
data class BundledDictionary(
    val words: List<BundledDictionaryWord>,
)

@Serializable
data class BundledDictionaryWord(
    val word: String,
    val language: String,
    val definition: String,
    val translations: Map<String, String>,
    val passageIds: List<String>,
)

@Singleton
class ReadingContentLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadPassages(language: String): BundledReadingContent {
        val fileName = "reading/${language.lowercase()}.json"
        val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }

    fun loadDictionary(language: String): BundledDictionary {
        val fileName = "reading/dictionary_${language.lowercase()}.json"
        val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }
}
