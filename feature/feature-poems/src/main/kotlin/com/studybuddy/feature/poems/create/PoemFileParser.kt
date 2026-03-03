package com.studybuddy.feature.poems.create

import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.model.PoemSource
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ImportablePoem(
    val title: String,
    val author: String = "",
    val lines: List<String>,
    val tags: List<String> = emptyList(),
)

object PoemFileParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parseTextFile(
        content: String,
        language: String,
    ): Result<List<Poem>> = runCatching {
        val lines = content.lines().filter { it.isNotBlank() }
        require(lines.size >= 2) { "File must have at least a title and one poem line" }

        val title = lines.first().trim()
        val author = lines[1].trim()
        val poemLines = lines.drop(2).map { it.trim() }

        listOf(
            Poem(
                id = UUID.randomUUID().toString(),
                title = title,
                author = author,
                lines = poemLines.ifEmpty { listOf(author).also { /* single-line poem: author line is the poem */ } },
                language = language,
                source = PoemSource.USER,
            ),
        )
    }

    fun parseJsonFile(
        content: String,
        language: String,
    ): Result<List<Poem>> = runCatching {
        val importable = try {
            listOf(json.decodeFromString<ImportablePoem>(content))
        } catch (_: Exception) {
            json.decodeFromString<List<ImportablePoem>>(content)
        }

        require(importable.isNotEmpty()) { "JSON file contained no poems" }

        importable.map { item ->
            Poem(
                id = UUID.randomUUID().toString(),
                title = item.title,
                author = item.author,
                lines = item.lines,
                language = language,
                source = PoemSource.USER,
                tags = item.tags,
            )
        }
    }
}
